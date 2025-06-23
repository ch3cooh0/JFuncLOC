package dev.ch3cooh0.jfuncloc.loc;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionLocCounterTest {
    @Test
    void testEmpty() {
        FunctionLocCounter counter = new FunctionLocCounter();
        Map<String, Integer> result = counter.count("src/test/resources/empty");
        assertTrue(result.isEmpty());
    }
}
