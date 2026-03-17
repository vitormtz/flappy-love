package com.example.flappylove.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.flappylove.FlappyLoveApplication;
import com.example.flappylove.R;
import com.example.flappylove.device.DeviceController;
import com.example.flappylove.device.LovenseController;
import com.example.flappylove.device.NoOpController;
import com.example.flappylove.util.ScoreManager;
import com.lovense.sdklibrary.Lovense;

public class SettingsActivity extends AppCompatActivity {
    private static final int BLUETOOTH_PERMISSION_REQUEST = 100;
    private ScoreManager scoreManager;
    private Switch lovenseSwitch;
    private TextView statusText;
    private Button connectButton;
    private Button disconnectButton;
    private Button testButton;
    private Handler statusHandler = new Handler(Looper.getMainLooper());

    private FlappyLoveApplication app;

    private final ActivityResultLauncher<Intent> bluetoothEnableLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                enableLovenseMode();
            } else {
                lovenseSwitch.setChecked(false);
                Toast.makeText(this, "Bluetooth is required for Lovense Mode", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();
        setContentView(R.layout.activity_settings);

        app = (FlappyLoveApplication) getApplication();
        scoreManager = new ScoreManager(this);

        lovenseSwitch = findViewById(R.id.switchLovense);
        TextView infoText = findViewById(R.id.tvLovenseInfo);
        statusText = findViewById(R.id.tvConnectionStatus);
        connectButton = findViewById(R.id.btnConnect);
        disconnectButton = findViewById(R.id.btnDisconnect);
        testButton = findViewById(R.id.btnTestVibration);

        infoText.setText("Enable Lovense Mode to connect to a Lovense Lush 3 device. " +
                        "The device will respond to game events with haptic feedback.\n\n" +
                        "The vibration intensity changes based on the bird's position relative to the pipe gap.");

        lovenseSwitch.setChecked(scoreManager.isLovenseEnabled());

        lovenseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleLovenseEnable();
            } else {
                scoreManager.setLovenseEnabled(false);
                app.releaseDeviceController();
                updateConnectionStatus();
                Toast.makeText(this, "Lovense Mode disabled", Toast.LENGTH_SHORT).show();
            }
        });

        connectButton.setOnClickListener(v -> {
            if (!scoreManager.isLovenseEnabled()) {
                Toast.makeText(this, "Please enable Lovense Mode first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkBluetoothPermissions()) {
                connectToDevice();
            } else {
                requestBluetoothPermissions();
            }
        });

        disconnectButton.setOnClickListener(v -> {
            DeviceController controller = app.getDeviceController();
            if (controller != null) {
                controller.disconnect();
                Toast.makeText(this, "Disconnecting...", Toast.LENGTH_SHORT).show();
                statusHandler.postDelayed(this::updateConnectionStatus, 1000);
            }
        });

        testButton.setOnClickListener(v -> {
            DeviceController controller = app.getDeviceController();
            if (controller != null && controller.isConnected()) {
                Toast.makeText(this, "Testing vibration...", Toast.LENGTH_SHORT).show();
                controller.vibrate(15, 500);
            } else {
                Toast.makeText(this, "Please connect to device first", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        Switch ghostSwitch = findViewById(R.id.switchGhostMode);
        Switch godModeSwitch = findViewById(R.id.switchGodMode);
        Switch slowMotionSwitch = findViewById(R.id.switchSlowMotion);
        Switch speedModeSwitch = findViewById(R.id.switchSpeedMode);
        Switch holdModeSwitch = findViewById(R.id.switchHoldMode);

        ghostSwitch.setChecked(scoreManager.isGhostMode());
        godModeSwitch.setChecked(scoreManager.isGodMode());
        slowMotionSwitch.setChecked(scoreManager.isSlowMotion());
        speedModeSwitch.setChecked(scoreManager.isSpeedMode());
        holdModeSwitch.setChecked(scoreManager.isHoldMode());

        ghostSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            scoreManager.setGhostMode(isChecked));

        godModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            scoreManager.setGodMode(isChecked));

        slowMotionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scoreManager.setSlowMotion(isChecked);
            if (isChecked && speedModeSwitch.isChecked()) {
                speedModeSwitch.setChecked(false);
            }
        });

        speedModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scoreManager.setSpeedMode(isChecked);
            if (isChecked && slowMotionSwitch.isChecked()) {
                slowMotionSwitch.setChecked(false);
            }
        });

        holdModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            scoreManager.setHoldMode(isChecked));

        updateConnectionStatus();
        startStatusUpdateTimer();
    }

    private void handleLovenseEnable() {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        if (!isBluetoothEnabled()) {
            promptEnableBluetooth();
            return;
        }

        enableLovenseMode();
    }

    private void enableLovenseMode() {
        scoreManager.setLovenseEnabled(true);
        connectButton.setEnabled(false);
        connectToDevice();
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void promptEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothEnableLauncher.launch(enableBtIntent);
    }

    private void connectToDevice() {
        Lovense.getInstance(getApplication()).setDeveloperToken(com.example.flappylove.BuildConfig.LOVENSE_DEVELOPER_TOKEN);
        Lovense.getInstance(getApplication()).setLogEnable(true);

        DeviceController controller = app.getDeviceController();
        if (controller instanceof NoOpController) {
            controller = new LovenseController(this);
            app.setDeviceController(controller);
        }
        Toast.makeText(this, "Searching for devices...", Toast.LENGTH_SHORT).show();
        controller.connect();
        statusHandler.postDelayed(this::updateConnectionStatus, 2000);
    }

    private void updateConnectionStatus() {
        DeviceController controller = app.getDeviceController();
        if (controller != null && controller.isConnected()) {
            statusText.setText("Status: Connected");
            statusText.setTextColor(0xFF4CAF50);
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            testButton.setEnabled(true);
        } else if (controller != null && controller.isSearching()) {
            statusText.setText("Status: Searching...");
            statusText.setTextColor(0xFFFF9800);
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(false);
            testButton.setEnabled(false);
        } else {
            statusText.setText("Status: Disconnected");
            statusText.setTextColor(0xFFF44336);
            connectButton.setEnabled(scoreManager.isLovenseEnabled());
            disconnectButton.setEnabled(false);
            testButton.setEnabled(false);
        }
    }

    private void startStatusUpdateTimer() {
        statusHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateConnectionStatus();
                statusHandler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                BLUETOOTH_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isBluetoothEnabled()) {
                    promptEnableBluetooth();
                } else {
                    enableLovenseMode();
                }
            } else {
                lovenseSwitch.setChecked(false);
                Toast.makeText(this, "Bluetooth permissions required for Lovense Mode", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusHandler.removeCallbacksAndMessages(null);
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
