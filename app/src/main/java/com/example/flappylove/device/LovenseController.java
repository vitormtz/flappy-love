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
import com.lovense.sdklibrary.callBack.OnCallBackBatteryV2Listener;
import com.component.toymodule.command.control.bean.CommandType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LovenseController implements DeviceController {
    private static final String TAG = "LovenseController";
    private final Context context;
    private boolean isSearching = false;
    private long lastVibrateTime = 0;
    private int lastSentLevel = -1;
    private static final long VIBRATE_THROTTLE_MS = 100;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Map<String, LovenseToy> connectedToys = new LinkedHashMap<>();
    private final Map<String, ToyInfo> toyInfoMap = new LinkedHashMap<>();
    private final List<LovenseToy> foundToys = new ArrayList<>();

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

        Application app = (Application) context.getApplicationContext();

        try {
            Lovense.getInstance(app).searchToys(new OnSearchToyListener() {
                @Override
                public void onSearchToy(LovenseToy lovenseToy) {
                    Log.d(TAG, "Found toy: " + lovenseToy.toString());
                    foundToys.add(lovenseToy);
                    connectToToy(lovenseToy);
                }

                @Override
                public void finishSearch() {
                    Log.d(TAG, "Search finished. Found " + foundToys.size() + " toy(s)");
                    isSearching = false;
                }

                @Override
                public void onError(LovenseError error) {
                    Log.e(TAG, "Search error: " + error.toString());
                    isSearching = false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception starting search: " + e.getMessage(), e);
            isSearching = false;
        }
    }

    private void connectToToy(LovenseToy toy) {
        String id;
        String name;
        try {
            id = (String) toy.getClass().getMethod("getToyId").invoke(toy);
            name = toy.getName();
            Log.d(TAG, "Attempting to connect to toy: " + name + " (ID: " + id + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error getting toy info: " + e.getMessage(), e);
            return;
        }

        if (id == null || id.isEmpty()) {
            Log.e(TAG, "Toy ID is null or empty, cannot connect");
            return;
        }

        if (connectedToys.containsKey(id)) {
            Log.d(TAG, "Toy already connected: " + id);
            return;
        }

        Application app = (Application) context.getApplicationContext();
        Lovense.getInstance(app).connectToy(id, new OnConnectListener() {
            @Override
            public void onConnect(String connId, String status) {
                Log.d(TAG, "Connection status for " + connId + ": " + status);

                if ("STATE_CONNECTED".equals(status) || "SERVICE_DISCOVERED".equals(status)) {
                    connectedToys.put(connId, toy);
                    toyInfoMap.put(connId, new ToyInfo(connId, name));
                    Log.i(TAG, "Successfully connected to toy: " + name);
                    requestBattery(connId);
                } else if ("STATE_FAILED".equals(status)) {
                    Log.e(TAG, "Failed to connect to toy: " + connId);
                }
            }

            @Override
            public void onError(LovenseError error) {
                Log.e(TAG, "Error connecting to toy: " + error.toString());
            }
        });
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "Disconnecting all toys");

        try {
            Application app = (Application) context.getApplicationContext();
            Lovense.getInstance(app).stopSearching();
        } catch (Exception e) {
            Log.e(TAG, "Error stopping search: " + e.getMessage());
        }
        isSearching = false;

        stopVibration();
        connectedToys.clear();
        toyInfoMap.clear();
        foundToys.clear();
        lastSentLevel = -1;
    }

    @Override
    public void disconnectToy(String toyId) {
        Log.d(TAG, "Disconnecting toy: " + toyId);

        try {
            Application app = (Application) context.getApplicationContext();
            Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, 0);
            Lovense.getInstance(app).disconnect(toyId);
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting toy: " + e.getMessage());
        }

        connectedToys.remove(toyId);
        toyInfoMap.remove(toyId);

        if (connectedToys.isEmpty()) {
            lastSentLevel = -1;
        }
    }

    @Override
    public void vibrate(int intensity, int durationMs) {
        if (connectedToys.isEmpty()) return;

        int level = Math.min(Math.max(intensity, 0), 20);

        if (level == lastSentLevel) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVibrateTime < VIBRATE_THROTTLE_MS) return;
        lastVibrateTime = currentTime;
        lastSentLevel = level;

        try {
            Application app = (Application) context.getApplicationContext();
            for (String id : connectedToys.keySet()) {
                Lovense.getInstance(app).sendCommand(id, CommandType.VIBRATE, level);
            }

            if (durationMs > 0) {
                mainHandler.postDelayed(() -> {
                    if (!connectedToys.isEmpty()) {
                        lastSentLevel = 0;
                        for (String id : connectedToys.keySet()) {
                            Lovense.getInstance(app).sendCommand(id, CommandType.VIBRATE, 0);
                        }
                    }
                }, durationMs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in vibrate: " + e.getMessage());
        }
    }

    @Override
    public void vibrateToy(String toyId, int intensity, int durationMs) {
        if (!connectedToys.containsKey(toyId)) return;

        int level = Math.min(Math.max(intensity, 0), 20);

        try {
            Application app = (Application) context.getApplicationContext();
            Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, level);

            if (durationMs > 0) {
                mainHandler.postDelayed(() -> {
                    if (connectedToys.containsKey(toyId)) {
                        Lovense.getInstance(app).sendCommand(toyId, CommandType.VIBRATE, 0);
                    }
                }, durationMs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in vibrateToy: " + e.getMessage());
        }
    }

    @Override
    public void stopVibration() {
        if (connectedToys.isEmpty()) return;
        if (lastSentLevel == 0) return;

        lastSentLevel = 0;

        try {
            Application app = (Application) context.getApplicationContext();
            for (String id : connectedToys.keySet()) {
                Lovense.getInstance(app).sendCommand(id, CommandType.VIBRATE, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in stopVibration: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return !connectedToys.isEmpty();
    }

    @Override
    public boolean isSearching() {
        return isSearching;
    }

    @Override
    public List<ToyInfo> getConnectedToys() {
        return new ArrayList<>(toyInfoMap.values());
    }

    private void requestBattery(String id) {
        try {
            Application app = (Application) context.getApplicationContext();
            Lovense.getInstance(app).addListener(id, new OnCallBackBatteryV2Listener() {
                @Override
                public void battery(String battId, int battery, boolean isWork) {
                    Log.d(TAG, "Battery for " + battId + ": " + battery + "%");
                    ToyInfo info = toyInfoMap.get(battId);
                    if (info != null) {
                        info.battery = battery;
                    }
                }
            });
            Lovense.getInstance(app).sendCommand(id, CommandType.GET_BATTERY);
        } catch (Exception e) {
            Log.e(TAG, "Error requesting battery: " + e.getMessage());
        }
    }

    @Override
    public void release() {
        disconnect();
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
