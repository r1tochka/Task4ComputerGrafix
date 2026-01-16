package com.cgvsu.math;

import com.cgvsu.math.base.AbstractVector;

public class Vector4f extends AbstractVector<Vector4f> {
    
    public Vector4f(float x, float y, float z, float w) {
        super(new float[]{x, y, z, w});
    }

    @Override
    protected Vector4f createNew(float[] components) {
        return new Vector4f(components[0], components[1], components[2], components[3]);
    }

    public float getX() {
        return components[0];
    }

    public float getY() {
        return components[1];
    }

    public float getZ() {
        return components[2];
    }

    public float getW() {
        return components[3];
    }

    // Конструктор из Vector3f с указанным w
    public Vector4f(Vector3f v, float w) {
        super(new float[]{v.getX(), v.getY(), v.getZ(), w});
    }

    // Конструктор из Vector3f с w=1
    public Vector4f(Vector3f v) {
        this(v, 1.0f);
    }

    // Преобразование в Vector3f (деление на w, если w != 0)
    public Vector3f toVector3f() {
        if (Math.abs(components[3]) > 1e-7f) {
            float invW = 1.0f / components[3];
            return new Vector3f(
                    components[0] * invW,
                    components[1] * invW,
                    components[2] * invW
            );
        }
        return new Vector3f(components[0], components[1], components[2]);
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%.2f, %.2f, %.2f, %.2f)", 
                components[0], components[1], components[2], components[3]);
    }
}
