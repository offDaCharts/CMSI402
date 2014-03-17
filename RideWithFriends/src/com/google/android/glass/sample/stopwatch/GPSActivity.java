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


/**
 * View used to display draw a running Chronometer.
 *
 * This code is greatly inspired by the Android's Chronometer widget.
 */
public class GPSActivity extends Activity {

    private LocationManager mgr=null;
	
	public GPSActivity() {
	    mgr=(LocationManager)getSystemService(LOCATION_SERVICE);
	}
	
	public String getLocString() {
		return null;
	}
	

       
    
    
//    //get updates
//    mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//            3600000, 1000,
//            onLocationChange);
//    
//    String url=String.format(format, loc.getLatitude(),
//            loc.getLongitude());
//	
//    //pause
//    mgr.removeUpdates(onLocationChange);
//    
	
    LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location loc) {
          String locationString=loc.getLatitude() + "," + loc.getLongitude();
          Log.d("GPS_service", locationString);
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