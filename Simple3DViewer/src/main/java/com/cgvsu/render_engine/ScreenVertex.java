package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

public class ScreenVertex {
    private final float x;
    private final float y;
    private final float z;
    // reciprocal of clip-space w (1 / w) for perspective-correct interpolation
    private final float invW;
    private final Vector2f textureCoords;
    private final Vector3f normal;
    private final Vector3f worldPosition;
    private final Float lightingIntensity;

    public ScreenVertex(float x, float y, float z) {
        this(x, y, z, 1.0f, null, null, null, null);
    }

    public ScreenVertex(float x, float y, float z, float invW,
                        Vector2f textureCoords, Vector3f normal,
                        Vector3f worldPosition, Float lightingIntensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.invW = invW;
        this.textureCoords = textureCoords;
        this.normal = normal;
        this.worldPosition = worldPosition;
        this.lightingIntensity = lightingIntensity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    /**
     * Возвращает 1 / w из clip-space.
     * Используется для перспективно корректной интерполяции атрибутов.
     */
    public float getInvW() {
        return invW;
    }


    public Vector2f getTextureCoords() {
        return textureCoords;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public boolean hasTextureCoords() {
        return textureCoords != null;
    }

    public boolean hasNormal() {
        return normal != null;
    }

    public Vector3f getWorldPosition() {
        return worldPosition;
    }

    public boolean hasWorldPosition() {
        return worldPosition != null;
    }

    public Float getLightingIntensity() {
        return lightingIntensity;
    }

    public boolean hasLightingIntensity() {
        return lightingIntensity != null;
    }
}


