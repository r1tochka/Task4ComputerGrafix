package com.cgvsu.controller;

import com.cgvsu.render_engine.Camera;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Контроллер для управления камерой мышью
 */
public class CameraController {

    private Camera camera;
    private double mousePrevX, mousePrevY;
    private boolean isRotating = false;
    private boolean isPanning = false;

    private final float ROTATION_SENSITIVITY = 0.01f;
    private final float PAN_SENSITIVITY = 0.03f;
    private final float ZOOM_SENSITIVITY = 0.1f;

    public CameraController(Camera camera) {
        this.camera = camera;
    }

    public void setCamera(final Camera camera) {
        if (camera == null) {
            return;
        }
        this.camera = camera;
    }

    /**
     * Обработка нажатия кнопки мыши
     * ЛКМ - вращение, ПКМ - панорама
     */
    public void handleMousePressed(MouseEvent event) {
        mousePrevX = event.getX();
        mousePrevY = event.getY();

        if (event.getButton() == MouseButton.PRIMARY) {
            isRotating = true;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            isPanning = true;
        }
    }

    /**
     * Обработка перетаскивания мыши
     */
    public void handleMouseDragged(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        double deltaX = mouseX - mousePrevX;
        double deltaY = mouseY - mousePrevY;

        if (isRotating && camera != null) {
            camera.rotate((float) (-deltaX * ROTATION_SENSITIVITY),
                         (float) (-deltaY * ROTATION_SENSITIVITY));
        } else if (isPanning && camera != null) {
            camera.pan((float) (deltaX * PAN_SENSITIVITY),
                      (float) (-deltaY * PAN_SENSITIVITY));
        }

        mousePrevX = mouseX;
        mousePrevY = mouseY;
    }

    /**
     * Обработка отпускания кнопки мыши
     */
    public void handleMouseReleased(MouseEvent event) {
        isRotating = false;
        isPanning = false;
    }

    /**
     * Обработка прокрутки колесика мыши (зум)
     */
    public void handleMouseScroll(ScrollEvent event) {
        if (camera != null) {
            double deltaY = event.getDeltaY();
            camera.zoom((float) (-deltaY * ZOOM_SENSITIVITY));
        }
    }
}
