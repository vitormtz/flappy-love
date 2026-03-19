package com.example.flappylove.device;

import java.util.List;

public interface DeviceController {
    void connect();
    void disconnect();
    void disconnectToy(String toyId);
    void vibrate(int intensity, int durationMs);
    void vibrateToy(String toyId, int intensity, int durationMs);
    void stopVibration();
    boolean isConnected();
    boolean isSearching();
    List<ToyInfo> getConnectedToys();
    void release();
}
