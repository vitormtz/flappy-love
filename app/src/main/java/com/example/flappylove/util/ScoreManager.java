package com.example.flappylove.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreManager {
    private final SharedPreferences prefs;

    public ScoreManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getHighScore() {
        return prefs.getInt(Constants.KEY_HIGH_SCORE, 0);
    }

    public void saveHighScore(int score) {
        if (score > getHighScore()) {
            prefs.edit().putInt(Constants.KEY_HIGH_SCORE, score).apply();
        }
    }

    public boolean isLovenseEnabled() {
        return prefs.getBoolean(Constants.KEY_LOVENSE_ENABLED, false);
    }

    public void setLovenseEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_LOVENSE_ENABLED, enabled).apply();
    }

    public boolean isGhostMode() {
        return prefs.getBoolean(Constants.KEY_GHOST_MODE, false);
    }

    public void setGhostMode(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_GHOST_MODE, enabled).apply();
    }

    public boolean isGodMode() {
        return prefs.getBoolean(Constants.KEY_GOD_MODE, false);
    }

    public void setGodMode(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_GOD_MODE, enabled).apply();
    }

    public boolean isSlowMotion() {
        return prefs.getBoolean(Constants.KEY_SLOW_MOTION, false);
    }

    public void setSlowMotion(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_SLOW_MOTION, enabled).apply();
    }

    public boolean isSpeedMode() {
        return prefs.getBoolean(Constants.KEY_SPEED_MODE, false);
    }

    public void setSpeedMode(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_SPEED_MODE, enabled).apply();
    }

    public boolean isHoldMode() {
        return prefs.getBoolean(Constants.KEY_HOLD_MODE, false);
    }

    public void setHoldMode(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_HOLD_MODE, enabled).apply();
    }
}
