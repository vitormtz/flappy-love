package com.example.flappylove.device;

public class NoOpController implements DeviceController {
    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void vibrate(int intensity, int durationMs) {
    }

    @Override
    public void stopVibration() {
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void release() {
    }
}
