package com.cgvsu.view;

import com.cgvsu.controller.CameraController;
import com.cgvsu.render_engine.Camera;
import javafx.scene.canvas.Canvas;

/**
 * Прослойка между CameraController и UI компонентами
 */
public class CameraViewController {
    private final CameraController cameraController;

    public CameraViewController(Camera camera) {
        this.cameraController = new CameraController(camera);
    }

    public void setCamera(final Camera camera) {
        cameraController.setCamera(camera);
    }

    /**
     * Настройка обработчиков мыши для canvas
     */
    public void setupMouseControls(Canvas canvas) {
        canvas.setOnMousePressed(cameraController::handleMousePressed);
        canvas.setOnMouseDragged(cameraController::handleMouseDragged);
        canvas.setOnMouseReleased(cameraController::handleMouseReleased);
        canvas.setOnScroll(cameraController::handleMouseScroll);
    }
}
