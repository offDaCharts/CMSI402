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

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class StopwatchService extends Service {

    private static final String TAG = "StopwatchService";
    private static final String LIVE_CARD_TAG = "stopwatch";

    private ChronometerDrawer mCallback;

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
        
        Log.d(TAG, "******************************************************");
        Log.d(TAG, "Creating intent and start activity");
        Intent intent = new Intent(getBaseContext(), GPSActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
        Log.d(TAG, "Created");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);

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
    
    public void postData(double time, double distance) {
    	//expects time in seconds and distance in miles
    	
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://10.22.164.215:5656/saveride/" + time + "/" + distance);

        try {
            // Add your data
            //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "posting success");

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "posting failed");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "posting fail");
        }
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
            postData((SpeedCalc.lastMillis - SpeedCalc.startMillis)/1000.0, SpeedCalc.distanceTraveled * 0.000621371);
        }
        super.onDestroy();
    }
}
