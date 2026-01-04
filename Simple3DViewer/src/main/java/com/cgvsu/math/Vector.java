package com.cgvsu.math;

public interface Vector {

    float length();
    Vector normalize();
    Vector multiply(float scalar);
    Vector divide(float scalar);
    float[] toArray();
}

