package com.vywer.vywer;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class Step implements Const {
    Context context;

    private LatLng origin;
//    private LatLng destination;
    private int distance; //meters
//    private int duration; //segs
    private int instuctions = STRAIGHT;
    private String htmlInstructions = "";

    public Step(Context context) {
        this.context = context;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

//    public LatLng getDestination() {
//        return destination;
//    }
//
//    public void setDestination(LatLng destination) {
//        this.destination = destination;
//    }
//
    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
//
//    public int getDuration() {
//        return duration;
//    }
//
//    public void setDuration(int duration) {
//        this.duration = duration;
//    }

    public int getInstuctions() {
        return instuctions;
    }

    //Convierte las posibles acciones de la api (http://stackoverflow.com/questions/17941812/google-directions-api)
    // en nuestras acciones definidas en Const
    public void setInstuctions(String i) {
        if (i.equalsIgnoreCase("straight"))                 this.instuctions = STRAIGHT;
        else if (i.equalsIgnoreCase("turn-left"))           this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("turn-right"))          this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("destination"))         this.instuctions = DESTINATION;
        else if (i.equalsIgnoreCase("turn-sharp-left"))     this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("turn-sharp-right"))    this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("turn-slight-left"))    this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("turn-slight-right"))   this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("keep-left"))           this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("keep-right"))          this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("ramp-left"))           this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("ramp-right"))          this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("fork-left"))           this.instuctions = LEFT;
        else if (i.equalsIgnoreCase("fork-right"))          this.instuctions = RIGHT;
        else if (i.equalsIgnoreCase("uturn-left"))          this.instuctions = UTURN;
        else if (i.equalsIgnoreCase("uturn-right"))         this.instuctions = UTURN;
        else if (i.equalsIgnoreCase("ferry-train"))         this.instuctions = FERRY;
        else if (i.equalsIgnoreCase("ferry"))               this.instuctions = FERRY;
        else if (i.equalsIgnoreCase("merge"))               this.instuctions = STRAIGHT;
        else if (i.equalsIgnoreCase("roundabout-left"))     this.instuctions = ROUNDABOUT + getRoundaboutNumber ();
        else if (i.equalsIgnoreCase("roundabout-right"))    this.instuctions = ROUNDABOUT + getRoundaboutNumber ();
    }

    private int getRoundaboutNumber () {
        if (!htmlInstructions.equals("")) {
            String exitText = htmlInstructions.split("<b>")[1].split("</b>")[0];
            String[] exits_array  = context.getResources().getStringArray(R.array.exits_array);

            for (int i = 0; i < exits_array.length; i++) {
                if (exitText.equalsIgnoreCase(exits_array[i])) {
                    return i + 1;
                }
            }
        }
        return 1;
    }

    public String getIstructionsText() {
        return this.htmlInstructions.replaceAll("\\<.*?>","");
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }
}
