package com.example.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FqcnUtilsTest {
    @Test
    public void testToFqcn() {
        assertEquals("com.example.MyClass#myMethod", FqcnUtils.toFqcn("com.example.MyClass", "myMethod"));
    }
}
