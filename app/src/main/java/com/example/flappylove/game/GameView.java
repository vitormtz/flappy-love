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

    private int currentVibrationLevel;
    private Paint barBgPaint;
    private Paint barFillPaint;
    private Paint barBorderPaint;
    private Paint barTextPaint;

    private static final float BAR_WIDTH = 40f;
    private static final float BAR_HEIGHT = 400f;
    private static final float BAR_MARGIN_RIGHT = 30f;
    private static final float BAR_MARGIN_TOP = 200f;
    private static final int MAX_VIBRATION_LEVEL = 20;

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

        currentVibrationLevel = 0;

        barBgPaint = new Paint();
        barBgPaint.setColor(0x80000000);

        barFillPaint = new Paint();

        barBorderPaint = new Paint();
        barBorderPaint.setColor(Color.WHITE);
        barBorderPaint.setStyle(Paint.Style.STROKE);
        barBorderPaint.setStrokeWidth(3f);

        barTextPaint = new Paint();
        barTextPaint.setColor(Color.WHITE);
        barTextPaint.setTextSize(28);
        barTextPaint.setTextAlign(Paint.Align.CENTER);
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
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        if (nextPipeX - Constants.PIPE_VELOCITY * deltaTime <= Constants.SCREEN_WIDTH - Constants.PIPE_SPAWN_DISTANCE) {
            spawnPipe();
        }
        nextPipeX -= Constants.PIPE_VELOCITY * deltaTime;

        updateVibration();
    }

    private void updateVibration() {
        float birdY = bird.getY();
        float floorY = Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT;

        float topLimit = Constants.PIPE_MIN_HEIGHT;
        float bottomLimit = floorY - Constants.PIPE_MIN_HEIGHT;
        float normalized = 1f - ((birdY - topLimit) / (bottomLimit - topLimit));
        normalized = Math.min(Math.max(normalized, 0f), 1f);
        currentVibrationLevel = Math.max(1, Math.round(normalized * MAX_VIBRATION_LEVEL));

        if (deviceController != null && currentVibrationLevel > 0) {
            deviceController.vibrate(currentVibrationLevel, 0);
        } else if (deviceController != null) {
            deviceController.stopVibration();
        }
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

        drawVibrationBar(canvas);

        if (gameOver) {
            canvas.drawText("GAME OVER", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f - 100, gameOverPaint);
            canvas.drawText("High Score: " + scoreManager.getHighScore(), Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 100, textPaint);
            canvas.drawText("Tap to restart", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 250, textPaint);
        }
    }

    private void drawVibrationBar(Canvas canvas) {
        if (deviceController == null || !deviceController.isConnected()) return;

        float barX = Constants.SCREEN_WIDTH - BAR_WIDTH - BAR_MARGIN_RIGHT;
        float barY = BAR_MARGIN_TOP;

        canvas.drawRect(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, barBgPaint);

        float fillRatio = currentVibrationLevel / (float) MAX_VIBRATION_LEVEL;
        float fillHeight = BAR_HEIGHT * fillRatio;
        float fillTop = barY + BAR_HEIGHT - fillHeight;

        int colorGreen = 0xFF4CAF50;
        int colorYellow = 0xFFFFEB3B;
        int colorRed = 0xFFF44336;

        if (fillRatio <= 0.5f) {
            float t = fillRatio * 2f;
            int r = (int) (Color.red(colorGreen) + t * (Color.red(colorYellow) - Color.red(colorGreen)));
            int g = (int) (Color.green(colorGreen) + t * (Color.green(colorYellow) - Color.green(colorGreen)));
            int b = (int) (Color.blue(colorGreen) + t * (Color.blue(colorYellow) - Color.blue(colorGreen)));
            barFillPaint.setColor(Color.rgb(r, g, b));
        } else {
            float t = (fillRatio - 0.5f) * 2f;
            int r = (int) (Color.red(colorYellow) + t * (Color.red(colorRed) - Color.red(colorYellow)));
            int g = (int) (Color.green(colorYellow) + t * (Color.green(colorRed) - Color.green(colorYellow)));
            int b = (int) (Color.blue(colorYellow) + t * (Color.blue(colorRed) - Color.blue(colorYellow)));
            barFillPaint.setColor(Color.rgb(r, g, b));
        }

        canvas.drawRect(barX, fillTop, barX + BAR_WIDTH, barY + BAR_HEIGHT, barFillPaint);
        canvas.drawRect(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, barBorderPaint);

        float textX = barX + BAR_WIDTH / 2f;
        canvas.drawText(currentVibrationLevel + "/" + MAX_VIBRATION_LEVEL, textX, barY + BAR_HEIGHT + 35, barTextPaint);
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
        currentVibrationLevel = 0;
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
