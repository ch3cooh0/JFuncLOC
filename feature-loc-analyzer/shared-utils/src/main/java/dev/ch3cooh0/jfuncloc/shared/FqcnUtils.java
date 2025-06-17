package dev.ch3cooh0.jfuncloc.shared;

public class FqcnUtils {
    public static String toFqcn(String className, String methodName) {
        return className + "#" + methodName;
    }
} 