package com.example.flappylove.ui;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flappylove.FlappyLoveApplication;
import com.example.flappylove.device.DeviceController;
import com.example.flappylove.game.GameView;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private DeviceController deviceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gameView = new GameView(this);
        setContentView(gameView);

        FlappyLoveApplication app = (FlappyLoveApplication) getApplication();
        deviceController = app.getDeviceController();

        gameView.setDeviceController(deviceController);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
        if (deviceController != null) {
            deviceController.stopVibration();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
