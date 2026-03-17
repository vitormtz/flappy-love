package com.example.flappylove.entity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.example.flappylove.util.Constants;

public class Bird {
    private float x;
    private float y;
    private float velocity;
    private final Paint paint;
    private final RectF bounds;

    public Bird(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.paint = new Paint();
        this.paint.setColor(0xFFFFD700);
        this.bounds = new RectF();
        updateBounds();
    }

    public void update(float deltaTime) {
        velocity += Constants.GRAVITY * deltaTime;
        y += velocity * deltaTime;
        updateBounds();
    }

    public void jump() {
        velocity = Constants.JUMP_VELOCITY;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, Constants.BIRD_SIZE / 2, paint);
    }

    private void updateBounds() {
        float radius = Constants.BIRD_SIZE / 2;
        bounds.set(x - radius, y - radius, x + radius, y + radius);
    }

    public RectF getBounds() {
        return bounds;
    }

    public float getY() {
        return y;
    }

    public void clampY(float value) {
        this.y = value;
        this.velocity = 0;
        updateBounds();
    }

    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        updateBounds();
    }
}
