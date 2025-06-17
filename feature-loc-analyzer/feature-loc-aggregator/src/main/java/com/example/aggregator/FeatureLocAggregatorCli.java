package com.example.aggregator;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Command(name = "feature-loc-aggregator", mixinStandardHelpOptions = true)
public class FeatureLocAggregatorCli implements Runnable {
    @Option(names = "--source", required = true, description = "ソースコードディレクトリ")
    private String source;

    @Option(names = "--entry", required = true, description = "エントリポイント定義ファイル")
    private File entry;

    @Option(names = "--output", defaultValue = "feature-loc.csv", description = "出力CSVファイル")
    private File output;

    @Override
    public void run() {
        FeatureLocAggregator aggregator = new FeatureLocAggregator();
        try {
            List<String[]> result = aggregator.aggregate(source, entry);
            try (FileWriter fw = new FileWriter(output)) {
                fw.write("Feature,TotalLOC,EntryFunctionCount,ReachableFunctionCount\n");
                for (String[] row : result) {
                    fw.write(String.join(",", row));
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new CommandLine(new FeatureLocAggregatorCli()).execute(args);
    }
}
