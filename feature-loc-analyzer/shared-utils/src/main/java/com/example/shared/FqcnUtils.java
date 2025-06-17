package com.example.shared;

public class FqcnUtils {
    public static String toFqcn(String className, String methodName) {
        return className + "#" + methodName;
    }
}
