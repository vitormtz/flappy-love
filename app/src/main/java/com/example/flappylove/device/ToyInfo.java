package com.example.flappylove.device;

public class ToyInfo {
    public final String id;
    public final String name;
    public int battery;

    public ToyInfo(String id, String name) {
        this.id = id;
        this.name = name;
        this.battery = -1;
    }
}
