package com.google.android.glass.sample.stopwatch;

//GPS stuff
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;


/**
* View used to display draw a running Chronometer.
*
* This code is greatly inspired by the Android's Chronometer widget.
*/
public class GPSActivity extends Activity {

  private LocationManager locationManager;
  private String current_location = "not yet initialized";
  private String bestProvider;  
  
  public final class MyLocationListener implements LocationListener {	  
      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {
          switch (status) {
          case LocationProvider.AVAILABLE:
              Log.d("GPS_service", "GPS available again\n");
              break;
          case LocationProvider.OUT_OF_SERVICE:
              Log.d("GPS_service", "GPS out of service\n");
              break;
          case LocationProvider.TEMPORARILY_UNAVAILABLE:
              Log.d("GPS_service", "GPS temporarily unavailable\n");
              break;
          }
      }

      @Override
      public void onProviderEnabled(String provider) {
    	  Log.d("GPS_service", "GPS Provider Enabled\n");
      }

      @Override
      public void onProviderDisabled(String provider) {
    	  Log.d("GPS_service", "GPS Provider Disabled\n");
      }

      @Override
      public void onLocationChanged(Location location) {
          locationManager.removeUpdates(networkLocationListener);
          current_location = "New GPS location: "
                  + String.format("%9.6f", location.getLatitude()) + ", "
                  + String.format("%9.6f", location.getLongitude()) + "\n";
          Log.d("GPS_service", current_location);
      }
      
      public void checkLocation() {
    	  try {
          Log.d("GPS_service", "here");
          Criteria criteria = new Criteria();
          bestProvider = locationManager.getBestProvider(criteria, true);
          Log.d("GPS_service", "there");

          Log.d("GPS_service", bestProvider);
          Log.d("GPS_service", bestProvider);
          Log.d("GPS_service", onLocationChange.toString());

    		  locationManager.requestSingleUpdate(bestProvider, onLocationChange, null);
    	  } catch (Exception e) {
    	  }
      }
  }
  
  private final MyLocationListener gpsLocationListener =new MyLocationListener();
  
  
  
//  {
//
//      @Override
//      public void onStatusChanged(String provider, int status, Bundle extras) {
//          switch (status) {
//          case LocationProvider.AVAILABLE:
//              Log.d("GPS_service", "GPS available again\n");
//              break;
//          case LocationProvider.OUT_OF_SERVICE:
//              Log.d("GPS_service", "GPS out of service\n");
//              break;
//          case LocationProvider.TEMPORARILY_UNAVAILABLE:
//              Log.d("GPS_service", "GPS temporarily unavailable\n");
//              break;
//          }
//      }
//
//      @Override
//      public void onProviderEnabled(String provider) {
//    	  Log.d("GPS_service", "GPS Provider Enabled\n");
//      }
//
//      @Override
//      public void onProviderDisabled(String provider) {
//    	  Log.d("GPS_service", "GPS Provider Disabled\n");
//      }
//
//      @Override
//      public void onLocationChanged(Location location) {
//          locationManager.removeUpdates(networkLocationListener);
//          current_location = "New GPS location: "
//                  + String.format("%9.6f", location.getLatitude()) + ", "
//                  + String.format("%9.6f", location.getLongitude()) + "\n";
//          Log.d("GPS_service", current_location);
//      }
//      
//
//      
//  };
  private final LocationListener networkLocationListener = new LocationListener(){

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras){
          switch (status) {
          case LocationProvider.AVAILABLE:
        	  Log.d("GPS_service", "Network location available again\n");
              break;
          case LocationProvider.OUT_OF_SERVICE:
        	  Log.d("GPS_service", "Network location out of service\n");
              break;
          case LocationProvider.TEMPORARILY_UNAVAILABLE:
        	  Log.d("GPS_service", "Network location temporarily unavailable\n");
              break;
          }
      }

      @Override
      public void onProviderEnabled(String provider) {
    	  Log.d("GPS_service", "Network Provider Enabled\n");
      }

      @Override
      public void onProviderDisabled(String provider) {
    	  Log.d("GPS_service", "Network Provider Disabled\n");
      }

      @Override
      public void onLocationChanged(Location location) {
    	  Log.d("GPS_service", "New network location: "
                  + String.format("%9.6f", location.getLatitude()) + ", "
                  + String.format("%9.6f", location.getLongitude()) + "\n");
      }
  };

	public GPSActivity() {
        Log.d("GPS_service", "gps constructor");
        current_location = "through gps constructor";
	}
	
	public MyLocationListener getLocationListener() {
		return this.gpsLocationListener;
	}

	@Override
	public void onCreate(Bundle icicle) {
        Log.d("GPS_service", "Setting up gps ***********************");
	    super.onCreate(icicle);
	    //setContentView(R.layout.main);
	    current_location = "creating gps";

	    locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        Log.d("GPS_service", "got mgr");
        
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d("GPS_service", "best provider: " + bestProvider);        
        
        locationManager.requestLocationUpdates(bestProvider, 1000, 1, gpsLocationListener);
        locationManager.requestLocationUpdates(bestProvider, 1000, 1, networkLocationListener);
        
        locationManager.requestSingleUpdate(bestProvider, onLocationChange, null);
	
	}
	
    @Override
    protected void onResume() {
        Log.d("GPS_service", "resuming ********");
        super.onResume();
       
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, gpsLocationListener);
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, networkLocationListener);

        Log.d("GPS_service", "resumed ********");
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(networkLocationListener);
        locationManager.removeUpdates(gpsLocationListener);
    }

  LocationListener onLocationChange=new LocationListener() {
      public void onLocationChanged(Location loc) {
        String locationString=loc.getLatitude() + "," + loc.getLongitude();
        Log.d("GPS_service", locationString);
        current_location = locationString;
        
        try {
            TextView locDisplay = (TextView) findViewById(R.id.locationTextBox);
	        locDisplay.setText(current_location);
        } catch (Exception e) {
        	Log.d("output", "could not find output");
        }
        
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