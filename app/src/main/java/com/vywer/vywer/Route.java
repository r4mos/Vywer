package com.vywer.vywer;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    private Boolean status = false;
//    private LatLng origin;
//    private String originText;
    private LatLng destination;
    private String destinationText;
//    private int distance = 0; //meters
//    private int duration = 0; //segs
    private List<Step> legs;
    private List<LatLng> polyline;

    public Route(){
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

//    public LatLng getOrigin() {
//        return origin;
//    }
//
//    public void setOrigin(LatLng origin) {
//        this.origin = origin;
//    }
//
//    public String getOriginText() {
//        return originText;
//    }
//
//    public void setOriginText(String originText) {
//        this.originText = originText;
//    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getDestinationText() {
        return destinationText;
    }

    public void setDestinationText(String destinationText) {
        this.destinationText = destinationText;
    }

//    public int getDistance() {
//        return distance;
//    }
//
//    public void setDistance(int distance) {
//        this.distance = distance;
//    }
//
//    public int getDuration() {
//        return duration;
//    }
//
//    public void setDuration(int duration) {
//        this.duration = duration;
//    }

    public List<Step> getLegs() {
        return legs;
    }

    public void setLegs(List<Step> legs) {
        this.legs = legs;
    }

    public List<LatLng> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<LatLng> polyline) {
        this.polyline = polyline;
    }
}
