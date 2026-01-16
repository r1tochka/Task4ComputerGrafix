package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class Lighting {
    private Vector3f lightDirection;
    private final Color ambientColor;
    private final Color diffuseColor;
    private final float ambientIntensity;
    private final float diffuseIntensity;

    public Lighting(Vector3f cameraPosition, Vector3f cameraTarget,
                    float ambientIntensity, float diffuseIntensity) {
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
        this.ambientIntensity = Math.max(0.0f, Math.min(1.0f, ambientIntensity));
        this.diffuseIntensity = Math.max(0.0f, Math.min(1.0f, diffuseIntensity));
        this.ambientColor = Color.WHITE;
        this.diffuseColor = Color.WHITE;
    }

    public Lighting(Vector3f cameraPosition, Vector3f cameraTarget,
                    Color ambientColor, Color diffuseColor,
                    float ambientIntensity, float diffuseIntensity) {
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
        this.ambientIntensity = Math.max(0.0f, Math.min(1.0f, ambientIntensity));
        this.diffuseIntensity = Math.max(0.0f, Math.min(1.0f, diffuseIntensity));
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
    }

    public void update(Vector3f cameraPosition, Vector3f cameraTarget, Matrix4f viewMatrix) {
        // Обновляем направление света от камеры
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
    }

    /**
     * ИСПРАВЛЕНО: правильная модель Ламберта (диффузное освещение)
     * @param normal нормаль поверхности (в мировых координатах)
     * @param vertexPosition позиция вершины (в мировых координатах)
     * @param cameraPosition позиция камеры (источника света)
     * @return интенсивность освещения [0.0, 1.0]
     */
    public float computeLightingIntensity(Vector3f normal, Vector3f vertexPosition, Vector3f cameraPosition) {
        // Направление от вершины к источнику света (камере)
        Vector3f lightDir = cameraPosition.subtract(vertexPosition).normalize();
        Vector3f n = normal.normalize();

        // Модель Ламберта: I = max(0, N · L)
        float diffuse = n.dot(lightDir);

        // Отсекаем отрицательные значения (поверхность отвернута от света)
        return Math.max(0.0f, Math.min(1.0f, diffuse));
    }

    public Color shadeColor(Color baseColor, float intensity) {
        // Ambient компонента (постоянная подсветка)
        double ambientR = baseColor.getRed() * ambientIntensity;
        double ambientG = baseColor.getGreen() * ambientIntensity;
        double ambientB = baseColor.getBlue() * ambientIntensity;

        // Diffuse компонента (зависит от угла к свету)
        double diffuseR = baseColor.getRed() * diffuseIntensity * intensity;
        double diffuseG = baseColor.getGreen() * diffuseIntensity * intensity;
        double diffuseB = baseColor.getBlue() * diffuseIntensity * intensity;

        // Итоговый цвет = ambient + diffuse
        double r = ambientR + diffuseR;
        double g = ambientG + diffuseG;
        double b = ambientB + diffuseB;

        // Clamp в [0, 1]
        r = Math.max(0.0, Math.min(1.0, r));
        g = Math.max(0.0, Math.min(1.0, g));
        b = Math.max(0.0, Math.min(1.0, b));

        return new Color(r, g, b, baseColor.getOpacity());
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }
}
