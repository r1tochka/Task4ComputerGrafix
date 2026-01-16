package com.cgvsu.math.interfaces;

public interface Vector<T extends Vector<T>> {
    T add(T other);
    T subtract(T other);
    T multiply(float scalar);
    T divide(float scalar);
    float length();
    T normalize();
    float dot(T other);
    float[] toArray();
    
    boolean equals(Object obj);
    int hashCode();
    String toString();
}
