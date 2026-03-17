package com.example.flappylove.device;

public interface DeviceController {
    void connect();
    void disconnect();
    void vibrate(int intensity, int durationMs);
    void stopVibration();
    boolean isConnected();
    boolean isSearching();
    void release();
}
