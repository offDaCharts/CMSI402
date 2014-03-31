package com.google.android.glass.sample.stopwatch;

//GPS stuff
import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.content.Context;
import android.content.Intent;


/**
* View used to display draw a running Chronometer.
*
* This code is greatly inspired by the Android's Chronometer widget.
*/
public class GPSActivity extends Activity {

  private LocationManager mgr=null;
  private String current_location = "not yet initialized";

	public GPSActivity() {
        Log.d("GPS_service", "gps constructor");
        current_location = "through gps constructor";

	}
	
	public void start() {
	    Intent intent = new Intent(GPSActivity.this, GPSActivity.class);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  	startActivity(intent);
	 }

	@Override
	public void onCreate(Bundle icicle) {
        Log.d("GPS_service", "Setting up gps");
	    super.onCreate(icicle);
	    //setContentView(R.layout.main);
	    current_location = "creating gps";

	    mgr=(LocationManager)getSystemService(LOCATION_SERVICE);


//	    mgr.requestSingleUpdate(LocationManager.GPS_PROVIDER,
//		    	onLocationChange, null);
//
//	    mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//		    	5000, 1,  //5000 milliseconds, mindistance of 1 meter
//		    	onLocationChange);
	}
	
	public String getLocString() {
		return current_location;
	}


  LocationListener onLocationChange=new LocationListener() {
      public void onLocationChanged(Location loc) {
        String locationString=loc.getLatitude() + "," + loc.getLongitude();
        Log.d("GPS_service", locationString);
        current_location = locationString;
      }
      
      public void onProviderDisabled(String provider) {
        // required for interface, not used
      }
      
      public void onProviderEnabled(String provider) {
        // required for interface, not used
      }
      
      public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        // required for interface, not used
      }
    };
  
}