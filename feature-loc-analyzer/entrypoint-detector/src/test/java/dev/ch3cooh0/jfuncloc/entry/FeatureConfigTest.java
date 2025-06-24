package dev.ch3cooh0.jfuncloc.entry;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeatureConfigTest {

    @Test
    void testFeatureConfigCreation() {
        List<String> entryPoints = Arrays.asList(
            "com.example.UserController.createUser",
            "com.example.UserController.updateUser"
        );
        List<String> packages = Arrays.asList("com.example.user", "com.example.auth");
        
        FeatureConfig config = new FeatureConfig(
            "ユーザー管理機能",
            "ユーザーの作成、更新、削除を行う機能",
            entryPoints,
            packages
        );
        
        assertEquals("ユーザー管理機能", config.getName());
        assertEquals("ユーザーの作成、更新、削除を行う機能", config.getDescription());
        assertEquals(2, config.getEntryPoints().size());
        assertEquals(2, config.getPackages().size());
        assertTrue(config.getEntryPoints().contains("com.example.UserController.createUser"));
        assertTrue(config.getPackages().contains("com.example.user"));
    }

    @Test
    void testDefaultConstructor() {
        FeatureConfig config = new FeatureConfig();
        assertNull(config.getName());
        assertNull(config.getDescription());
        assertNull(config.getEntryPoints());
        assertNull(config.getPackages());
    }

    @Test
    void testSetters() {
        FeatureConfig config = new FeatureConfig();
        
        config.setName("テスト機能");
        config.setDescription("テスト用の機能");
        config.setEntryPoints(Arrays.asList("com.test.TestController.test"));
        config.setPackages(Arrays.asList("com.test"));
        
        assertEquals("テスト機能", config.getName());
        assertEquals("テスト用の機能", config.getDescription());
        assertEquals(1, config.getEntryPoints().size());
        assertEquals(1, config.getPackages().size());
    }
}