package com.example.flappylove.util;

public class Constants {
    public static final int SCREEN_WIDTH = 1080;
    public static final int SCREEN_HEIGHT = 1920;

    public static final float GRAVITY = 1.2f;
    public static final float JUMP_VELOCITY = -20f;
    public static final float BIRD_SIZE = 60f;

    public static final float PIPE_WIDTH = 150f;
    public static final float PIPE_GAP = 400f;
    public static final float PIPE_VELOCITY = 5f;
    public static final float PIPE_SPAWN_DISTANCE = 600f;
    public static final float PIPE_MIN_HEIGHT = 200f;
    public static final float PIPE_MAX_HEIGHT = 800f;

    public static final float FLOOR_HEIGHT = 200f;
    public static final float FLOOR_VELOCITY = 5f;

    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_MS = 1000 / TARGET_FPS;

    public static final String PREFS_NAME = "FlappyLovePrefs";
    public static final String KEY_HIGH_SCORE = "highScore";
    public static final String KEY_LOVENSE_ENABLED = "lovenseEnabled";
    public static final String KEY_GHOST_MODE = "ghostMode";
    public static final String KEY_GOD_MODE = "godMode";
    public static final String KEY_SLOW_MOTION = "slowMotion";
    public static final String KEY_SPEED_MODE = "speedMode";
    public static final String KEY_HOLD_MODE = "holdMode";
    public static final String KEY_LOOP_MODE = "loopMode";
}
