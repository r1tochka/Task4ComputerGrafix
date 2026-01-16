package com.cgvsu.render_engine;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

public class Camera {

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        
        // Инициализация сферических координат
        updateSphericalCoordinates();
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
        updateSphericalCoordinates();
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
        updateCameraPosition();
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
    }

    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
    }

    /**
     * Вращение камеры вокруг цели (сферические координаты)
     * @param deltaAzimuth изменение азимутального угла (горизонтальное вращение)
     * @param deltaPolar изменение полярного угла (вертикальное вращение)
     */
    public void rotate(float deltaAzimuth, float deltaPolar) {
        azimuthAngle += deltaAzimuth;
        polarAngle += deltaPolar;
        
        // Ограничение полярного угла для предотвращения переворота камеры
        if (polarAngle < MIN_POLAR_ANGLE) {
            polarAngle = MIN_POLAR_ANGLE;
        } else if (polarAngle > MAX_POLAR_ANGLE) {
            polarAngle = MAX_POLAR_ANGLE;
        }
        
        updateCameraPosition();
    }

    /**
     * Панорамирование камеры (перемещение цели и позиции)
     * @param deltaX изменение по оси X
     * @param deltaY изменение по оси Y
     */
    public void pan(float deltaX, float deltaY) {
        // Вычисляем векторы направления камеры
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = forward.cross(new Vector3f(0, 1, 0)).normalize();
        Vector3f up = right.cross(forward).normalize();
        
        // Перемещаем цель и позицию вместе
        Vector3f panVector = right.multiply(deltaX).add(up.multiply(deltaY));
        target = target.add(panVector);
        position = position.add(panVector);
    }

    /**
     * Приближение/отдаление камеры (изменение расстояния до цели)
     * @param delta изменение расстояния
     */
    public void zoom(float delta) {
        distanceFromTarget += delta;
        
        // Ограничение расстояния
        if (distanceFromTarget < MIN_DISTANCE) {
            distanceFromTarget = MIN_DISTANCE;
        } else if (distanceFromTarget > MAX_DISTANCE) {
            distanceFromTarget = MAX_DISTANCE;
        }
        
        updateCameraPosition();
    }

    /**
     * Обновление сферических координат на основе текущей позиции и цели
     */
    private void updateSphericalCoordinates() {
        Vector3f direction = position.subtract(target);
        distanceFromTarget = direction.length();
        
        if (distanceFromTarget < 1e-6f) {
            distanceFromTarget = 10.0f; // Значение по умолчанию
        }
        
        // Вычисляем полярный угол (угол от оси Y)
        polarAngle = (float) Math.acos(direction.getY() / distanceFromTarget);
        
        // Вычисляем азимутальный угол (угол в плоскости XZ)
        azimuthAngle = (float) Math.atan2(direction.getX(), direction.getZ());
    }

    /**
     * Обновление позиции камеры на основе сферических координат
     */
    private void updateCameraPosition() {
        float x = distanceFromTarget * (float) Math.sin(polarAngle) * (float) Math.sin(azimuthAngle);
        float y = distanceFromTarget * (float) Math.cos(polarAngle);
        float z = distanceFromTarget * (float) Math.sin(polarAngle) * (float) Math.cos(azimuthAngle);
        
        position = target.add(new Vector3f(x, y, z));
    }

    public Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // Геттеры для параметров камеры
    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    // Сферические координаты для управления камерой
    private float distanceFromTarget = 10.0f;
    private float azimuthAngle = 0.0f;  // Азимутальный угол (горизонтальное вращение)
    private float polarAngle = (float) Math.PI / 4.0f;  // Полярный угол (вертикальное вращение)

    // Ограничения для сферических координат
    private static final float MIN_POLAR_ANGLE = 0.01f;
    private static final float MAX_POLAR_ANGLE = (float) Math.PI - 0.01f;
    private static final float MIN_DISTANCE = 0.5f;
    private static final float MAX_DISTANCE = 2000.0f;
}