package com.example.flappylove.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.example.flappylove.util.Constants;

public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private boolean running;
    private long lastTime;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.running = false;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        lastTime = System.nanoTime();
        final long targetTime = Constants.FRAME_TIME_MS;

        while (running) {
            long startTime = System.currentTimeMillis();
            Canvas canvas = null;

            try {
                long currentTime = System.nanoTime();
                float deltaTime = (currentTime - lastTime) / 1000000000.0f;
                lastTime = currentTime;

                deltaTime = Math.min(deltaTime, 0.05f);

                gameView.update(deltaTime * 60f);

                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        gameView.render(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = targetTime - elapsed;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
