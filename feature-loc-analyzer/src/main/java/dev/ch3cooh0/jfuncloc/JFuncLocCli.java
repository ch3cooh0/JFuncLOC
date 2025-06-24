package dev.ch3cooh0.jfuncloc;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * JFuncLOC統合CLIアプリケーションのメインクラス。
 * 各モジュールのコマンドを統合して提供します。
 */
@Command(name = "jfuncloc", mixinStandardHelpOptions = true,
         subcommands = {
             dev.ch3cooh0.jfuncloc.callgraph.Main.class,
             dev.ch3cooh0.jfuncloc.loc.Main.class,
             dev.ch3cooh0.jfuncloc.entry.Main.class,
             dev.ch3cooh0.jfuncloc.aggregator.FeatureLocAggregatorCli.class
         },
         description = "Java関数行数分析ツール - コールグラフ生成、行数カウント、エントリーポイント検出、集計機能を提供します")
public class JFuncLocCli implements Runnable {

    @Override
    public void run() {
        // ヘルプメッセージを表示
        System.out.println("JFuncLOC - Java関数行数分析ツール");
        System.out.println();
        System.out.println("利用可能なコマンド:");
        System.out.println("  callgraph-generator    - コールグラフ生成");
        System.out.println("  function-loc-counter   - 関数行数カウント");
        System.out.println("  entrypoint-detector    - エントリーポイント検出");
        System.out.println("  feature-loc-aggregator - 機能行数集計");
        System.out.println();
        System.out.println("詳細な使用方法は各コマンドに --help オプションを付けて実行してください。");
    }

    /**
     * メインプログラムのエントリーポイント。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JFuncLocCli()).execute(args);
        System.exit(exitCode);
    }
} 