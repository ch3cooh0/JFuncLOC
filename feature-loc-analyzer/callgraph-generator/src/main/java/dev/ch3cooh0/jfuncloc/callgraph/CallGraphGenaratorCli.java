package dev.ch3cooh0.jfuncloc.callgraph;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * コールグラフ生成ツールのメインクラス。
 * Javaのバイトコードからメソッド間の呼び出し関係を解析し、CSVファイルとして出力します。
 */
@Command(name = "callgraph-generator", mixinStandardHelpOptions = true,
         description = "Javaバイトコードからコールグラフを生成し、CSVファイルとして出力します")
public class CallGraphGenaratorCli implements Runnable {

    @Option(names = {"-i", "--input"}, required = true,
            description = "入力パス（.jarまたはclassファイルのディレクトリ）")
    private String inputPath;

    @Option(names = {"-o", "--output"}, defaultValue = "callgraph.csv",
            description = "出力ファイルパス（デフォルト: callgraph.csv）")
    private String outputPath;

    @Option(names = {"-p", "--package"}, split = ",",
            description = "対象パッケージ名を指定（カンマ区切りで複数指定可能）")
    private List<String> targetPackages;

    protected CallGraphGenerator generator;
    private final ExitHandler exitHandler;

    public CallGraphGenaratorCli() {
        this(new CallGraphGenerator(), new SystemExitHandler());
    }

    public CallGraphGenaratorCli(CallGraphGenerator generator, ExitHandler exitHandler) {
        this.generator = generator;
        this.exitHandler = exitHandler;
    }

    @Override
    public void run() {
        validateInputPath(inputPath);
        printExecutionInfo();

        CallGraphResult result = generateCallGraph();
        writeCallGraphToFile(result, outputPath);
        printCompletionMessage(outputPath);
    }

    private void validateInputPath(String inputPath) {
        if (!Files.exists(Paths.get(inputPath))) {
            throw new IllegalArgumentException("エラー: 入力パスが存在しません: " + inputPath);
        }
    }

    private void printExecutionInfo() {
        System.out.println("コールグラフの生成を開始します...");
        System.out.println("入力パス: " + inputPath);
        System.out.println("出力ファイル: " + outputPath);
        if (targetPackages != null && !targetPackages.isEmpty()) {
            System.out.println("対象パッケージ: " + String.join(", ", targetPackages));
        }
    }

    private CallGraphResult generateCallGraph() {
        this.generator.setTargetPackages(targetPackages);
        return generator.buildCallGraph(inputPath);
    }

    private void writeCallGraphToFile(CallGraphResult result, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            for (Map.Entry<String, Set<String>> entry : result.getCallRelations().entrySet()) {
                String caller = entry.getKey();
                for (String callee : entry.getValue()) {
                    writer.write(caller + "," + callee + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ファイル出力に失敗しました: " + e.getMessage(), e);
        }
    }

    private void printCompletionMessage(String outputPath) {
        System.out.println("コールグラフの生成が完了しました。");
        System.out.println("出力ファイル: " + outputPath);
    }

    /**
     * メインプログラムのエントリーポイント。
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CallGraphGenaratorCli()).execute(args);
        System.exit(exitCode);
    }
}
