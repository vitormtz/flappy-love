package com.example.flappylove;

import android.app.Application;
import android.util.Log;

public class FlappyLoveApplication extends Application {
    private static final String TAG = "FlappyLoveApp";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLovenseSDK();
    }

    private void initializeLovenseSDK() {
        // TODO: Uncomment when Lovense SDK .aar is properly loaded
        // try {
        //     Lovense.getInstance(this).setDeveloperToken(BuildConfig.LOVENSE_DEVELOPER_TOKEN);
        //     Lovense.getInstance(this).setLogEnable(true);
        //     Log.d(TAG, "Lovense SDK initialized successfully!");
        // } catch (Exception e) {
        //     Log.e(TAG, "Error initializing Lovense SDK", e);
        // }
        Log.w(TAG, "Lovense SDK initialization skipped - .aar not recognized by Gradle");
    }
}
