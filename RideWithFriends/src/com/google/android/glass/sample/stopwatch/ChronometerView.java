/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.stopwatch;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.apache.http.util.EncodingUtils;


/**
 * View used to display draw a running Chronometer.
 *
 * This code is greatly inspired by the Android's Chronometer widget.
 */
public class ChronometerView extends FrameLayout {

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface ChangeListener {
        /** Notified of a change in the view. */
        public void onChange();
    }
    
    // About 24 FPS.
    private static final long DELAY_MILLIS = 41;
    
    private final TextView mMinuteView;
    private final TextView mSecondView;
    private final TextView mCentiSecondView;
    private final TextView locDisplay;
    
    private boolean mStarted;
    private boolean mForceStart;
    private boolean mVisible;
    private boolean mRunning;
    public static long lastMillis = 0;
    public static long lastLocUpdate = 0;
    public Context thisContext;

    private long mBaseMillis;

    private ChangeListener mChangeListener;
    
    private GPSActivity gps = new GPSActivity();

    public ChronometerView(Context context) {
        this(context, null, 0);
    }

    public ChronometerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public void setLocation(String loc) {
        Log.d("output", "outputting location: " + loc + " ************");
    	locDisplay.setText(loc);
    }

    public ChronometerView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_chronometer, this);

    	thisContext = context;
        
        mMinuteView = (TextView) findViewById(R.id.minute);
        mSecondView = (TextView) findViewById(R.id.second);
        mCentiSecondView = (TextView) findViewById(R.id.centi_second);
        
    	Log.d("chrono", "location text box: " + R.id.locationTextBox);
        locDisplay = (TextView) findViewById(R.id.locationTextBox);

        setBaseMillis(SystemClock.elapsedRealtime());
    }

    /**
     * Set the base value of the chronometer in milliseconds.
     */
    public void setBaseMillis(long baseMillis) {
        mBaseMillis = baseMillis;
        updateText();
    }

    /**
     * Get the base value of the chronometer in milliseconds.
     */
    public long getBaseMillis() {
        return mBaseMillis;
    }

    /**
     * Set a {@link ChangeListener}.
     */
    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    /**
     * Set whether or not to force the start of the chronometer when a window has not been attached
     * to the view.
     */
    public void setForceStart(boolean forceStart) {
        mForceStart = forceStart;
        updateRunning();
    }

    /**
     * Start the chronometer.
     */
    public void start() {
    	//gps
    	//gps.setup();
        //gps.getLocationListener().checkLocation();
    
    	mStarted = true;
        updateRunning();
    }

    /**
     * Stop the chronometer.
     */
    public void stop() {
        mStarted = false;
        gps.finish();
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = (visibility == VISIBLE);
        updateRunning();
    }


    private final Handler mHandler = new Handler();

    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                updateText();
                mHandler.postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    /**
     * Update the running state of the chronometer.
     */
    private void updateRunning() {
        boolean running = (mVisible || mForceStart) && mStarted;
        if (running != mRunning) {
            if (running) {
                mHandler.post(mUpdateTextRunnable);
            } else {
                mHandler.removeCallbacks(mUpdateTextRunnable);
            }
            mRunning = running;
        }
    }

    /**
     * Update the value of the chronometer.
     */
    private void updateText() {
        long millis = SystemClock.elapsedRealtime() - mBaseMillis;
        long currentSecond;
        long currentMinute;
        long currentMillis = millis;
        // Cap chronometer to one hour.
        
        if(lastLocUpdate == 0) {
            Log.d("chrono view", "init last loc update");            
        	lastLocUpdate = currentMillis;
        }
        
        millis %= TimeUnit.HOURS.toMillis(1);

        currentMinute = TimeUnit.MILLISECONDS.toMinutes(millis);
        mMinuteView.setText(String.format("%02d", currentMinute));
        millis %= TimeUnit.MINUTES.toMillis(1);
        
        currentSecond = TimeUnit.MILLISECONDS.toSeconds(millis);
        mSecondView.setText(String.format("%02d", currentSecond));
        
        millis = (millis % TimeUnit.SECONDS.toMillis(1)) / 10;
        mCentiSecondView.setText(String.format("%02d", millis));
        
        if(lastMillis + 3000 <= currentMillis) {
        	Log.d("time", "last: " + lastMillis);
        	Log.d("time", "current: " + currentMillis);
        	if(GPSActivity.current_location != null) {
        		SpeedCalc.tick(currentMillis, GPSActivity.current_location);
        	}
        	//three seconds has passed, update gps
        	lastMillis = currentMillis;
            gps.updateLoc();
        }
        
        //Every 10 seconds update location in mongo database
        if(GPSActivity.current_location != null && currentMillis > (10000 + lastLocUpdate)) {
        	lastLocUpdate = currentMillis;
            Log.d("chrono view", "location string: " + GPSActivity.current_location.toString());            
            String webSiteAddress = "http://10.0.1.152:5656/";
            //String url = webSiteAddress + "updatelocation/" + GPSActivity.current_location.toString() + "/quin";
            String url = webSiteAddress + "updatelocation/" + GPSActivity.current_location.getLatitude() + "," + GPSActivity.current_location.getLongitude() + "/quin";
            Log.d("chrono view", "location string: " + GPSActivity.current_location.toString());            
           
            WebView webview = new WebView(thisContext);
            byte[] post = EncodingUtils.getBytes("", "BASE64");
            webview.postUrl(url, post);
            Log.d("chrono view", "location update success");
        }
                
        if(GPSActivity.current_location != null) {
        	DecimalFormat df = new DecimalFormat("0.00");
            locDisplay.setText("Speed: " + df.format(SpeedCalc.metersPerMillisToMph(SpeedCalc.instantSpeed))
            		//+ " Avg: " + df.format(SpeedCalc.metersPerMillisToMph(SpeedCalc.avgSpeed)));
            		+ " Dist: " + df.format(SpeedCalc.distanceTraveled * 0.000621371));
        } else {
            locDisplay.setText("Setting up gps");
        }
        
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }
       
}
