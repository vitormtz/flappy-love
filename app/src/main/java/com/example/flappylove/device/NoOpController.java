package com.example.flappylove.device;

import java.util.ArrayList;
import java.util.List;

public class NoOpController implements DeviceController {
    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void disconnectToy(String toyId) {
    }

    @Override
    public void vibrate(int intensity, int durationMs) {
    }

    @Override
    public void vibrateToy(String toyId, int intensity, int durationMs) {
    }

    @Override
    public void stopVibration() {
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isSearching() {
        return false;
    }

    @Override
    public List<ToyInfo> getConnectedToys() {
        return new ArrayList<>();
    }

    @Override
    public void release() {
    }
}
