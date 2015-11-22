package com.vywer.vywer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

public class PathJSONParser {

    public List<Route> parse(JSONObject jObject, Context context) {
        List<Route> routes = new ArrayList<>();

        try {
            if (jObject.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                return routes;
            }

            JSONArray jRoutes = jObject.getJSONArray("routes");
            /* Traversing all routes */
            // for (int i = 0; i < jRoutes.length(); i++) {
            for (int i = 0; i < 1; i++) { //Como solo se coge la primera en el MainActivity no hay que calcular mas
                Route route = new Route();
                JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                //Overview
                String overwiew = ((JSONObject) ((JSONObject) jRoutes.get(i)).get("overview_polyline")).getString("points");
                route.setPolyline(PolyUtil.decode(overwiew));

//                //Origin
//                route.setOrigin(new LatLng(((JSONObject) ((JSONObject) jLegs.get(0)).get("start_location")).getDouble("lat"),
//                                           ((JSONObject) ((JSONObject) jLegs.get(0)).get("start_location")).getDouble("lng") ));
//                route.setOriginText( ((JSONObject)jLegs.get(0)).getString("start_address") );

                //Destination
                route.setDestination(new LatLng(((JSONObject) ((JSONObject) jLegs.get(jRoutes.length()-1)).get("end_location")).getDouble("lat"),
                                                ((JSONObject) ((JSONObject) jLegs.get(jRoutes.length()-1)).get("end_location")).getDouble("lng") ));
                route.setDestinationText( ((JSONObject)jLegs.get(jRoutes.length()-1)).getString("end_address") );

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
//                    //Distance
//                    route.setDistance( route.getDistance() +
//                                       ((JSONObject) ((JSONObject) jLegs.get(j)).get("distance")).getInt("value") );
//
//                    //Duration
//                    route.setDuration( route.getDuration() +
//                                       ((JSONObject) ((JSONObject) jLegs.get(j)).get("duration")).getInt("value") );

                    //Steps
                    JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    List<Step> steps = new ArrayList<>();

                    /* Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        Step step = new Step(context);

                        //Origin
                        step.setOrigin(new LatLng( ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).getDouble("lat"),
                                                   ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).getDouble("lng") ));

//                        //Destination
//                        step.setDestination(new LatLng( ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).getDouble("lat"),
//                                                        ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).getDouble("lng") ));

                        //Distance
                        step.setDistance( ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).getInt("value") );

//                        //Duration
//                        step.setDuration( ((JSONObject) ((JSONObject) jSteps.get(k)).get("duration")).getInt("value") );

                        //HtmlInstructions
                        step.setHtmlInstructions( ((JSONObject) jSteps.get(k)).getString("html_instructions") );

                        //Instructions
                        if ( ((JSONObject) jSteps.get(k)).has("maneuver") ) {
                            step.setInstuctions( ((JSONObject) jSteps.get(k)).getString("maneuver") );
                        }

                        steps.add(step);
                    }

                    Step step = new Step(context);
                    step.setOrigin(route.getDestination());
                    step.setInstuctions("destination");
                    step.setHtmlInstructions(context.getResources().getString(R.string.instructions_destination));
                    steps.add(step);

                    route.setLegs(steps);
                }

                route.setStatus(true);

                routes.add(route);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }
}