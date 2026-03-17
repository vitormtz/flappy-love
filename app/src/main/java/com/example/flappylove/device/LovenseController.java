package com.example.flappylove.device;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lovense.sdklibrary.Lovense;
import com.lovense.sdklibrary.LovenseToy;
import com.lovense.sdklibrary.callBack.OnSearchToyListener;
import com.lovense.sdklibrary.callBack.OnConnectListener;
import com.lovense.sdklibrary.callBack.LovenseError;
import com.lovense.sdklibrary.callBack.OnErrorListener;
import com.component.toymodule.command.control.bean.CommandType;

import java.util.ArrayList;
import java.util.List;

public class LovenseController implements DeviceController {
    private static final String TAG = "LovenseController";
    private final Context context;
    private LovenseToy connectedToy = null;
    private String toyId = null;
    private boolean isConnected = false;
    private boolean isSearching = false;
    private long lastVibrateTime = 0;
    private int lastSentLevel = -1;
    private static final long VIBRATE_THROTTLE_MS = 100;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<LovenseToy> foundToys = new ArrayList<>();

    public LovenseController(Context context) {
        this.context = context;
    }

    @Override
    public void connect() {
        if (isSearching) {
            Log.d(TAG, "Search already in progress");
            return;
        }

        isSearching = true;
        foundToys.clear();

        Log.d(TAG, "Starting search for Lovense toys...");
        Log.d(TAG, "Bluetooth enabled: " + isBluetoothEnabled());

        Application app = (Application) context.getApplicationContext();

        try {
            Lovense.getInstance(app).searchToys(new OnSearchToyListener() {
            @Override
            public void onSearchToy(LovenseToy lovenseToy) {
                Log.d(TAG, "Found toy: " + lovenseToy.toString());
                foundToys.add(lovenseToy);

                Log.d(TAG, "connectedToy is null: " + (connectedToy == null));
                if (connectedToy == null) {
                    Log.d(TAG, "Calling connectToToy from onSearchToy");
                    connectToToy(lovenseToy);
                } else {
                    Log.d(TAG, "NOT calling connectToToy - already have connectedToy");
                }
            }

            @Override
            public void finishSearch() {
                Log.d(TAG, "Search finished. Found " + foundToys.size() + " toy(s)");
                isSearching = false;

                Log.d(TAG, "In finishSearch - connectedToy is null: " + (connectedToy == null) + ", foundToys.isEmpty: " + foundToys.isEmpty());
                if (connectedToy == null && !foundToys.isEmpty()) {
                    Log.d(TAG, "Calling connectToToy from finishSearch");
                    connectToToy(foundToys.get(0));
                } else {
                    Log.d(TAG, "NOT calling connectToToy from finishSearch");
                }
            }

            @Override
            public void onError(LovenseError error) {
                String errorMsg = "Unknown error";
                try {
                    errorMsg = (String) error.getClass().getMethod("getMsg").invoke(error);
                } catch (Exception e1) {
                    try {
                        errorMsg = (String) error.getClass().getMethod("getMessage").invoke(error);
                    } catch (Exception e2) {
                        try {
                            errorMsg = (String) error.getClass().getMethod("getCode").invoke(error);
                        } catch (Exception e3) {
                            try {
                                errorMsg = (String) error.getClass().getField("msg").get(error);
                            } catch (Exception e4) {
                                errorMsg = error.toString();
                            }
                        }
                    }
                }
                Log.e(TAG, "Search error: " + errorMsg);
                Log.e(TAG, "Error object: " + error.toString());
                isSearching = false;
                isConnected = false;
            }
        });
        } catch (Exception e) {
            Log.e(TAG, "Exception starting search: " + e.getMessage(), e);
            isSearching = false;
            isConnected = false;
        }
    }

    private void connectToToy(LovenseToy toy) {
        Log.d(TAG, "Attempting to connect to toy: " + toy.getName());
        connectedToy = toy;

        try {
            toyId = (String) toy.getClass().getMethod("getToyId").invoke(toy);
            Log.d(TAG, "Toy ID: " + toyId);
            Log.d(TAG, "Toy Name: " + toy.getClass().getMethod("getName").invoke(toy));
            Log.d(TAG, "Device Name: " + toy.getClass().getMethod("getDeviceName").invoke(toy));
            Log.d(TAG, "Toy Type: " + toy.getClass().getMethod("getType").invoke(toy));
        } catch (Exception e) {
            Log.e(TAG, "Error getting toy ID: " + e.getMessage(), e);
            toyId = null;
            isConnected = false;
            connectedToy = null;
            return;
        }

        if (toyId == null || toyId.isEmpty()) {
            Log.e(TAG, "Toy ID is null or empty, cannot connect");
            isConnected = false;
            connectedToy = null;
            return;
        }

        Log.d(TAG, "Initiating connection to toy ID: " + toyId);

        Application app = (Application) context.getApplicationContext();
        Lovense.getInstance(app).connectToy(toyId, new OnConnectListener() {
            @Override
            public void onConnect(String id, String status) {
                Log.d(TAG, "Connection status for " + id + ": " + status);

                if ("STATE_CONNECTED".equals(status)) {
                    isConnected = true;
                    Log.i(TAG, "Successfully connected to toy!");
                } else if ("SERVICE_DISCOVERED".equals(status)) {
                    isConnected = true;
                    Log.i(TAG, "Toy services discovered, ready to use!");
                } else if ("STATE_FAILED".equals(status)) {
                    isConnected = false;
                    Log.e(TAG, "Failed to connect to toy");
                } else if ("STATE_CONNECTING".equals(status)) {
                    Log.d(TAG, "Connecting to toy...");
                }
            }

            @Override
            public void onError(LovenseError error) {
                Log.e(TAG, "Error connecting to toy: " + error.toString());
                isConnected = false;
                connectedToy = null;
                toyId = null;
            }
        });
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "Disconnecting from toy");

        try {
            Application app = (Application) context.getApplicationContext();
            Lovense.getInstance(app).stopSearching();
        } catch (Exception e) {
            Log.e(TAG, "Error stopping search: " + e.getMessage());
        }
        isSearching = false;

        if (connectedToy != null) {
            stopVibration();
            connectedToy = null;
            toyId = null;
        }

        isConnected = false;
        foundToys.clear();
    }

    @Override
    public void vibrate(int intensity, int durationMs) {
        if (!isConnected || toyId == null) {
            Log.w(TAG, "Cannot vibrate - not connected");
            return;
        }

        int level = Math.min(Math.max(intensity, 0), 20);

        if (level == lastSentLevel) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVibrateTime < VIBRATE_THROTTLE_MS) {
            return;
        }
        lastVibrateTime = currentTime;
        lastSentLevel = level;

        try {
            Application app = (Application) context.getApplicationContext();
            Log.d(TAG, "Sending vibrate command with level: " + level);

            Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, level);

            if (durationMs > 0) {
                mainHandler.postDelayed(() -> {
                    if (isConnected && toyId != null) {
                        lastSentLevel = 0;
                        Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, 0);
                    }
                }, durationMs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in vibrate method: " + e.getMessage());
        }
    }

    @Override
    public void stopVibration() {
        if (!isConnected || toyId == null) {
            return;
        }

        if (lastSentLevel == 0) {
            return;
        }

        lastSentLevel = 0;

        try {
            Application app = (Application) context.getApplicationContext();
            Log.d(TAG, "Stopping vibration");

            Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error in stopVibration method: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && connectedToy != null;
    }

    @Override
    public boolean isSearching() {
        return isSearching;
    }

    @Override
    public void release() {
        disconnect();
        isSearching = false;
        foundToys.clear();
    }

    private boolean isBluetoothEnabled() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            return adapter != null && adapter.isEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Error checking Bluetooth: " + e.getMessage());
            return false;
        }
    }

}
