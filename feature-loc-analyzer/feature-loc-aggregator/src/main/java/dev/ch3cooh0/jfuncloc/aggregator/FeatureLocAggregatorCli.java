package dev.ch3cooh0.jfuncloc.aggregator;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;

/**
 * 機能別LOC集計のコマンドラインインターフェース。
 * 
 * <p>このクラスはJFuncLOCツールの機能別LOC集計機能をコマンドラインから実行するための
 * エントリーポイントを提供します。
 * 
 * <h3>必須パラメータ</h3>
 * <ul>
 *   <li><strong>--source</strong>: Java ソースコードのディレクトリパス</li>
 *   <li><strong>--jar</strong>: コールグラフ生成用のJARファイルまたはクラスファイルディレクトリ</li>
 *   <li><strong>--entry</strong>: 機能定義ファイル（YAML/JSON形式）</li>
 * </ul>
 * 
 * <h3>オプションパラメータ</h3>
 * <ul>
 *   <li><strong>--output</strong>: 出力CSVファイルパス（デフォルト: feature-loc.csv）</li>
 * </ul>
 * 
 * <h3>使用例</h3>
 * <pre>
 * java -jar feature-loc-aggregator.jar \
 *   --source /path/to/source \
 *   --jar /path/to/application.jar \
 *   --entry features.yaml \
 *   --output result.csv
 * </pre>
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
@Command(name = "feature-loc-aggregator", 
         mixinStandardHelpOptions = true,
         description = "機能別LOC集計ツール - エントリーポイントから到達可能な関数・クラスのLOCを機能別に集計します")
public class FeatureLocAggregatorCli implements Runnable {
    @Option(names = "--source", required = true, description = "ソースコードディレクトリ")
    private String source;

    @Option(names = "--jar", required = true, description = "JARファイルまたはクラスファイルディレクトリ")
    private String jarPath;

    @Option(names = "--entry", required = true, description = "エントリポイント定義ファイル")
    private File entry;

    @Option(names = "--output", defaultValue = "feature-loc.csv", description = "出力CSVファイル")
    private File output;

    @Override
    public void run() {
        FeatureLocAggregator aggregator = new FeatureLocAggregator();
        try {
            aggregator.exportToCsv(source, jarPath, entry, output.getAbsolutePath());
            System.out.println("機能別LOC集計結果を " + output.getAbsolutePath() + " に出力しました。");
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new CommandLine(new FeatureLocAggregatorCli()).execute(args);
    }
} 