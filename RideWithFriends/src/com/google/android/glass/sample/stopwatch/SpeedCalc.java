package com.google.android.glass.sample.stopwatch;

import android.location.Location;
import android.util.Log;

public class SpeedCalc {
	public static Location startLocation = null;
	public static Location lastLocation = null;
	public static double distanceTraveled = 0;	//meters
	public static double distanceDelta = 0;	//meters
	public static long startMillis = 0;
	public static long lastMillis = 0;
	public static long timeDelta = 0;
	public static double instantSpeed = 0;	//meters/ms
	public static double maxSpeed = 0;	//meters/ms
	public static double avgSpeed = 0;	//meters/ms
	
	
	
	public SpeedCalc() {}
	
	public static void tick(long millis, Location currentLocation) {
    	Log.d("speed calc", "tick");
		if(lastLocation == null) {
	    	Log.d("speed calc", "init");
			startLocation = currentLocation;
			lastLocation = currentLocation;
			startMillis = millis;
			lastMillis = millis;
		} else {
	    	Log.d("speed calc", "continue tick");
			distanceDelta = Math.abs(lastLocation.distanceTo(currentLocation));
	    	Log.d("speed calc", "deltaDist: " + distanceDelta);

			distanceTraveled += distanceDelta;
			lastLocation = currentLocation;
			
			timeDelta = millis - lastMillis;
			lastMillis = millis;
			
			instantSpeed = distanceDelta/timeDelta;
			if(instantSpeed > maxSpeed) {
				maxSpeed = instantSpeed;
			}
	    	Log.d("speed calc", "instant: " + instantSpeed);
			avgSpeed = distanceTraveled/(millis-startMillis);
	    	Log.d("speed calc", "avg: " + avgSpeed);
		}
	}
	
	public static double metersPerMillisToMph(double metersPerMillis) {
		return metersPerMillis*2.23694*1000;
	}
	
	public static double metersToMiles(double meters) {
		return meters/1609.34;
	}	
}
