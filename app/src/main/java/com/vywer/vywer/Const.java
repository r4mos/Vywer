package com.vywer.vywer;

public interface Const {
    //Default Settings
    static final boolean SETTINGS_ORIENTATION = true;
    static final boolean SETTINGS_SOUND = false;
    static final String SETTINGS_TRANSPORT = "driving";
    static final boolean SETTINGS_AVOIDTOLLS = true;
    static final boolean SETTINGS_AVOIDHIGHWAYS = false;

    //Avanced Settings
    static final float ORIENTATION_MIN_ANGLE_UPDATE = 8.0f;
    static final float ORIENTATION_MIN_ANGLE_UPDATE_WITHOUT_ANIMATION = 15.0f;
    static final long GPS_MIN_TIME_UPDATE = 1000;
    static final long GPS_MIN_DISTANCE_UPDATE = 1;
    static final double ROUTE_DEVIATION_DISTANCE = 5.0;
    static final int SWIPE_DISTANCE_THRESHOLD = 100;
    static final int SWIPE_VELOCITY_THRESHOLD = 100;

    //Menu & Request Code
    static final int EXPLORE = 0;
    static final int GO = 1;
    static final int SETTINGS = 2;

    //Actions
    static final int WRONG = -1;
    static final int STRAIGHT = 0;
    static final int LEFT = 10;
    static final int RIGHT = 1;
    static final int UTURN = 11;
    static final int FERRY = 12;
    static final int DESTINATION = 19;
    static final int ROUNDABOUT = 20; // Store ROUNDABOUT + Exit number. Example: 21=first
}
