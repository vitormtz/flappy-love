package com.example.flappylove.entity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.example.flappylove.util.Constants;

public class Pipe {
    private float x;
    private final float gapY;
    private final Paint paint;
    private final RectF topBounds;
    private final RectF bottomBounds;
    private boolean scored;

    public Pipe(float startX, float gapCenterY) {
        this.x = startX;
        this.gapY = gapCenterY;
        this.paint = new Paint();
        this.paint.setColor(0xFF4CAF50);
        this.topBounds = new RectF();
        this.bottomBounds = new RectF();
        this.scored = false;
        updateBounds();
    }

    public void update(float deltaTime) {
        x -= Constants.PIPE_VELOCITY * deltaTime;
        updateBounds();
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(topBounds, paint);
        canvas.drawRect(bottomBounds, paint);
    }

    private void updateBounds() {
        float halfGap = Constants.PIPE_GAP / 2;
        topBounds.set(x, 0, x + Constants.PIPE_WIDTH, gapY - halfGap);
        bottomBounds.set(x, gapY + halfGap, x + Constants.PIPE_WIDTH, Constants.SCREEN_HEIGHT);
    }

    public boolean isOffScreen() {
        return x + Constants.PIPE_WIDTH < 0;
    }

    public boolean collidesWith(RectF birdBounds) {
        return RectF.intersects(birdBounds, topBounds) || RectF.intersects(birdBounds, bottomBounds);
    }

    public boolean isPassed(float birdX) {
        return !scored && x + Constants.PIPE_WIDTH < birdX;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }

    public float getX() {
        return x;
    }
}
