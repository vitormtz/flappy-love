package com.example.flappylove.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.flappylove.device.DeviceController;
import com.example.flappylove.entity.Bird;
import com.example.flappylove.entity.Pipe;
import com.example.flappylove.util.Constants;
import com.example.flappylove.util.ScoreManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private Bird bird;
    private List<Pipe> pipes;
    private Random random;
    private float nextPipeX;
    private int score;
    private boolean gameOver;
    private Paint textPaint;
    private Paint gameOverPaint;
    private ScoreManager scoreManager;
    private DeviceController deviceController;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        init(context);
    }

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        init(context);
    }

    private void init(Context context) {
        bird = new Bird(Constants.SCREEN_WIDTH / 4f, Constants.SCREEN_HEIGHT / 2f);
        pipes = new ArrayList<>();
        random = new Random();
        nextPipeX = Constants.SCREEN_WIDTH;
        score = 0;
        gameOver = false;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(120);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        scoreManager = new ScoreManager(context);
    }

    public void setDeviceController(DeviceController controller) {
        this.deviceController = controller;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                resetGame();
            } else {
                bird.jump();
                if (deviceController != null) {
                    deviceController.vibrate(8, 50);
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update(float deltaTime) {
        if (gameOver) return;

        bird.update(deltaTime);

        if (bird.getY() - Constants.BIRD_SIZE / 2 <= 0 ||
            bird.getY() + Constants.BIRD_SIZE / 2 >= Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT) {
            triggerGameOver();
            return;
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update(deltaTime);

            if (pipe.collidesWith(bird.getBounds())) {
                triggerGameOver();
                return;
            }

            if (pipe.isPassed(bird.getBounds().left)) {
                pipe.setScored(true);
                score++;
                if (deviceController != null) {
                    deviceController.vibrate(12, 100);
                }
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        if (nextPipeX - Constants.PIPE_VELOCITY * deltaTime <= Constants.SCREEN_WIDTH - Constants.PIPE_SPAWN_DISTANCE) {
            spawnPipe();
        }
        nextPipeX -= Constants.PIPE_VELOCITY * deltaTime;
    }

    public void render(Canvas canvas) {
        if (canvas == null) return;

        canvas.drawColor(0xFF87CEEB);

        for (Pipe pipe : pipes) {
            pipe.draw(canvas);
        }

        bird.draw(canvas);

        Paint floorPaint = new Paint();
        floorPaint.setColor(0xFF8B4513);
        canvas.drawRect(0, Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT,
                       Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, floorPaint);

        canvas.drawText("Score: " + score, Constants.SCREEN_WIDTH / 2f, 150, textPaint);

        if (gameOver) {
            canvas.drawText("GAME OVER", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f - 100, gameOverPaint);
            canvas.drawText("High Score: " + scoreManager.getHighScore(), Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 100, textPaint);
            canvas.drawText("Tap to restart", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 250, textPaint);
        }
    }

    private void spawnPipe() {
        float minY = Constants.PIPE_MIN_HEIGHT + Constants.PIPE_GAP / 2;
        float maxY = Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT - Constants.PIPE_GAP / 2 - Constants.PIPE_MIN_HEIGHT;
        float gapY = minY + random.nextFloat() * (maxY - minY);
        pipes.add(new Pipe(Constants.SCREEN_WIDTH, gapY));
        nextPipeX = Constants.SCREEN_WIDTH;
    }

    private void triggerGameOver() {
        gameOver = true;
        scoreManager.saveHighScore(score);
        if (deviceController != null) {
            deviceController.stopVibration();
        }
    }

    private void resetGame() {
        bird.reset(Constants.SCREEN_WIDTH / 4f, Constants.SCREEN_HEIGHT / 2f);
        pipes.clear();
        nextPipeX = Constants.SCREEN_WIDTH;
        score = 0;
        gameOver = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameThread = new GameThread(getHolder(), this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        if (gameThread != null) {
            gameThread.setRunning(false);
        }
    }

    public void resume() {
        if (gameThread != null && !gameThread.isRunning()) {
            gameThread.setRunning(true);
        }
    }
}
