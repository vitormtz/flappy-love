package com.example.flappylove.game;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private boolean paused;
    private Paint textPaint;
    private Paint gameOverPaint;
    private ScoreManager scoreManager;
    private DeviceController deviceController;
    private Context context;

    private boolean ghostMode;
    private boolean immortalMode;
    private boolean slowMotion;
    private boolean speedMode;

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

    private static final float GEAR_SIZE = 80f;
    private static final float GEAR_MARGIN = 30f;
    private RectF gearBounds;
    private Paint gearPaint;
    private Paint overlayPaint;
    private Paint panelPaint;
    private Paint panelTextPaint;
    private Paint panelSubTextPaint;
    private Paint toggleOnPaint;
    private Paint toggleOffPaint;
    private Paint closeBtnPaint;

    private static final float PANEL_WIDTH = 700f;
    private static final float PANEL_HEIGHT = 750f;
    private static final float TOGGLE_SIZE = 50f;
    private static final float ROW_HEIGHT = 100f;

    private RectF ghostToggleBounds;
    private RectF immortalToggleBounds;
    private RectF slowMotionToggleBounds;
    private RectF speedModeToggleBounds;
    private RectF closePanelBounds;
    private RectF exitBounds;

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
        this.context = context;
        bird = new Bird(Constants.SCREEN_WIDTH / 4f, Constants.SCREEN_HEIGHT / 2f);
        pipes = new ArrayList<>();
        random = new Random();
        nextPipeX = Constants.SCREEN_WIDTH;
        score = 0;
        gameOver = false;
        paused = false;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(120);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        scoreManager = new ScoreManager(context);

        ghostMode = scoreManager.isGhostMode();
        immortalMode = scoreManager.isImmortalMode();
        slowMotion = scoreManager.isSlowMotion();
        speedMode = scoreManager.isSpeedMode();

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

        gearPaint = new Paint();
        gearPaint.setColor(Color.WHITE);
        gearPaint.setTextSize(60);
        gearPaint.setTextAlign(Paint.Align.CENTER);
        gearPaint.setAntiAlias(true);

        float gearX = Constants.SCREEN_WIDTH - GEAR_MARGIN - GEAR_SIZE;
        float gearY = GEAR_MARGIN;
        gearBounds = new RectF(gearX, gearY, gearX + GEAR_SIZE, gearY + GEAR_SIZE);

        overlayPaint = new Paint();
        overlayPaint.setColor(0xAA000000);

        panelPaint = new Paint();
        panelPaint.setColor(0xF0FFFFFF);
        panelPaint.setAntiAlias(true);

        panelTextPaint = new Paint();
        panelTextPaint.setColor(0xFF333333);
        panelTextPaint.setTextSize(42);
        panelTextPaint.setTextAlign(Paint.Align.LEFT);
        panelTextPaint.setAntiAlias(true);

        panelSubTextPaint = new Paint();
        panelSubTextPaint.setColor(0xFF888888);
        panelSubTextPaint.setTextSize(28);
        panelSubTextPaint.setTextAlign(Paint.Align.LEFT);
        panelSubTextPaint.setAntiAlias(true);

        toggleOnPaint = new Paint();
        toggleOnPaint.setColor(0xFF4CAF50);
        toggleOnPaint.setAntiAlias(true);

        toggleOffPaint = new Paint();
        toggleOffPaint.setColor(0xFFBDBDBD);
        toggleOffPaint.setAntiAlias(true);

        closeBtnPaint = new Paint();
        closeBtnPaint.setColor(0xFF333333);
        closeBtnPaint.setTextSize(48);
        closeBtnPaint.setTextAlign(Paint.Align.CENTER);
        closeBtnPaint.setAntiAlias(true);

        ghostToggleBounds = new RectF();
        immortalToggleBounds = new RectF();
        slowMotionToggleBounds = new RectF();
        speedModeToggleBounds = new RectF();
        closePanelBounds = new RectF();
        exitBounds = new RectF();
    }

    public void setDeviceController(DeviceController controller) {
        this.deviceController = controller;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            if (paused) {
                handlePanelTouch(x, y);
                return true;
            }

            if (gearBounds.contains(x, y)) {
                paused = true;
                if (deviceController != null) {
                    deviceController.stopVibration();
                }
                return true;
            }

            if (gameOver) {
                resetGame();
            } else {
                bird.jump();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void handlePanelTouch(float x, float y) {
        if (closePanelBounds.contains(x, y)) {
            paused = false;
            return;
        }
        if (exitBounds.contains(x, y)) {
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
            return;
        }
        if (ghostToggleBounds.contains(x, y)) {
            ghostMode = !ghostMode;
            scoreManager.setGhostMode(ghostMode);
            return;
        }
        if (immortalToggleBounds.contains(x, y)) {
            immortalMode = !immortalMode;
            scoreManager.setImmortalMode(immortalMode);
            return;
        }
        if (slowMotionToggleBounds.contains(x, y)) {
            slowMotion = !slowMotion;
            scoreManager.setSlowMotion(slowMotion);
            if (slowMotion && speedMode) {
                speedMode = false;
                scoreManager.setSpeedMode(false);
            }
            return;
        }
        if (speedModeToggleBounds.contains(x, y)) {
            speedMode = !speedMode;
            scoreManager.setSpeedMode(speedMode);
            if (speedMode && slowMotion) {
                slowMotion = false;
                scoreManager.setSlowMotion(false);
            }
        }
    }

    public void update(float deltaTime) {
        if (gameOver || paused) return;

        float speedMultiplier = 1f;
        if (slowMotion) speedMultiplier = 0.5f;
        if (speedMode) speedMultiplier = 2f;

        bird.update(deltaTime);

        if (!immortalMode) {
            if (bird.getY() - Constants.BIRD_SIZE / 2 <= 0 ||
                bird.getY() + Constants.BIRD_SIZE / 2 >= Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT) {
                triggerGameOver();
                return;
            }
        } else {
            float minY = Constants.BIRD_SIZE / 2;
            float maxY = Constants.SCREEN_HEIGHT - Constants.FLOOR_HEIGHT - Constants.BIRD_SIZE / 2;
            if (bird.getY() < minY) {
                bird.clampY(minY);
            } else if (bird.getY() > maxY) {
                bird.clampY(maxY);
            }
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update(deltaTime * speedMultiplier);

            if (pipe.collidesWith(bird.getBounds())) {
                if (!ghostMode && !immortalMode) {
                    triggerGameOver();
                    return;
                }
                if (immortalMode && !ghostMode) {
                    if (pipe.collidesWithTop(bird.getBounds())) {
                        bird.clampY(pipe.getGapTopY() + Constants.BIRD_SIZE / 2);
                    } else if (pipe.collidesWithBottom(bird.getBounds())) {
                        bird.clampY(pipe.getGapBottomY() - Constants.BIRD_SIZE / 2);
                    }
                }
            }

            if (pipe.isPassed(bird.getBounds().left)) {
                pipe.setScored(true);
                score++;
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        float pipeSpeed = Constants.PIPE_VELOCITY * deltaTime * speedMultiplier;
        float spawnDistance = speedMode ? 400f : Constants.PIPE_SPAWN_DISTANCE;
        if (nextPipeX - pipeSpeed <= Constants.SCREEN_WIDTH - spawnDistance) {
            spawnPipe();
        }
        nextPipeX -= pipeSpeed;

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

        canvas.drawText("\u2699", gearBounds.centerX(), gearBounds.centerY() + 20, gearPaint);

        if (gameOver) {
            canvas.drawText("GAME OVER", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f - 100, gameOverPaint);
            canvas.drawText("High Score: " + scoreManager.getHighScore(), Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 100, textPaint);
            canvas.drawText("Tap to restart", Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f + 250, textPaint);
        }

        if (paused) {
            drawPausePanel(canvas);
        }
    }

    private void drawPausePanel(Canvas canvas) {
        canvas.drawRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, overlayPaint);

        float panelX = (Constants.SCREEN_WIDTH - PANEL_WIDTH) / 2f;
        float panelY = (Constants.SCREEN_HEIGHT - PANEL_HEIGHT) / 2f;
        RectF panelRect = new RectF(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT);
        canvas.drawRoundRect(panelRect, 30, 30, panelPaint);

        closePanelBounds.set(panelX + PANEL_WIDTH - 70, panelY + 10, panelX + PANEL_WIDTH - 10, panelY + 70);
        canvas.drawText("\u2715", closePanelBounds.centerX(), closePanelBounds.centerY() + 15, closeBtnPaint);

        Paint titlePaint = new Paint(panelTextPaint);
        titlePaint.setTextSize(50);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Game Modes", panelX + PANEL_WIDTH / 2f, panelY + 70, titlePaint);

        float startY = panelY + 130;
        float leftX = panelX + 40;
        float toggleX = panelX + PANEL_WIDTH - 40 - TOGGLE_SIZE;

        drawModeRow(canvas, leftX, startY, toggleX, "Ghost Mode", "Pass through pipes", ghostMode, ghostToggleBounds);
        drawModeRow(canvas, leftX, startY + ROW_HEIGHT, toggleX, "Immortal Mode", "Full invincibility", immortalMode, immortalToggleBounds);
        drawModeRow(canvas, leftX, startY + ROW_HEIGHT * 2, toggleX, "Slow Motion", "Half pipe speed", slowMotion, slowMotionToggleBounds);
        drawModeRow(canvas, leftX, startY + ROW_HEIGHT * 3, toggleX, "Speed Mode", "Faster pipes, closer spawn", speedMode, speedModeToggleBounds);

        float exitY = startY + ROW_HEIGHT * 4 + 40;
        Paint exitPaint = new Paint();
        exitPaint.setColor(0xFFF44336);
        exitPaint.setAntiAlias(true);
        exitBounds.set(panelX + 40, exitY, panelX + PANEL_WIDTH - 40, exitY + 70);
        canvas.drawRoundRect(exitBounds, 15, 15, exitPaint);

        Paint exitTextPaint = new Paint();
        exitTextPaint.setColor(Color.WHITE);
        exitTextPaint.setTextSize(36);
        exitTextPaint.setTextAlign(Paint.Align.CENTER);
        exitTextPaint.setAntiAlias(true);
        canvas.drawText("EXIT TO MENU", exitBounds.centerX(), exitBounds.centerY() + 12, exitTextPaint);
    }

    private void drawModeRow(Canvas canvas, float leftX, float y, float toggleX, String title, String subtitle, boolean enabled, RectF toggleBounds) {
        canvas.drawText(title, leftX, y + 30, panelTextPaint);
        canvas.drawText(subtitle, leftX, y + 60, panelSubTextPaint);

        toggleBounds.set(toggleX, y + 10, toggleX + TOGGLE_SIZE, y + 10 + TOGGLE_SIZE);
        Paint bg = enabled ? toggleOnPaint : toggleOffPaint;
        canvas.drawRoundRect(toggleBounds, 10, 10, bg);

        if (enabled) {
            Paint checkPaint = new Paint();
            checkPaint.setColor(Color.WHITE);
            checkPaint.setTextSize(36);
            checkPaint.setTextAlign(Paint.Align.CENTER);
            checkPaint.setAntiAlias(true);
            canvas.drawText("\u2713", toggleBounds.centerX(), toggleBounds.centerY() + 12, checkPaint);
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
