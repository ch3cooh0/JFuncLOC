package dev.ch3cooh0.jfuncloc.loc;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 関数の行数カウントツールのメインクラス。
 * このツールは、Javaソースコードから関数ごとの行数を解析し、
 * CSVファイルとして出力します。
 */
@Command(name = "function-loc-counter", mixinStandardHelpOptions = true, 
         description = "Javaソースコードから関数ごとの行数を解析し、CSVファイルとして出力します")
public class Main implements Runnable {
    
    @Option(names = {"-i", "--input"}, required = true, 
            description = "入力パス（Javaソースコードのディレクトリ）")
    private String inputPath;
    
    @Option(names = {"-o", "--output"}, defaultValue = "function-loc.csv", 
            description = "出力ファイルパス（デフォルト: function-loc.csv）")
    private String outputPath;
    
    @Option(names = {"-p", "--package"}, split = ",", 
            description = "対象パッケージ名を指定（カンマ区切りで複数指定可能）")
    private java.util.List<String> targetPackages;

    @Override
    public void run() {
        try {
            System.out.println("関数行数カウントを開始します...");
            validateInputPath(inputPath);
            
            printExecutionInfo();
            
            FunctionLocCounter counter = new FunctionLocCounter();
            Map<String, Integer> result = counter.countFunctionLines(inputPath, targetPackages);
            
            writeFunctionLocToFile(result, outputPath);
            
            printCompletionMessage(outputPath, result.size());
        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 入力パスの存在を検証します。
     *
     * @param inputPath 検証する入力パス
     * @throws IllegalArgumentException 入力パスが存在しない場合
     */
    private void validateInputPath(String inputPath) {
        if (!Files.exists(Paths.get(inputPath))) {
            throw new IllegalArgumentException("エラー: 入力パスが存在しません: " + inputPath);
        }
    }

    /**
     * 実行情報を標準出力に表示します。
     */
    private void printExecutionInfo() {
        System.out.println("入力パス: " + inputPath);
        System.out.println("出力ファイル: " + outputPath);
        if (targetPackages != null && !targetPackages.isEmpty()) {
            System.out.println("対象パッケージ: " + String.join(", ", targetPackages));
        }
    }

    /**
     * 関数行数をCSVファイルに出力します。
     * 出力形式: 関数名,行数
     *
     * @param result 関数行数の結果
     * @param outputPath 出力ファイルのパス
     * @throws IOException ファイルの書き込みに失敗した場合
     */
    private void writeFunctionLocToFile(Map<String, Integer> result, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Function,LineCount\n");
            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }

    /**
     * 完了メッセージを表示します。
     *
     * @param outputPath 出力ファイルのパス
     * @param functionCount 処理した関数の数
     */
    private void printCompletionMessage(String outputPath, int functionCount) {
        System.out.println("関数行数カウントが完了しました。");
        System.out.println("出力ファイル: " + outputPath);
        System.out.println("処理した関数数: " + functionCount);
    }

    /**
     * メインプログラムのエントリーポイント。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
} 