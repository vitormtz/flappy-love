package com.example.flappylove;

import android.app.Application;
import android.util.Log;

import com.example.flappylove.device.DeviceController;
import com.example.flappylove.device.LovenseController;
import com.example.flappylove.device.NoOpController;
import com.example.flappylove.util.ScoreManager;

public class FlappyLoveApplication extends Application {
    private static final String TAG = "FlappyLoveApp";
    private DeviceController deviceController;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public DeviceController getDeviceController() {
        if (deviceController == null) {
            ScoreManager scoreManager = new ScoreManager(this);
            if (scoreManager.isLovenseEnabled()) {
                deviceController = new LovenseController(this);
            } else {
                deviceController = new NoOpController();
            }
        }
        return deviceController;
    }

    public void setDeviceController(DeviceController controller) {
        if (deviceController != null && deviceController != controller) {
            deviceController.stopVibration();
        }
        this.deviceController = controller;
    }

    public void releaseDeviceController() {
        if (deviceController != null) {
            deviceController.release();
            deviceController = null;
        }
    }
}
