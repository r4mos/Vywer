package com.vywer.vywer;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity
        extends ActionBarActivity
        implements Const,
                   NavigationDrawerFragment.NavigationDrawerCallbacks,
                   OnMapReadyCallback,
                   SensorEventListener,
                   LocationListener,
                   TextToSpeech.OnInitListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private View mNavPanel;
    private TextView mNavPanelLeft;
    private TextView mNavPanelText;
    private View mStartNavPanel;
    private ImageView mStartNavPanelLeft;
    private TextView mStartNavPanelText;

    private SharedPreferences mLastLocation;
    private SharedPreferences mSettings;

    protected LocationManager mLocationManager;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private TextToSpeech mTextToSpeech;

    private static float azimuthOrientation = 0.0f;
    private float[] mGravity;
    private float[] mGeomagnetic;

    private GoogleMap mMap = null;
    private CameraPosition mCP = null;
    private Route mRoute = null;
    private boolean mRecalculate = false;
    private Boolean mNewAction = true;
    private int mStep = 0;
    private float mMetersToGoal = Float.MAX_VALUE;

    private Boolean isNav = false;
    private Boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Scren on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Activity animations
        overridePendingTransition(R.anim.activity_open_translate,R.anim.activity_close_scale);

        mLastLocation = getSharedPreferences("LastLocation", Context.MODE_PRIVATE);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mNavPanel = findViewById(R.id.navPanel);
        mNavPanel.setOnTouchListener(new OnSwipeTouchListener(getBaseContext()) {
            @Override
            public void onSwipeLeft() {
                mNavPanel.startAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), false));
                mNavPanel.setVisibility(View.INVISIBLE);
                navMode(false);
            }
            @Override
            public void onSwipeRight() {
                mNavPanel.startAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), true));
                mNavPanel.setVisibility(View.INVISIBLE);
                navMode(false);
            }
        });
        mNavPanelLeft = (TextView)findViewById(R.id.navPanelLeft);
        mNavPanelText = (TextView)findViewById(R.id.navPanelText);
        mStartNavPanel = findViewById(R.id.startNavPanel);
        mStartNavPanel.setOnTouchListener(new OnSwipeTouchListener(getBaseContext()) {
            @Override
            public void onSwipeLeft() {
                mStartNavPanel.startAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), false));
                mStartNavPanel.setVisibility(View.INVISIBLE);
                navMode(false);
            }
            @Override
            public void onSwipeRight() {
                mStartNavPanel.startAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), true));
                mStartNavPanel.setVisibility(View.INVISIBLE);
                navMode(false);
            }
        });
        View.OnClickListener clickToStartNav = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartNavPanel.setVisibility(View.INVISIBLE);
                navMode(true);
            }
        };
        mStartNavPanelLeft = (ImageView)findViewById(R.id.startNavPanelLeft);
        mStartNavPanelLeft.setOnClickListener(clickToStartNav);
        mStartNavPanelText = (TextView)findViewById(R.id.startNavPanelText);
        mStartNavPanelText.setOnClickListener(clickToStartNav);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mTextToSpeech = new TextToSpeech(this, this);

        mTitle = getTitle();
    }

    private void showMap(Boolean map) {
        View container = findViewById(R.id.container);
        View loading = findViewById(R.id.loading);

        if (map) {
            container.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            mNavigationDrawerFragment.setMenuVisibility(true);
        } else {
            container.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            mNavigationDrawerFragment.setMenuVisibility(false);
        }
    }

    private void saveLastLocation(){
        SharedPreferences.Editor editor = mLastLocation.edit();
        editor.putString( "latitude",
            String.valueOf(getLastLatLng().latitude) );
        editor.putString( "longitude",
            String.valueOf(getLastLatLng().longitude) );
        editor.apply();
    }

    private LatLng getLastLatLng() {
        if (mMap != null && mMap.getMyLocation() != null) {
            return new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
        } else {
            return new LatLng(Double.parseDouble(mLastLocation.getString("latitude",  "40.412691")),
                              Double.parseDouble(mLastLocation.getString("longitude", "-3.705493")) );
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void getRoute(LatLng destination) {
        isNav = false;
        mStep = 0;
        mMetersToGoal = Float.MAX_VALUE;

        if (mMap == null || mCP == null) return; //Control map & camera error
        if (!isOnline()) {                       //Control Internet connexion
            Toast.makeText(getBaseContext(), R.string.alert_no_internet,Toast.LENGTH_SHORT).show();
            return;
        }

        //Get HTTP Request
        String url = getMapsApiDirectionsUrl(getLastLatLng(), destination);

        //Hide map
        showMap(false);

        //Run tasks
        ReadUrlTask downloadUrl = new ReadUrlTask();
        downloadUrl.execute(url);
    }

    private void drawRoute() {
        if (mMap == null || mRoute == null) return; //Control map & route errors
        mMap.clear();

        PolylineOptions polyLineOptions = new PolylineOptions();
        polyLineOptions.addAll(mRoute.getPolyline());
        polyLineOptions.width(10);
        polyLineOptions.color(Color.BLUE);

        mMap.addPolyline(polyLineOptions);

        mMap.addMarker(new MarkerOptions().position(mRoute.getDestination()));
    }

    private String getMapsApiDirectionsUrl(LatLng start, LatLng end) {
        String url = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&language=" + getString(R.string.app_language)
                + "&sensor=false"
                + "&units=metric"
                + "&mode=" + mSettings.getString("settingsTransport", SETTINGS_TRANSPORT);
        if (mSettings.getBoolean("settingsAvoidTolls", SETTINGS_AVOIDTOLLS))     url += "&avoid=tolls";
        if (mSettings.getBoolean("settingsAvoidHighways", SETTINGS_AVOIDHIGHWAYS))  url += "&avoid=highways";
        Log.v("URL: ", url);
        return url;
    }

    private void navMode(boolean navMode) {
        if (mMap == null) return; //Control map error

        if (navMode && mRoute != null) {
            isNav = true;

            //Change title
            mTitle = getString(R.string.title_section_going);
            setTitle(mTitle);

            //Change map options
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(false);

            //Change camera
            if (mSettings.getBoolean("settingsDisplayOrientation", SETTINGS_ORIENTATION)) {
                mCP = new CameraPosition(getLastLatLng(), 18, 40, 0);
            } else {
                mCP = new CameraPosition(getLastLatLng(), 18, 0, 0);
            }
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCP));

            //Open nav panel
            showNavPanel(true);
        } else {
            isNav = false;
            mStep = 0;

            //Change title
            mTitle = getString(R.string.title_section_explore);
            setTitle(mTitle);

            //Clean
            if (mRoute != null) mRoute = null;
            mMap.clear();

            //Change map options
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);

            //Change camera
            mCP = new CameraPosition(getLastLatLng(), 18, 0, 0);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCP));

            //Close nav panel
            if (mNavPanel.getVisibility() == View.VISIBLE) {
                showNavPanel(false);
            }
        }
    }

    private float kmhToMs(int k){
        return (float)(k/3.6);
    }

    private float getDistance(LatLng start, LatLng end) {
        float [] distance = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, distance);
        return distance[0];
    }
    private void startAction(int step) {
        startAction( mRoute.getLegs().get(step).getInstuctions(),
                mRoute.getLegs().get(step).getIstructionsText(),
                mRoute.getLegs().get(step).getDistance() );
    }
    private void startAction(int action, String description, float distance) {
        //Description text
        if (description.length() < 60) {
            mNavPanelText.setTextAppearance(getBaseContext(), R.style.TextAppearance_AppCompat_Large);
        } else {
            mNavPanelText.setTextAppearance(getBaseContext(), R.style.TextAppearance_AppCompat_Medium);
        }
        mNavPanelText.setText(description);

        //Image
        switch (action) {
            case WRONG:
                setNavImage(R.drawable.ic_u_turn);
                break;
            case STRAIGHT:
                setNavImage(R.drawable.ic_continue);
                break;
            case LEFT:
                setNavImage(R.drawable.ic_turn_left);
                break;
            case RIGHT:
                setNavImage(R.drawable.ic_turn_right);
                break;
            case UTURN:
                setNavImage(R.drawable.ic_u_turn);
                break;
            case FERRY:
                setNavImage(R.drawable.ic_ferry);
                break;
            case DESTINATION:
                setNavImage(R.drawable.ic_arrived);
                break;
            default:
                setNavImage(R.drawable.ic_roundabout);
        }

        //Sound
        if (mSettings.getBoolean("settingsAlertsSound", SETTINGS_SOUND) && isNav) {
            if (mTextToSpeech != null && mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }

            switch (action) {
                case WRONG:
                    convertTextToSpeech( description );
                    break;
                case STRAIGHT:
                case LEFT:
                case RIGHT:
                case UTURN:
                case FERRY:
                case DESTINATION:
                    convertTextToSpeech( description
                            + " "
                            + getString(R.string.sound_during)
                            + " "
                            + String.valueOf(distance)
                            + " "
                            + getString(R.string.sound_meters) );
                    break;
                default:
                    convertTextToSpeech( getString(R.string.sound_to)
                            + " "
                            + String.valueOf(distance)
                            + " "
                            + getString(R.string.sound_meters)
                            + ", "
                            + description );
            }
        }
    }
    private void stopAction() {
        //Sound
        if (mTextToSpeech != null && mTextToSpeech.isSpeaking()) {
            mTextToSpeech.stop();
        }
    }

    private void showNavPanel(boolean show) {
        if (show) {
            mNavPanel.startAnimation( AnimationUtils.loadAnimation(this, android.R.anim.fade_in) );
            mNavPanel.setVisibility(View.VISIBLE);
        } else {
            mNavPanel.startAnimation(AnimationUtils.makeOutAnimation(this, true));
            mNavPanel.setVisibility(View.INVISIBLE);
        }
    }

    private void setNavImage (int id) {
        Drawable image = getResources().getDrawable( id );
        image.setBounds( 0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight() );
        mNavPanelLeft.setCompoundDrawables( null, image, null, null );
    }

    private void setNavDistance (float distance) {
        if (distance > 1000) {
            mNavPanelLeft.setText(String.format("%.1f", distance/1000) + "km");
        } else {
            mNavPanelLeft.setText(String.format("%.0f", distance) + "m");
        }
    }

    private void setStartNavPanelImageBySettings() {
        String transport = mSettings.getString("settingsTransport", SETTINGS_TRANSPORT);
        if ( transport.equalsIgnoreCase("walking") ) {
            mStartNavPanelLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_walking));
        } else if ( transport.equalsIgnoreCase("bicycling") ) {
            mStartNavPanelLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_bicycling));
        } else {
            mStartNavPanelLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_driving));
        }
    }

    @SuppressWarnings("deprecation")
    private void convertTextToSpeech(String text) {
        if (mSettings.getBoolean("settingsAlertsSound", SETTINGS_SOUND) && !text.equals("") && mTextToSpeech != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        //Internet control
        if (!isOnline()) {
            Toast.makeText(getBaseContext(), R.string.alert_no_internet, Toast.LENGTH_SHORT).show();
        }
        //Start Orientation Updates
        if (mSettings.getBoolean("settingsDisplayOrientation", SETTINGS_ORIENTATION)) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        //Start Location Updates
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME_UPDATE, GPS_MIN_DISTANCE_UPDATE, this);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                onLocationChanged(location);
            }
        } else {
            Toast.makeText(getBaseContext(), R.string.alert_no_gps, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
        saveLastLocation();
        //Stop Orientation Updates
        mSensorManager.unregisterListener(this);
        //Close activity animations
        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLastLocation();
        //Stop Location Updates
        mLocationManager.removeUpdates(this);
        //Stop TextToSpeech
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        //Exit dialog
        if (isNav) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setMessage(R.string.alert_exit_on_nav);
            d.setCancelable(false);
            d.setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int id) {
                    finish();
                }
            });
            d.setNegativeButton(R.string.alert_no, null);
            d.show();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTINGS:
                if (mSettings.getBoolean("settingsDisplayOrientation", SETTINGS_ORIENTATION)) {
                    if (isNav) {
                        mCP = new CameraPosition(getLastLatLng(), 18, 40, 0);
                    }
                } else {
                    mCP = new CameraPosition(mCP.target, mCP.zoom, 0, 0);
                }
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCP));
                break;

            case GO:
                setTitle(getString(R.string.title_section_explore));
                navMode(false);

                if(resultCode == RESULT_OK){
                    getRoute(new LatLng(data.getDoubleExtra("lat", 39.40642),
                                        data.getDoubleExtra("lon", -3.11702477)));
                }
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getBaseContext(), R.string.alert_no_destination,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Intent intent;

        switch (position) {
            case EXPLORE:
                navMode(false);
                break;
            case GO:
                intent = new Intent(getBaseContext(), SelectEndActivity.class);
                startActivityForResult(intent, GO);
                break;
            case SETTINGS:
                intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivityForResult(intent, SETTINGS);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                navMode(true);
                return true;
            }
        });

        navMode(false);
        showMap(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLastLatLng(), 18));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isNav || !isVisible || !mSettings.getBoolean("settingsDisplayOrientation", SETTINGS_ORIENTATION)) return; //!isActive
        if (mMap == null || mCP == null) return; //Control map & camera error

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float)Math.toDegrees(orientation[0]);

                if (Math.abs(azimuth-azimuthOrientation) > ORIENTATION_MIN_ANGLE_UPDATE) {
                    azimuthOrientation = azimuth;
                    mCP = new CameraPosition(mCP.target, mCP.zoom, mCP.tilt, azimuthOrientation);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCP));
                } else if (Math.abs(azimuth-azimuthOrientation) > ORIENTATION_MIN_ANGLE_UPDATE_WITHOUT_ANIMATION) {
                    azimuthOrientation = azimuth;
                    mCP = new CameraPosition(mCP.target, mCP.zoom, mCP.tilt, azimuthOrientation);
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCP));
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onLocationChanged(Location loc) {
        if (mMap == null || mCP == null) return; //Control map & camera error

        int metersToFirstWarning;

        if (isVisible && isNav) {
            //Update zoon by speed
            float speed = loc.getSpeed();
            if (speed > kmhToMs(100)) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                metersToFirstWarning = 1000;
            } else if (speed > kmhToMs(80)) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                metersToFirstWarning = 500;
            } else if (speed > kmhToMs(50)) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                metersToFirstWarning = 100;
            } else if (speed > kmhToMs(20)) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                metersToFirstWarning = 60;
            } else {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                metersToFirstWarning = 30;
            }

            //Update camera position on navMode
            LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (!location.equals(mCP.target)) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
            }

            //Navigation
            if (isNav) {
                float distance = getDistance(location, mRoute.getLegs().get(mStep).getOrigin());

                if (distance < metersToFirstWarning) {
                    if (distance <= mMetersToGoal) {
                        if (mNewAction || mMetersToGoal == Float.MAX_VALUE) {
                            mNewAction = false;
                            if (mStep == mRoute.getLegs().size()-1) {
                                startAction(mRoute.getLegs().get(mStep).getInstuctions(),
                                            mRoute.getLegs().get(mStep).getIstructionsText(),
                                            distance );
                            } else {
                                startAction(mStep);
                            }
                        }
                        mMetersToGoal = distance;
                    } else {
                        mNewAction = true;
                        stopAction();
                        mMetersToGoal = Float.MAX_VALUE;

                        if (mStep == mRoute.getLegs().size()-1) {
                            navMode(false);
                        } else {
                            mStep++;
                        }
                    }
                } else {
                    if (mNewAction) {
                        mNewAction = false;
                        startAction(STRAIGHT, getString(R.string.instructions_straight), distance);
                    }
                }

                setNavDistance(distance);
            }

            //Deviation from selected route
            if (PolyUtil.isLocationOnPath(location, mRoute.getPolyline(), false, ROUTE_DEVIATION_DISTANCE)) {
                startAction(WRONG, getString(R.string.instructions_recalculate), 0);
                mRecalculate = true;
                getRoute(mRoute.getDestination());
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(new Locale(getString(R.string.app_language)));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getBaseContext(),R.string.alert_no_sound, Toast.LENGTH_SHORT).show();
            } else {
                convertTextToSpeech("");
            }
        }
    }

    private class ReadUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... path) { //Get json file
            String data = "";
            InputStream iStream;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(path[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
                iStream.close();
            } catch (Exception e) {
                return "";
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("")) {
                showMap(true);
                Toast.makeText(getBaseContext(), R.string.alert_error_loading_route,Toast.LENGTH_SHORT).show();
            } else {
                new JSONParserTask().execute(result);
            }
        }
    }

    private class JSONParserTask extends AsyncTask<String, Integer, Route> {
        @Override
        protected Route doInBackground(String... jsonData) {
            JSONObject jObject;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                List<Route> routes = parser.parse(jObject, getBaseContext());
                if (routes.size() > 0) {
                    return routes.get(0); //Get first route
                } else {
                    return new Route();
                }
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(Route route) {
            if (route == null) {
                showMap(true);
                Toast.makeText(getBaseContext(), R.string.alert_error_loading_route, Toast.LENGTH_SHORT).show();
            } else if (!route.getStatus()) {
                showMap(true);
                Toast.makeText(getBaseContext(), R.string.alert_error_calculating_route, Toast.LENGTH_SHORT).show();
            } else {
                mRoute = route;
                drawRoute();
                startAction(mStep);
                showMap(true);

                if (mRecalculate) {
                    mRecalculate = false;
                } else {
                    //Move camara to show all route
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    List<LatLng> points = mRoute.getPolyline();
                    for (int i = 0; i < points.size(); i++) {
                        builder.include(points.get(i));
                    }
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                    mMap.animateCamera(cu);
                    mCP = mMap.getCameraPosition();

                    //Show button panel
                    mStartNavPanelText.setText(mRoute.getDestinationText());
                    setStartNavPanelImageBySettings();
                    mStartNavPanel.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                    mStartNavPanel.setVisibility(View.VISIBLE);
                }

                mStep = 1;
            }
        }
    }
}