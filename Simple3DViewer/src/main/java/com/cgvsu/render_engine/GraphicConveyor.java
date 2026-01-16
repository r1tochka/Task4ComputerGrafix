package com.cgvsu.render_engine;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate(Vector3f translation, Vector3f rotation, Vector3f scale) {
        // Матрица масштабирования
        float[][] scaleMatrix = {
                {scale.getX(), 0, 0, 0},
                {0, scale.getY(), 0, 0},
                {0, 0, scale.getZ(), 0},
                {0, 0, 0, 1}
        };

        // Матрицы вращения
        float cosX = (float) Math.cos(rotation.getX());
        float sinX = (float) Math.sin(rotation.getX());
        float[][] rotateX = {
                {1, 0, 0, 0},
                {0, cosX, -sinX, 0},
                {0, sinX, cosX, 0},
                {0, 0, 0, 1}
        };

        float cosY = (float) Math.cos(rotation.getY());
        float sinY = (float) Math.sin(rotation.getY());
        float[][] rotateY = {
                {cosY, 0, sinY, 0},
                {0, 1, 0, 0},
                {-sinY, 0, cosY, 0},
                {0, 0, 0, 1}
        };

        float cosZ = (float) Math.cos(rotation.getZ());
        float sinZ = (float) Math.sin(rotation.getZ());
        float[][] rotateZ = {
                {cosZ, -sinZ, 0, 0},
                {sinZ, cosZ, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };

        // Матрица трансляции
        float[][] translateMatrix = {
                {1, 0, 0, translation.getX()},
                {0, 1, 0, translation.getY()},
                {0, 0, 1, translation.getZ()},
                {0, 0, 0, 1}
        };

        // Композиция: T * RZ * RY * RX * S
        Matrix4f scaleMat = new Matrix4f(scaleMatrix);
        Matrix4f rotXMat = new Matrix4f(rotateX);
        Matrix4f rotYMat = new Matrix4f(rotateY);
        Matrix4f rotZMat = new Matrix4f(rotateZ);
        Matrix4f transMat = new Matrix4f(translateMatrix);

        return transMat.multiply(rotZMat).multiply(rotYMat).multiply(rotXMat).multiply(scaleMat);
    }

    public static Matrix4f rotateScaleTranslate() {
        return rotateScaleTranslate(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(1, 1, 1)
        );
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.subtract(eye);
        Vector3f resultX = up.cross(resultZ);
        Vector3f resultY = resultZ.cross(resultX);

        resultX = resultX.normalize();
        resultY = resultY.normalize();
        resultZ = resultZ.normalize();

        float[][] matrix = new float[][]{
                {resultX.getX(), resultY.getX(), resultZ.getX(), 0},
                {resultX.getY(), resultY.getY(), resultZ.getY(), 0},
                {resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0},
                {-resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        float[][] matrix = new float[][]{
                {tangentMinusOnDegree / aspectRatio, 0, 0, 0},
                {0, tangentMinusOnDegree, 0, 0},
                {0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), 1.0F},
                {0, 0, 2 * (nearPlane * farPlane) / (nearPlane - farPlane), 0}
        };
        return new Matrix4f(matrix);
    }

    // УДАЛЕНО: multiplyMatrix4ByVector3 и multiplyMatrix4ByVector4
    // Теперь используется прямое умножение: matrix.multiply(vector)

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.getX() * width + width / 2.0F, -vertex.getY() * height + height / 2.0F);
    }
}
