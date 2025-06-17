package dev.ch3cooh0.jfuncloc.callgraph;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * コールグラフ生成ツールのメインクラス。
 * このツールは、Javaのバイトコードからメソッド間の呼び出し関係を解析し、
 * CSVファイルとして出力します。
 */
public class Main {
    private static final String DEFAULT_OUTPUT_FILE = "callgraph.csv";
    private static ExitHandler exitHandler = new SystemExitHandler();

    /**
     * テスト用にExitHandlerを差し替えるためのメソッド。
     */
    static void setExitHandler(ExitHandler handler) {
        exitHandler = handler;
    }

    /**
     * メインプログラムのエントリーポイント。
     * コマンドライン引数を解析し、コールグラフの生成と出力を実行します。
     *
     * @param args コマンドライン引数
     *             args[0]: 入力パス（.jarまたはclassファイルのディレクトリ）
     *             args[1]: 出力ファイルパス（オプション、デフォルト: callgraph.csv）
     *             args[2...]: オプション引数
     *             -p, --package: 対象パッケージ名を指定
     */
    public static void main(String[] args) {
        try {
            System.out.println("デバッグ: プログラム開始");
            CommandLineArgs cliArgs = parseCommandLineArgs(args);
            System.out.println("デバッグ: コマンドライン引数解析完了");
            validateInputPath(cliArgs.getInputPath());
            System.out.println("デバッグ: 入力パス検証完了");
            
            printExecutionInfo(cliArgs);
            
            System.out.println("デバッグ: コールグラフ生成開始");
            CallGraphResult result = generateCallGraph(cliArgs);
            System.out.println("デバッグ: コールグラフ生成完了");
            
            System.out.println("デバッグ: ファイル出力開始");
            writeCallGraphToFile(result, cliArgs.getOutputPath());
            System.out.println("デバッグ: ファイル出力完了");
            
            printCompletionMessage(cliArgs.getOutputPath());
        } catch (Exception e) {
            System.err.println("デバッグ: エラー発生");
            e.printStackTrace();
            handleError(e);
        }
    }

    /**
     * コマンドライン引数を保持する内部クラス。
     */
    static class CommandLineArgs {
        private final String inputPath;
        private final String outputPath;
        private final String targetPackage;

        public CommandLineArgs(String inputPath, String outputPath, String targetPackage) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.targetPackage = targetPackage;
        }

        public String getInputPath() { return inputPath; }
        public String getOutputPath() { return outputPath; }
        public String getTargetPackage() { return targetPackage; }
    }

    /**
     * コマンドライン引数を解析し、CommandLineArgsオブジェクトを生成します。
     *
     * @param args コマンドライン引数
     * @return 解析されたコマンドライン引数
     * @throws IllegalArgumentException 引数の形式が不正な場合
     */
    static CommandLineArgs parseCommandLineArgs(String[] args) {
        if (args.length < 1) {
            printUsage();
            exitHandler.exit(1);
            throw new IllegalArgumentException("引数が不足しています"); // 到達しないがテスト用
        }

        String inputPath = args[0];
        String outputPath = args.length > 1 ? args[1] : DEFAULT_OUTPUT_FILE;
        String targetPackage = null;

        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("-p") || args[i].equals("--package")) {
                if (i + 1 < args.length) {
                    targetPackage = args[i + 1];
                    i++;
                } else {
                    throw new IllegalArgumentException("エラー: パッケージ名が指定されていません");
                }
            }
        }

        return new CommandLineArgs(inputPath, outputPath, targetPackage);
    }

    /**
     * 入力パスの存在を検証します。
     *
     * @param inputPath 検証する入力パス
     * @throws IllegalArgumentException 入力パスが存在しない場合
     */
    static void validateInputPath(String inputPath) {
        if (!Files.exists(Paths.get(inputPath))) {
            throw new IllegalArgumentException("エラー: 入力パスが存在しません: " + inputPath);
        }
    }

    /**
     * 実行情報を標準出力に表示します。
     *
     * @param args コマンドライン引数
     */
    static void printExecutionInfo(CommandLineArgs args) {
        System.out.println("コールグラフの生成を開始します...");
        System.out.println("入力パス: " + args.getInputPath());
        System.out.println("出力ファイル: " + args.getOutputPath());
        if (args.getTargetPackage() != null) {
            System.out.println("対象パッケージ: " + args.getTargetPackage());
        }
    }

    /**
     * コールグラフを生成します。
     *
     * @param args コマンドライン引数
     * @return 生成されたコールグラフの結果
     */
    static CallGraphResult generateCallGraph(CommandLineArgs args) {
        CallGraphGenerator generator = new CallGraphGenerator(args.getTargetPackage());
        return generator.buildCallGraph(args.getInputPath());
    }

    /**
     * コールグラフをCSVファイルに出力します。
     * 出力形式: 呼び出し元メソッド,呼び出し先メソッド
     *
     * @param result コールグラフの結果
     * @param outputPath 出力ファイルのパス
     * @throws IOException ファイルの書き込みに失敗した場合
     */
    static void writeCallGraphToFile(CallGraphResult result, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            for (Map.Entry<String, Set<String>> entry : result.getCallRelations().entrySet()) {
                String caller = entry.getKey();
                for (String callee : entry.getValue()) {
                    writer.write(caller + "," + callee + "\n");
                }
            }
        }
    }

    /**
     * 完了メッセージを標準出力に表示します。
     *
     * @param outputPath 出力ファイルのパス
     */
    static void printCompletionMessage(String outputPath) {
        System.out.println("コールグラフの生成が完了しました。");
        System.out.println("出力ファイル: " + outputPath);
    }

    /**
     * エラーを処理し、エラーメッセージを表示してプログラムを終了します。
     *
     * @param e 発生した例外
     */
    static void handleError(Exception e) {
        System.err.println("エラーが発生しました: " + e.getMessage());
        e.printStackTrace();
        exitHandler.exit(1);
    }

    /**
     * 使用方法を標準出力に表示します。
     */
    private static void printUsage() {
        System.out.println("使用方法: java -jar callgraph-generator.jar <入力パス> [出力ファイル] [-p|--package <パッケージ名>]");
        System.out.println("  <入力パス>      : .jarまたはclassファイルのディレクトリ");
        System.out.println("  [出力ファイル]  : 出力するCSVファイルのパス（デフォルト: callgraph.csv）");
        System.out.println("  -p, --package   : 対象とするパッケージ名（オプション）");
    }
} 