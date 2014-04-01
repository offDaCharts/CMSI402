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
import android.view.LayoutInflater;
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

  public static LocationManager locationManager;
  public static LocationListener onLocationChange=new LocationListener() {
      public void onLocationChanged(Location loc) {
          GPSActivity.current_location = loc;        
          Log.d("GPS_service", loc.toString());
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
  private String bestProvider;  
  public static Location current_location = null;
  private final LocationListener gpsLocationListener = new LocationListener(){

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
          GPSActivity.locationManager.removeUpdates(networkLocationListener);
          GPSActivity.current_location = location;   
          Log.d("GPS_service", location.toString());
      }
      
  };
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
    	  GPSActivity.current_location = location;
    	  Log.d("GPS_service", "New network location: "
                  + String.format("%9.6f", location.getLatitude()) + ", "
                  + String.format("%9.6f", location.getLongitude()) + "\n");
      }
  };

	public GPSActivity() {
        Log.d("GPS_service", "gps constructor");
	}


	@Override
	public void onCreate(Bundle icicle) {
        Log.d("GPS_service", "Setting up gps ***********************");
	    super.onCreate(icicle);
	    //setContentView(R.layout.main);

	    GPSActivity.locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        Log.d("GPS_service", "got mgr");
        
        Criteria criteria = new Criteria();
        bestProvider = GPSActivity.locationManager.getBestProvider(criteria, true);
        Log.d("GPS_service", "best provider: " + bestProvider);        
        
        GPSActivity.locationManager.requestLocationUpdates(bestProvider, 1000, 1, gpsLocationListener);
        GPSActivity.locationManager.requestLocationUpdates(bestProvider, 1000, 1, networkLocationListener);
        GPSActivity.locationManager.requestSingleUpdate(bestProvider, GPSActivity.onLocationChange, null);
	}
	
	public void updateLoc() {
		try {
	        Criteria criteria = new Criteria();
	        bestProvider = GPSActivity.locationManager.getBestProvider(criteria, true);
			GPSActivity.locationManager.requestSingleUpdate(bestProvider, GPSActivity.onLocationChange, null);
		} catch (Exception e) {
			
		}
	}
		
    @Override
    protected void onResume() {
        Log.d("GPS_service", "resuming ********");
        super.onResume();
       
        Criteria criteria = new Criteria();
        bestProvider = GPSActivity.locationManager.getBestProvider(criteria, true);
        GPSActivity.locationManager.requestLocationUpdates(bestProvider, 1000, 0, gpsLocationListener);
        GPSActivity.locationManager.requestLocationUpdates(bestProvider, 1000, 0, networkLocationListener);

        Log.d("GPS_service", "resumed ********");
    }

    @Override
    protected void onPause() {
        super.onPause();
        GPSActivity.locationManager.removeUpdates(networkLocationListener);
        GPSActivity.locationManager.removeUpdates(gpsLocationListener);
    }

  
}