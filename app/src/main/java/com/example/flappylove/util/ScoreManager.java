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
}
