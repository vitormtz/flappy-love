package com.example.flappylove.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.flappylove.FlappyLoveApplication;
import com.example.flappylove.device.DeviceController;
import com.example.flappylove.game.GameView;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private DeviceController deviceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();

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
        hideSystemBars();
        gameView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
