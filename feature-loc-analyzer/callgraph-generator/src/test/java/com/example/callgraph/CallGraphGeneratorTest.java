package com.example.callgraph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CallGraphGeneratorTest {
    @Test
    void testFqcnHelper() {
        String fqcn = CallGraphGenerator.fqcn("com.example.Foo", "bar");
        assertEquals("com.example.Foo#bar", fqcn);
    }
}
