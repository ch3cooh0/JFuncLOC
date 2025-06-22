package dev.ch3cooh0.jfuncloc.callgraph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;
    private Path testInputDir;
    private Path testOutputFile;

    @BeforeEach
    void setUp() throws IOException {
        // テスト用の入力ディレクトリを作成
        testInputDir = tempDir.resolve("test-input");
        Files.createDirectory(testInputDir);
        
        // テスト用の出力ファイルパスを設定
        testOutputFile = tempDir.resolve("test-output.csv");
        
        // System.exitを無効化
        Main.setExitHandler(new NoOpExitHandler());
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 最小限の引数")
    void testParseCommandLineArgsMinimal() {
        String[] args = {"-i", testInputDir.toString()};
        Main.CommandLineArgs result = Main.parseCommandLineArgs(args);
        
        assertEquals(testInputDir.toString(), result.getInputPath());
        assertEquals("callgraph.csv", result.getOutputPath());
        assertNull(result.getTargetPackages());
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - すべての引数")
    void testParseCommandLineArgsFull() {
        String[] args = {
            "-i", testInputDir.toString(),
            "-o", testOutputFile.toString(),
            "-p", "test.package"
        };
        Main.CommandLineArgs result = Main.parseCommandLineArgs(args);
        
        assertEquals(testInputDir.toString(), result.getInputPath());
        assertEquals(testOutputFile.toString(), result.getOutputPath());
        assertNotNull(result.getTargetPackages());
        assertEquals(1, result.getTargetPackages().size());
        assertEquals("test.package", result.getTargetPackages().get(0));
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 複数パッケージ（複数-p指定）")
    void testParseCommandLineArgsMultiplePackages() {
        String[] args = {
            "-i", testInputDir.toString(),
            "-o", testOutputFile.toString(),
            "-p", "test.package1",
            "-p", "test.package2"
        };
        Main.CommandLineArgs result = Main.parseCommandLineArgs(args);
        assertEquals(testInputDir.toString(), result.getInputPath());
        assertEquals(testOutputFile.toString(), result.getOutputPath());
        assertNotNull(result.getTargetPackages());
        assertEquals(2, result.getTargetPackages().size());
        assertTrue(result.getTargetPackages().contains("test.package1"));
        assertTrue(result.getTargetPackages().contains("test.package2"));
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 複数パッケージ（カンマ区切り）")
    void testParseCommandLineArgsCommaSeparatedPackages() {
        String[] args = {
            "-i", testInputDir.toString(),
            "-o", testOutputFile.toString(),
            "-p", "test.package1,test.package2"
        };
        Main.CommandLineArgs result = Main.parseCommandLineArgs(args);
        assertEquals(testInputDir.toString(), result.getInputPath());
        assertEquals(testOutputFile.toString(), result.getOutputPath());
        assertNotNull(result.getTargetPackages());
        assertEquals(2, result.getTargetPackages().size());
        assertTrue(result.getTargetPackages().contains("test.package1"));
        assertTrue(result.getTargetPackages().contains("test.package2"));
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 引数なし")
    void testParseCommandLineArgsEmpty() {
        String[] args = {};
        assertThrows(IllegalArgumentException.class, () -> {
            Main.parseCommandLineArgs(args);
        });
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 入力パスなし")
    void testParseCommandLineArgsNoInput() {
        String[] args = {"-o", testOutputFile.toString()};
        assertThrows(IllegalArgumentException.class, () -> {
            Main.parseCommandLineArgs(args);
        });
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - 不明なオプション")
    void testParseCommandLineArgsUnknownOption() {
        String[] args = {"-i", testInputDir.toString(), "-x", "value"};
        assertThrows(IllegalArgumentException.class, () -> {
            Main.parseCommandLineArgs(args);
        });
    }

    @Test
    @DisplayName("コマンドライン引数の解析 - オプション値なし")
    void testParseCommandLineArgsMissingValue() {
        String[] args = {"-i"};
        assertThrows(IllegalArgumentException.class, () -> {
            Main.parseCommandLineArgs(args);
        });
    }

    @Test
    @DisplayName("入力パスの検証 - 存在するパス")
    void testValidateInputPathExists() {
        assertDoesNotThrow(() -> {
            Main.validateInputPath(testInputDir.toString());
        });
    }

    @Test
    @DisplayName("入力パスの検証 - 存在しないパス")
    void testValidateInputPathNotExists() {
        Path nonExistentPath = tempDir.resolve("non-existent");
        assertThrows(IllegalArgumentException.class, () -> {
            Main.validateInputPath(nonExistentPath.toString());
        });
    }

    @Test
    @DisplayName("コールグラフのファイル出力")
    void testWriteCallGraphToFile() throws IOException {
        // テスト用のコールグラフデータを作成
        Map<String, Set<String>> callRelations = new HashMap<>();
        Set<String> method1Callees = new HashSet<>();
        method1Callees.add("method2");
        method1Callees.add("method3");
        callRelations.put("method1", method1Callees);

        Set<String> method2Callees = new HashSet<>();
        method2Callees.add("method4");
        callRelations.put("method2", method2Callees);

        CallGraphResult result = new CallGraphResult(callRelations);

        // ファイルに出力
        Main.writeCallGraphToFile(result, testOutputFile.toString());

        // 出力されたファイルの内容を検証
        List<String> lines = Files.readAllLines(testOutputFile);
        assertEquals(3, lines.size());
        assertTrue(lines.contains("method1,method2"));
        assertTrue(lines.contains("method1,method3"));
        assertTrue(lines.contains("method2,method4"));
    }

    @Test
    @DisplayName("エラーハンドリング - 存在しないディレクトリへの出力")
    void testErrorHandlingInvalidOutputPath() {
        Path invalidPath = tempDir.resolve("invalid/dir/output.csv");
        Map<String, Set<String>> callRelations = new HashMap<>();
        CallGraphResult result = new CallGraphResult(callRelations);
        
        assertThrows(IOException.class, () -> {
            Main.writeCallGraphToFile(result, invalidPath.toString());
        });
    }
} 