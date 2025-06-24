package dev.ch3cooh0.jfuncloc.callgraph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CallGraphGenaratorCliのCLIテスト。
 */
@ExtendWith(MockitoExtension.class)
class CallGraphGenaratorCliTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    /**
     * --helpオプションでヘルプが表示されることを確認。
     */
    @Test
    @DisplayName("--helpオプションでヘルプが表示される")
    void ヘルプオプションでヘルプが表示される() {
        String[] args = {"--help"};
        int exitCode = new CommandLine(new CallGraphGenaratorCli())
                .setOut(new PrintWriter(out, true))
                .setErr(new PrintWriter(err, true))
                .execute(args);
        String output = out.toString();
        assertTrue(output.contains("Usage: callgraph-generator"), "ヘルプメッセージが含まれるべき");
        assertEquals(0, exitCode, "正常終了コードであるべき");
    }

    /**
     * 存在しない入力パスを指定した場合、エラー終了することを確認。
     */
    @Test
    @DisplayName("入力パスが存在しない場合はエラー終了する")
    void testInputPathNotExist() {
        String[] args = {"-i", "notfound", "-o", "dummy.csv"};
        int exitCode = new CommandLine(new CallGraphGenaratorCli())
                .setOut(new PrintWriter(out, true))
                .setErr(new PrintWriter(err, true))
                .execute(args);
        String error = err.toString();
        System.out.println("Captured error output: [" + error + "]");
        System.out.println("Exit code: " + exitCode);
        assertTrue(error.contains("Error: Input path does not exist"), "エラーメッセージが含まれるべき");
        assertNotEquals(0, exitCode, "異常終了コードであるべき");
    }

    /**
     * 正常系: buildCallGraphが呼ばれ、CSVが正しく出力されることを確認。
     * @param tempDir テスト用一時ディレクトリ（JUnitが自動で用意）
     */
    @Test
    @DisplayName("正常系: buildCallGraphが呼ばれCSVが出力される")
    void 正常系でCSVが出力される(@TempDir Path tempDir) throws Exception {
        // ダミーの入力ファイルを作成
        Path input = tempDir.resolve("dummy.class");
        Files.createFile(input);
        Path output = tempDir.resolve("out.csv");

        // モックのCallGraphGeneratorを用意
        CallGraphGenerator generator = mock(CallGraphGenerator.class);
        CallGraphResult result = new CallGraphResult(Map.of("A.a", Set.of("B.b")));
        when(generator.buildCallGraph(any())).thenReturn(result);

        String[] args = {"-i", input.toString(), "-o", output.toString()};
        int exitCode = new CommandLine(new CallGraphGenaratorCli(generator))
                .setOut(new PrintWriter(out, true))
                .setErr(new PrintWriter(err, true))
                .execute(args);
        assertEquals(0, exitCode, "正常終了コードであるべき");
        String csv = Files.readString(output);
        assertTrue(csv.contains("A.a,B.b"), "CSVにコール関係が出力されるべき");
    }
} 