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

import org.apache.http.util.EncodingUtils;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
//Deprecated from XE14
//import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class StopwatchService extends Service {

    private static final String TAG = "StopwatchService";
    private static final String LIVE_CARD_TAG = "stopwatch";
    public static final String username = "quin";

    private ChronometerDrawer mCallback;

    //private TimelineManager mTimelineManager;
    public LiveCard mLiveCard;
    public Intent gpsIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        //mTimelineManager = TimelineManager.from(this);
        
        Log.d(TAG, "******************************************************");
        Log.d(TAG, "Creating intent and start activity");
        gpsIntent = new Intent(getBaseContext(), GPSActivity.class);
        gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(gpsIntent);
        Log.d(TAG, "Created");

    }
   
// To make work with both 14XE and 16XE
//    public static LiveCard getLiveCard(Context context, String tag) {
//	  try {
//	    Class<?> tmClass = Class.forName("com.google.android.glass.timeline.TimelineManager");
//	    Method from = tmClass.getMethod("from", new Class[] { Context.class });
//	    Object tm = from.invoke(tmClass, context);
//	    Method createLiveCard = tmClass.getMethod("createLiveCard", new Class[] { String.class });
//	    return (LiveCard) createLiveCard.invoke(tm, tag);
//	  } catch (Exception e) {
//	    // TimelineManager must be gone
//	    return new LiveCard();
//	  }
//	}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            //mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
            
            //mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
            try {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            } catch (Exception e) {}
            
            // Keep track of the callback to remove it before unpublishing.
            mCallback = new ChronometerDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(PublishMode.REVEAL);
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }
        
        return START_STICKY;
    }
    
    public void postData(double time, double distance, double maxSpeed) {
    	//expects time in seconds and distance in miles, max speed in mph
    	
        String webSiteAddress = "http://10.0.1.152:5656/";
        String url = webSiteAddress + "saveride/" + time + "/" + distance + "/" + maxSpeed + "/" + StopwatchService.username;
       
        WebView webview = new WebView(this);
        byte[] post = EncodingUtils.getBytes("", "BASE64");
        webview.postUrl(url, post);
        Log.d(TAG, "posting success");
    } 

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            if (mCallback != null) {
                mLiveCard.getSurfaceHolder().removeCallback(mCallback);
            }
            mLiveCard.unpublish();
            mLiveCard = null;
            Log.d(TAG, "Ride Data");
            Log.d(TAG, "Time: " + (SpeedCalc.lastMillis - SpeedCalc.startMillis));
            Log.d(TAG, "Distance: " + SpeedCalc.distanceTraveled);
            Log.d(TAG, "posting ride");
            
            postData((SpeedCalc.lastMillis - SpeedCalc.startMillis)/1000.0, SpeedCalc.distanceTraveled * 0.000621371, SpeedCalc.maxSpeed * 2.23694);
        }
        getApplication().stopService(gpsIntent);
        super.onDestroy();
    }
}
