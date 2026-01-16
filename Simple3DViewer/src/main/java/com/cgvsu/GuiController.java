package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.RenderingModes;
import com.cgvsu.render_engine.Texture;
import com.cgvsu.render_engine.Lighting;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.ModelProcessor;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.CameraManager;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    private Vector3f modelPosition = new Vector3f(0, 0, 0); // Позиция модели
    private Vector3f modelRotation = new Vector3f(0, 0, 0); // Вращение (в радианах)
    private float modelScale = 1.0f; // Масштаб

    private float rotationSpeed = 0.05f; // Скорость вращения
    private float moveSpeed = 0.5f;      // Скорость движения
    private float zoomSpeed = 0.1f;      // Скорость приближения

    // Для вращения мышкой
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private CheckBox drawWireframeCheckBox;

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private CheckBox useLightingCheckBox;

    private Model mesh = null;
    private RenderingModes renderingModes = new RenderingModes();
    private Texture texture = null;
    private Lighting lighting = null;

    private CameraManager camera = new CameraManager(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    //private Timeline timeline;

    @FXML
    private void initialize() {
        System.out.println("Графический контроллер запущен");

        // Рисуем начальную сцену
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawStaticScene(gc);

        // Назначаем чекбоксам перерисовку
        drawWireframeCheckBox.setOnAction(e -> drawStaticScene(gc));
        useTextureCheckBox.setOnAction(e -> drawStaticScene(gc));
        useLightingCheckBox.setOnAction(e -> drawStaticScene(gc));

        // ★ ВАЖНО: Делаем canvas фокусируемым ★
        canvas.setFocusTraversable(true);

        // ★ Автоматически даём фокус canvas при запуске ★
        canvas.requestFocus();

        // ★ ДОБАВЬ ОБРАБОТКУ КЛАВИШ ★
        setupKeyboardControls();
        setupMouseControls();
    }

    private void setupMouseControls() {
        // ★ КЛИК ПО CANVAS ДАЁТ ЕМУ ФОКУС ★
        canvas.setOnMouseClicked(event -> {
            canvas.requestFocus();
            System.out.println("Canvas получил фокус (клик мыши)");
        });

        // Вращение модели при зажатой ЛКМ
        canvas.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                isDragging = true;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                canvas.requestFocus(); // Даём фокус при начале перетаскивания
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (isDragging && mesh != null) {
                double deltaX = event.getX() - lastMouseX;
                double deltaY = event.getY() - lastMouseY;

                modelRotation = new Vector3f(
                        modelRotation.getX() + (float)deltaY * 0.01f,
                        modelRotation.getY() + (float)deltaX * 0.01f,
                        modelRotation.getZ()
                );

                lastMouseX = event.getX();
                lastMouseY = event.getY();

                drawStaticScene(canvas.getGraphicsContext2D());
            }
        });

        canvas.setOnMouseReleased(event -> {
            isDragging = false;
        });

        // Zoom колесиком мыши
        canvas.setOnScroll(event -> {
            if (mesh != null) {
                double delta = event.getDeltaY();

                if (delta < 0) { // Вниз = приблизить
                    modelScale *= 1.1f;
                } else { // Вверх = отдалить
                    modelScale *= 0.9f;
                }

                System.out.println("Масштаб колесиком: " + modelScale);
                drawStaticScene(canvas.getGraphicsContext2D());
            }
        });
    }

    // ★ Метод для настройки клавиатуры ★
    private void setupKeyboardControls() {
        // Обработка нажатия клавиш
        canvas.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());

            switch (event.getCode()) {
                // ВРАЩЕНИЕ ВОКРУГ ОСИ Y
                case Q:
                    modelRotation = new Vector3f(
                            modelRotation.getX(),
                            modelRotation.getY() - rotationSpeed,
                            modelRotation.getZ()
                    );
                    System.out.println("Вращение влево (Q)");
                    break;

                case E:
                    modelRotation = new Vector3f(
                            modelRotation.getX(),
                            modelRotation.getY() + rotationSpeed,
                            modelRotation.getZ()
                    );
                    System.out.println("Вращение вправо (E)");
                    break;

                // ВРАЩЕНИЕ ВОКРУГ ОСИ X
                case R:
                    modelRotation = new Vector3f(
                            modelRotation.getX() - rotationSpeed,
                            modelRotation.getY(),
                            modelRotation.getZ()
                    );
                    System.out.println("Вращение вверх (R)");
                    break;

                case F:
                    modelRotation = new Vector3f(
                            modelRotation.getX() + rotationSpeed,
                            modelRotation.getY(),
                            modelRotation.getZ()
                    );
                    System.out.println("Вращение вниз (F)");
                    break;

                // ДВИЖЕНИЕ МОДЕЛИ
                case W: // Вперед (по Z)
                    modelPosition = new Vector3f(
                            modelPosition.getX(),
                            modelPosition.getY(),
                            modelPosition.getZ() - moveSpeed
                    );
                    System.out.println("Вперед (W)");
                    break;

                case S: // Назад (по Z)
                    modelPosition = new Vector3f(
                            modelPosition.getX(),
                            modelPosition.getY(),
                            modelPosition.getZ() + moveSpeed
                    );
                    System.out.println("Назад (S)");
                    break;

                case A: // Влево (по X)
                    modelPosition = new Vector3f(
                            modelPosition.getX() - moveSpeed,
                            modelPosition.getY(),
                            modelPosition.getZ()
                    );
                    System.out.println("Влево (A)");
                    break;

                case D: // Вправо (по X)
                    modelPosition = new Vector3f(
                            modelPosition.getX() + moveSpeed,
                            modelPosition.getY(),
                            modelPosition.getZ()
                    );
                    System.out.println("Вправо (D)");
                    break;

                // ВВЕРХ/ВНИЗ
                case SPACE: // Вверх (по Y)
                    modelPosition = new Vector3f(
                            modelPosition.getX(),
                            modelPosition.getY() + moveSpeed,
                            modelPosition.getZ()
                    );
                    System.out.println("Вверх (Space)");
                    break;

                case C: // Вниз (по Y)
                    modelPosition = new Vector3f(
                            modelPosition.getX(),
                            modelPosition.getY() - moveSpeed,
                            modelPosition.getZ()
                    );
                    System.out.println("Вниз (C)");
                    break;

                // МАСШТАБ
                case ADD:      // + на цифровой клавиатуре
                case EQUALS:   // = (рядом с backspace, требует Shift)
                case PLUS:     // +
                    modelScale *= 1.1f;
                    System.out.println("Приблизить (+) Масштаб: " + modelScale);
                    break;

                case SUBTRACT: // - на цифровой клавиатуре
                case MINUS:    // -
                    modelScale *= 0.9f;
                    System.out.println("Отдалить (-) Масштаб: " + modelScale);
                    break;

                // СБРОС
                case BACK_SPACE:
                    modelPosition = new Vector3f(0, 0, 0);
                    modelRotation = new Vector3f(0, 0, 0);
                    modelScale = 1.0f;
                    System.out.println("Сброс (Backspace)");
                    break;
            }

            // Перерисовываем сцену
            drawStaticScene(canvas.getGraphicsContext2D());
        });
    }

    private void drawStaticScene(GraphicsContext gc) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // 1. Очищаем экран
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, width, height);

        // 2. Если модель ЗАГРУЖЕНА - показываем информацию
        if (mesh != null) {
            drawModelInfo(gc, width, height);
        }
        // 3. Если модель НЕ загружена - показываем синий куб
        else {
            drawBlueCube(gc, width, height);
        }
    }

    // Метод для рисования синего куба (только когда модель не загружена)
    private void drawBlueCube(GraphicsContext gc, double width, double height) {
        // Координаты для куба
        double centerX = width / 2;
        double centerY = height / 2;
        double size = 200; // Фиксированный размер

        // Синий квадрат
        gc.setFill(Color.rgb(100, 150, 255, 0.7));
        gc.fillRect(centerX - size/2, centerY - size/2, size, size);

        // Обводка если выбран wireframe
        if (drawWireframeCheckBox.isSelected()) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            gc.strokeRect(centerX - size/2, centerY - size/2, size, size);
        }
    }

    // Метод для отображения информации о загруженной модели
    private void drawModelInfo(GraphicsContext gc, double width, double height) {
        // 1. Зелёная надпись "УСПЕХ"
        gc.setFill(Color.GREEN);
        gc.fillText("✓ МОДЕЛЬ ЗАГРУЖЕНА!", 20, 30);

        // 2. Информация о модели
        gc.setFill(Color.BLACK);
        gc.fillText("Вершин: " + mesh.vertices.size(), 20, 50);
        gc.fillText("Полигонов: " + mesh.polygons.size(), 20, 70);

        // 3. Проверяем, маленькая ли модель (< 1000 полигонов)
        if (mesh.polygons.size() <= 1000) {
            gc.setFill(Color.DARKBLUE);
            gc.fillText("Режим: 3D РЕНДЕРИНГ (полигоны)", 20, 90);

            // ★ ВЫЗЫВАЕМ НАСТОЯЩИЙ 3D РЕНДЕРИНГ ★
            render3DModel(gc, width, height);
        }
        // 4. Для средних моделей - только каркас
        else if (mesh.polygons.size() <= 5000) {
            gc.setFill(Color.ORANGE);
            gc.fillText("Режим: КАРКАС (только линии)", 20, 90);
            gc.fillText("Полигонов много, рисую линии", 20, 110);

            // Упрощенный режим - только линии
            renderWireframeOnly(gc, width, height);
        }
        // 5. Для очень больших моделей - только точки
        else {
            gc.setFill(Color.ORANGE);
            gc.fillText("Режим: ТОЧКИ (упрощённо)", 20, 90);
            gc.fillText("Очень большая модель", 20, 110);

            // Только точки
            renderPointsOnly(gc, width, height);
        }
    }
    // Метод для UI текста
    private void drawUI(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.BLACK);
        gc.fillText("3D Viewer - Working!", 20, height - 60);
        gc.fillText("File → Load Model to import OBJ", 20, height - 40);
        gc.fillText("Checkboxes change visualization", 20, height - 20);
    }


    // Метод для настоящего 3D рендеринга полигонов
    private void render3DModel(GraphicsContext gc, double width, double height) {
        if (mesh == null || mesh.polygons.isEmpty()) return;

        System.out.println("Rendering 3D model with " + mesh.polygons.size() + " polygons");

        // ★ ПРИМЕНЯЕМ ТРАНСФОРМАЦИИ К ВЕРШИНАМ ★
        java.util.List<Vector3f> transformedVertices = new java.util.ArrayList<>();

        for (Vector3f original : mesh.vertices) {
            // 1. Масштаб
            Vector3f scaled = new Vector3f(
                    original.getX() * modelScale,
                    original.getY() * modelScale,
                    original.getZ() * modelScale
            );

            // 2. Вращение вокруг осей
            Vector3f rotated = rotateVertex(scaled, modelRotation);

            // 3. Позиция (смещение)
            Vector3f positioned = new Vector3f(
                    rotated.getX() + modelPosition.getX(),
                    rotated.getY() + modelPosition.getY(),
                    rotated.getZ() + modelPosition.getZ()
            );

            transformedVertices.add(positioned);
        }

        // Настройки камеры
        float cameraZ = 5.0f;
        float scale = 200.0f;

        // Рисуем полигоны с трансформированными вершинами
        for (com.cgvsu.model.Polygon polygon : mesh.polygons) {
            java.util.ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            // Рисуем только треугольники (после триангуляции)
            if (vertexIndices.size() >= 3) {
                // Берем первые 3 вершины для треугольника
                int idx1 = vertexIndices.get(0);
                int idx2 = vertexIndices.get(1);
                int idx3 = vertexIndices.get(2);

                if (idx1 < transformedVertices.size() &&
                        idx2 < transformedVertices.size() &&
                        idx3 < transformedVertices.size()) {

                    Vector3f v1 = transformedVertices.get(idx1);
                    Vector3f v2 = transformedVertices.get(idx2);
                    Vector3f v3 = transformedVertices.get(idx3);

                    // Простая перспективная проекция
                    double x1 = width/2 + (v1.getX() / (v1.getZ() + cameraZ)) * scale;
                    double y1 = height/2 - (v1.getY() / (v1.getZ() + cameraZ)) * scale;

                    double x2 = width/2 + (v2.getX() / (v2.getZ() + cameraZ)) * scale;
                    double y2 = height/2 - (v2.getY() / (v2.getZ() + cameraZ)) * scale;

                    double x3 = width/2 + (v3.getX() / (v3.getZ() + cameraZ)) * scale;
                    double y3 = height/2 - (v3.getY() / (v3.getZ() + cameraZ)) * scale;

                    // Проверяем, не вышли ли координаты за пределы экрана
                    if (isTriangleVisible(x1, y1, x2, y2, x3, y3, width, height)) {

                        // Если включен wireframe - рисуем линии
                        if (drawWireframeCheckBox.isSelected()) {
                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(1);
                            gc.strokeLine(x1, y1, x2, y2);
                            gc.strokeLine(x2, y2, x3, y3);
                            gc.strokeLine(x3, y3, x1, y1);
                        }
                        // Иначе рисуем залитые полигоны
                        else {
                            // Цвет в зависимости от нормали (простое освещение)
                            Vector3f normal = computeNormal(v1, v2, v3);
                            double brightness = Math.max(0.3, Math.abs(normal.getZ()));

                            // Если включено освещение - используем вычисленную яркость
                            if (useLightingCheckBox.isSelected()) {
                                gc.setFill(Color.rgb(
                                        (int)(100 * brightness),
                                        (int)(150 * brightness),
                                        (int)(255 * brightness),
                                        0.8
                                ));
                            } else {
                                // Без освещения - просто голубой цвет
                                gc.setFill(Color.rgb(100, 150, 255, 0.8));
                            }

                            // Рисуем треугольник
                            double[] xPoints = {x1, x2, x3};
                            double[] yPoints = {y1, y2, y3};
                            gc.fillPolygon(xPoints, yPoints, 3);

                            // Если включена текстура - добавляем узор (упрощённая имитация)
                            if (useTextureCheckBox.isSelected()) {
                                gc.setStroke(Color.rgb(255, 200, 100, 0.5));
                                gc.setLineWidth(0.5);

                                // Рисуем простой узор внутри треугольника
                                gc.strokeLine((x1+x2)/2, (y1+y2)/2, (x2+x3)/2, (y2+y3)/2);
                                gc.strokeLine((x2+x3)/2, (y2+y3)/2, (x3+x1)/2, (y3+y1)/2);
                                gc.strokeLine((x3+x1)/2, (y3+y1)/2, (x1+x2)/2, (y1+y2)/2);
                            } else {
                                // Тонкая обводка
                                gc.setStroke(Color.rgb(0, 0, 100, 0.3));
                                gc.setLineWidth(0.5);
                                gc.strokePolygon(xPoints, yPoints, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    // Вспомогательный метод для проверки видимости треугольника
    private boolean isTriangleVisible(double x1, double y1, double x2, double y2,
                                      double x3, double y3, double width, double height) {
        // Проверяем, находится ли хотя бы одна вершина в пределах экрана
        return (x1 >= -width && x1 <= width*2 && y1 >= -height && y1 <= height*2) ||
                (x2 >= -width && x2 <= width*2 && y2 >= -height && y2 <= height*2) ||
                (x3 >= -width && x3 <= width*2 && y3 >= -height && y3 <= height*2);
    }


    // Метод для вращения вершины вокруг осей X, Y, Z
    private Vector3f rotateVertex(Vector3f vertex, Vector3f rotation) {
        float x = vertex.getX();
        float y = vertex.getY();
        float z = vertex.getZ();

        // Вращение вокруг оси X
        float cosX = (float)Math.cos(rotation.getX());
        float sinX = (float)Math.sin(rotation.getX());
        float y1 = y * cosX - z * sinX;
        float z1 = y * sinX + z * cosX;

        // Вращение вокруг оси Y
        float cosY = (float)Math.cos(rotation.getY());
        float sinY = (float)Math.sin(rotation.getY());
        float x2 = x * cosY + z1 * sinY;
        float z2 = -x * sinY + z1 * cosY;

        // Вращение вокруг оси Z
        float cosZ = (float)Math.cos(rotation.getZ());
        float sinZ = (float)Math.sin(rotation.getZ());
        float x3 = x2 * cosZ - y1 * sinZ;
        float y3 = x2 * sinZ + y1 * cosZ;

        return new Vector3f(x3, y3, z2);
    }

    // Рендеринг только каркаса (линий)
    // Рендеринг только каркаса (линий) с трансформациями
    private void renderWireframeOnly(GraphicsContext gc, double width, double height) {
        if (mesh == null || mesh.polygons.isEmpty()) return;

        // Применяем трансформации к вершинам
        java.util.List<Vector3f> transformedVertices = new java.util.ArrayList<>();

        for (Vector3f original : mesh.vertices) {
            Vector3f scaled = new Vector3f(
                    original.getX() * modelScale,
                    original.getY() * modelScale,
                    original.getZ() * modelScale
            );
            Vector3f rotated = rotateVertex(scaled, modelRotation);
            Vector3f positioned = new Vector3f(
                    rotated.getX() + modelPosition.getX(),
                    rotated.getY() + modelPosition.getY(),
                    rotated.getZ() + modelPosition.getZ()
            );
            transformedVertices.add(positioned);
        }

        float cameraZ = 5.0f;
        float scale = 200.0f;

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(0.5f);

        // Рисуем только каждую 10-ю грань для производительности
        for (int i = 0; i < mesh.polygons.size(); i += 10) {
            com.cgvsu.model.Polygon polygon = mesh.polygons.get(i);
            java.util.ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                int idx1 = vertexIndices.get(0);
                int idx2 = vertexIndices.get(1);
                int idx3 = vertexIndices.get(2);

                if (idx1 < transformedVertices.size() &&
                        idx2 < transformedVertices.size() &&
                        idx3 < transformedVertices.size()) {

                    Vector3f v1 = transformedVertices.get(idx1);
                    Vector3f v2 = transformedVertices.get(idx2);
                    Vector3f v3 = transformedVertices.get(idx3);

                    double x1 = width/2 + (v1.getX() / (v1.getZ() + cameraZ)) * scale;
                    double y1 = height/2 - (v1.getY() / (v1.getZ() + cameraZ)) * scale;

                    double x2 = width/2 + (v2.getX() / (v2.getZ() + cameraZ)) * scale;
                    double y2 = height/2 - (v2.getY() / (v2.getZ() + cameraZ)) * scale;

                    double x3 = width/2 + (v3.getX() / (v3.getZ() + cameraZ)) * scale;
                    double y3 = height/2 - (v3.getY() / (v3.getZ() + cameraZ)) * scale;

                    // Рисуем треугольник линиями
                    gc.strokeLine(x1, y1, x2, y2);
                    gc.strokeLine(x2, y2, x3, y3);
                    gc.strokeLine(x3, y3, x1, y1);
                }
            }
        }
    }

    // Рендеринг только точек с трансформациями
    private void renderPointsOnly(GraphicsContext gc, double width, double height) {
        if (mesh == null || mesh.vertices.isEmpty()) return;

        // Применяем трансформации к вершинам
        java.util.List<Vector3f> transformedVertices = new java.util.ArrayList<>();

        for (Vector3f original : mesh.vertices) {
            Vector3f scaled = new Vector3f(
                    original.getX() * modelScale,
                    original.getY() * modelScale,
                    original.getZ() * modelScale
            );
            Vector3f rotated = rotateVertex(scaled, modelRotation);
            Vector3f positioned = new Vector3f(
                    rotated.getX() + modelPosition.getX(),
                    rotated.getY() + modelPosition.getY(),
                    rotated.getZ() + modelPosition.getZ()
            );
            transformedVertices.add(positioned);
        }

        float cameraZ = 5.0f;
        float scale = 200.0f;

        gc.setFill(Color.BLUE);

        // Рисуем только каждую 100-ю вершину
        for (int i = 0; i < transformedVertices.size(); i += 100) {
            Vector3f vertex = transformedVertices.get(i);
            double x = width/2 + (vertex.getX() / (vertex.getZ() + cameraZ)) * scale;
            double y = height/2 - (vertex.getY() / (vertex.getZ() + cameraZ)) * scale;

            gc.fillOval(x - 1, y - 1, 2, 2);
        }
    }

    // Вспомогательный метод для вычисления нормали треугольника
    private Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = v2.subtract(v1);
        Vector3f edge2 = v3.subtract(v1);
        return edge1.cross(edge2).normalize();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            // Предобработка модели: триангуляция и пересчет нормалей
            if (mesh != null) {
                ModelProcessor.preprocess(mesh);
                mesh.setOriginalVertices(); // ВАЖНО: сохраняем оригинальные вершины для сброса трансформаций
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawStaticScene(gc);
        System.out.println("Model loaded and scene redrawn!");
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void onDrawWireframeCheckBoxChange() {
        renderingModes.setDrawWireframe(drawWireframeCheckBox.isSelected());
    }

    @FXML
    public void onUseTextureCheckBoxChange() {
        renderingModes.setUseTexture(useTextureCheckBox.isSelected());
    }

    @FXML
    public void onUseLightingCheckBoxChange() {
        renderingModes.setUseLighting(useLightingCheckBox.isSelected());
    }
}