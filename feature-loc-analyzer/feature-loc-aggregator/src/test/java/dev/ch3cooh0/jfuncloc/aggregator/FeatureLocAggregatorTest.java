package dev.ch3cooh0.jfuncloc.aggregator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FeatureLocAggregatorTest {
    @Test
    void simple() {
        FeatureLocAggregator agg = new FeatureLocAggregator();
        assertNotNull(agg);
    }
}
