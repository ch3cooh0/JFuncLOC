package dev.ch3cooh0.jfuncloc.entry;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * エントリーポイント検出ツールのメインクラス。
 * このツールは、Javaソースコードからエントリーポイントを検出し、
 * YAMLファイルとして出力します。
 */
@Command(name = "entrypoint-detector", mixinStandardHelpOptions = true, 
         description = "Javaソースコードからエントリーポイントを検出し、YAMLファイルとして出力します")
public class Main implements Runnable {
    
    @Option(names = {"-i", "--input"}, required = true, 
            description = "入力パス（Javaソースコードのディレクトリ）")
    private String inputPath;
    
    @Option(names = {"-o", "--output"}, defaultValue = "entrypoints.yaml", 
            description = "出力ファイルパス（デフォルト: entrypoints.yaml）")
    private String outputPath;
    
    @Option(names = {"-p", "--package"}, split = ",", 
            description = "対象パッケージ名を指定（カンマ区切りで複数指定可能）")
    private java.util.List<String> targetPackages;
    
    @Option(names = {"-f", "--format"}, defaultValue = "yaml", 
            description = "出力形式（yaml または json、デフォルト: yaml）")
    private String outputFormat;

    @Override
    public void run() {
        try {
            System.out.println("エントリーポイント検出を開始します...");
            validateInputPath(inputPath);
            validateOutputFormat(outputFormat);
            
            printExecutionInfo();
            
            EntrypointDetector detector = new EntrypointDetector();
            List<EntryPointInfo> entryPoints = detector.detectEntryPoints(inputPath, targetPackages);
            
            writeEntryPointsToFile(entryPoints, outputPath, outputFormat);
            
            printCompletionMessage(outputPath, entryPoints.size());
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
     * 出力形式を検証します。
     *
     * @param outputFormat 検証する出力形式
     * @throws IllegalArgumentException 出力形式が不正な場合
     */
    private void validateOutputFormat(String outputFormat) {
        if (!outputFormat.equalsIgnoreCase("yaml") && !outputFormat.equalsIgnoreCase("json")) {
            throw new IllegalArgumentException("エラー: サポートされていない出力形式です: " + outputFormat + " (yaml または json を指定してください)");
        }
    }

    /**
     * 実行情報を標準出力に表示します。
     */
    private void printExecutionInfo() {
        System.out.println("入力パス: " + inputPath);
        System.out.println("出力ファイル: " + outputPath);
        System.out.println("出力形式: " + outputFormat);
        if (targetPackages != null && !targetPackages.isEmpty()) {
            System.out.println("対象パッケージ: " + String.join(", ", targetPackages));
        }
    }

    /**
     * エントリーポイントをファイルに出力します。
     *
     * @param entryPoints エントリーポイントのリスト
     * @param outputPath 出力ファイルのパス
     * @param outputFormat 出力形式
     * @throws IOException ファイルの書き込みに失敗した場合
     */
    private void writeEntryPointsToFile(List<EntryPointInfo> entryPoints, String outputPath, String outputFormat) throws IOException {
        if (outputFormat.equalsIgnoreCase("yaml")) {
            writeEntryPointsAsYaml(entryPoints, outputPath);
        } else {
            writeEntryPointsAsJson(entryPoints, outputPath);
        }
    }

    /**
     * エントリーポイントをYAML形式で出力します。
     *
     * @param entryPoints エントリーポイントのリスト
     * @param outputPath 出力ファイルのパス
     * @throws IOException ファイルの書き込みに失敗した場合
     */
    private void writeEntryPointsAsYaml(List<EntryPointInfo> entryPoints, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("entrypoints:\n");
            for (EntryPointInfo entryPoint : entryPoints) {
                writer.write("  - name: " + entryPoint.getName() + "\n");
                writer.write("    class: " + entryPoint.getClassName() + "\n");
                writer.write("    method: " + entryPoint.getMethodName() + "\n");
                if (entryPoint.getDescription() != null && !entryPoint.getDescription().isEmpty()) {
                    writer.write("    description: " + entryPoint.getDescription() + "\n");
                }
            }
        }
    }

    /**
     * エントリーポイントをJSON形式で出力します。
     *
     * @param entryPoints エントリーポイントのリスト
     * @param outputPath 出力ファイルのパス
     * @throws IOException ファイルの書き込みに失敗した場合
     */
    private void writeEntryPointsAsJson(List<EntryPointInfo> entryPoints, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("{\n");
            writer.write("  \"entrypoints\": [\n");
            for (int i = 0; i < entryPoints.size(); i++) {
                EntryPointInfo entryPoint = entryPoints.get(i);
                writer.write("    {\n");
                writer.write("      \"name\": \"" + entryPoint.getName() + "\",\n");
                writer.write("      \"class\": \"" + entryPoint.getClassName() + "\",\n");
                writer.write("      \"method\": \"" + entryPoint.getMethodName() + "\"");
                if (entryPoint.getDescription() != null && !entryPoint.getDescription().isEmpty()) {
                    writer.write(",\n      \"description\": \"" + entryPoint.getDescription() + "\"");
                }
                writer.write("\n    }");
                if (i < entryPoints.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("  ]\n");
            writer.write("}\n");
        }
    }

    /**
     * 完了メッセージを表示します。
     *
     * @param outputPath 出力ファイルのパス
     * @param entryPointCount 検出したエントリーポイントの数
     */
    private void printCompletionMessage(String outputPath, int entryPointCount) {
        System.out.println("エントリーポイント検出が完了しました。");
        System.out.println("出力ファイル: " + outputPath);
        System.out.println("検出したエントリーポイント数: " + entryPointCount);
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