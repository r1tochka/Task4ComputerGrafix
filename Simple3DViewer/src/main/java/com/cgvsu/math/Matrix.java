package com.cgvsu.math;

public interface Matrix {
    Matrix add(Matrix other);
    Matrix subtract(Matrix other); //вычитание
    Matrix multiply(Matrix other);
    Matrix transpose();
}
