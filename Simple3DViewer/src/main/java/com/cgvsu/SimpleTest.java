//package com.cgvsu;
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.control.Button;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//
//public class SimpleTest extends Application {
//
//    @Override
//    public void start(Stage primaryStage) {
//        System.out.println("=== Starting SimpleTest ===");
//
//        Canvas canvas = new Canvas(800, 600);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        // Простая отрисовка
//        drawTestScene(gc);
//
//        // Кнопка для загрузки модели
//        Button loadButton = new Button("Load Test Cube");
//        loadButton.setOnAction(e -> {
//            System.out.println("Button clicked!");
//            drawCube(gc);
//        });
//
//        Button clearButton = new Button("Clear");
//        clearButton.setOnAction(e -> drawTestScene(gc));
//
//        VBox root = new VBox(10, loadButton, clearButton, canvas);
//        Scene scene = new Scene(root, 850, 700);
//
//        primaryStage.setTitle("Simple 3D Viewer Test");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        System.out.println("Window shown successfully!");
//    }
//
//    private void drawTestScene(GraphicsContext gc) {
//        gc.setFill(Color.LIGHTGRAY);
//        gc.fillRect(0, 0, 800, 600);
//
//        gc.setFill(Color.BLUE);
//        gc.fillRect(100, 100, 200, 200);
//
//        gc.setStroke(Color.RED);
//        gc.setLineWidth(3);
//        gc.strokeRect(100, 100, 200, 200);
//
//        gc.setFill(Color.BLACK);
//        gc.fillText("JavaFX works! Click 'Load Test Cube'", 120, 180);
//    }
//
//    private void drawCube(GraphicsContext gc) {
//        gc.clearRect(0, 0, 800, 600);
//        gc.setFill(Color.LIGHTBLUE);
//        gc.fillRect(0, 0, 800, 600);
//
//        // Рисуем 3D куб в 2D (изометрическая проекция)
//        double centerX = 400;
//        double centerY = 300;
//        double size = 100;
//
//        // Передняя грань - используем double[]
//        double[] frontX = {centerX, centerX + size, centerX + size, centerX};
//        double[] frontY = {centerY, centerY - size/2, centerY + size/2, centerY};
//
//        // Задняя грань (смещена) - используем double[]
//        double[] backX = {
//                centerX + size/2,
//                centerX + size + size/2,
//                centerX + size + size/2,
//                centerX + size/2
//        };
//        double[] backY = {
//                centerY - size/2,
//                centerY - size,
//                centerY,
//                centerY - size/2
//        };
//
//        gc.setFill(Color.rgb(100, 150, 255, 0.7));
//        gc.fillPolygon(frontX, frontY, 4);
//        gc.fillPolygon(backX, backY, 4);
//
//        gc.setStroke(Color.DARKBLUE);
//        gc.setLineWidth(2);
//        gc.strokePolygon(frontX, frontY, 4);
//        gc.strokePolygon(backX, backY, 4);
//
//        // Соединяем углы (линии используют double координаты)
//        gc.strokeLine(frontX[0], frontY[0], backX[0], backY[0]);
//        gc.strokeLine(frontX[1], frontY[1], backX[1], backY[1]);
//        gc.strokeLine(frontX[2], frontY[2], backX[2], backY[2]);
//        gc.strokeLine(frontX[3], frontY[3], backX[3], backY[3]);
//
//        gc.setFill(Color.BLACK);
//        gc.fillText("3D Cube (simulated)", centerX - 50, centerY - 150);
//        gc.fillText("Vertices: 8", centerX - 50, centerY + 200);
//        gc.fillText("Polygons: 12", centerX - 50, centerY + 220);
//    }
//
//    // Важно: только один main-метод!
//    public static void main(String[] args) {
//        System.out.println("Launching JavaFX application...");
//        launch(args);
//    }
//}