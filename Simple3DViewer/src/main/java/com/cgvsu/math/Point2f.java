package com.cgvsu.math;

public class Point2f {
    private final float x;
    private final float y;

    public Point2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("Point2f(%.2f, %.2f)", x, y);
    }
}
