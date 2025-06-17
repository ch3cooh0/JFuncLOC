package dev.ch3cooh0.jfuncloc.entry;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntrypointDetectorTest {
    @Test
    void testDetectFromFileEmpty() throws Exception {
        EntrypointDetector detector = new EntrypointDetector();
        Map<String, Set<String>> result = detector.detectFromFile(new File("src/test/resources/entry.yaml"));
        assertTrue(result.isEmpty());
    }
}
