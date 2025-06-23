package dev.ch3cooh0.jfuncloc.entry;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EntrypointDetectorTest {
    @Test
    void testDetectFromFileEmpty() throws Exception {
        EntrypointDetector detector = new EntrypointDetector();
        Map<String, FeatureConfig> result = detector.detectFromFile(new File("src/test/resources/empty.yaml"));
        assertTrue(result.isEmpty());
    }

    @Test
    void testDetectFromFile() throws Exception {
        EntrypointDetector detector = new EntrypointDetector();
        Map<String, FeatureConfig> result = detector.detectFromFile(new File("src/test/resources/entry.yaml"));
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertTrue(result.containsKey("user-management"));
        assertTrue(result.containsKey("order-processing"));
        assertTrue(result.containsKey("product-catalog"));
    }
}
