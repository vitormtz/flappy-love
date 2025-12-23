package com.example.flappylove.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flappylove.R;
import com.example.flappylove.util.ScoreManager;

public class MainActivity extends AppCompatActivity {
    private ScoreManager scoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreManager = new ScoreManager(this);

        Button playButton = findViewById(R.id.btnPlay);
        Button settingsButton = findViewById(R.id.btnSettings);
        TextView highScoreText = findViewById(R.id.tvHighScore);

        highScoreText.setText("High Score: " + scoreManager.getHighScore());

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView highScoreText = findViewById(R.id.tvHighScore);
        highScoreText.setText("High Score: " + scoreManager.getHighScore());
    }
}
