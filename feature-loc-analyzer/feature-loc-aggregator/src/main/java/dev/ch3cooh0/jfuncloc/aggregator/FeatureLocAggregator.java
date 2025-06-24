package dev.ch3cooh0.jfuncloc.aggregator;

import dev.ch3cooh0.jfuncloc.callgraph.CallGraphGenerator;
import dev.ch3cooh0.jfuncloc.callgraph.CallGraphResult;
import dev.ch3cooh0.jfuncloc.entry.EntrypointDetector;
import dev.ch3cooh0.jfuncloc.entry.FeatureConfig;
import dev.ch3cooh0.jfuncloc.loc.FunctionLocCounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 機能別LOC集計機能を提供するメインクラス。
 * 
 * <p>このクラスは以下の処理フローを実行します：
 * <ol>
 *   <li>YAML/JSONファイルから機能定義とエントリーポイントを読み込み</li>
 *   <li>ソースコードから関数・クラスのLOCを計測</li>
 *   <li>JARファイルからコールグラフを生成</li>
 *   <li>エントリーポイントから到達可能な関数・クラスを特定</li>
 *   <li>機能別にLOCを集計してCSV形式で出力</li>
 * </ol>
 * 
 * <h3>入力要件</h3>
 * <ul>
 *   <li><strong>sourcePath</strong>: Java ソースコードのディレクトリパス</li>
 *   <li><strong>jarPath</strong>: コールグラフ生成用のJARファイルまたはクラスファイルディレクトリ</li>
 *   <li><strong>entryFile</strong>: 機能定義ファイル（YAML/JSON形式）</li>
 * </ul>
 * 
 * <h3>機能定義ファイル形式</h3>
 * <pre>
 * features:
 *   user-management:
 *     name: "ユーザー管理機能"
 *     description: "ユーザーの作成、更新、削除を行う機能"
 *     entry-points:
 *       - "com.example.controller.UserController.createUser"
 *       - "com.example.controller.UserController.updateUser"
 *     packages:
 *       - "com.example.user"
 *       - "com.example.auth"
 * </pre>
 * 
 * <h3>出力CSV形式</h3>
 * <pre>
 * 機能名,機能説明,エントリーポイント数,対象クラス数,対象関数数,クラス総LOC,関数総LOC,コールグラフエッジ数
 * ユーザー管理機能,ユーザーの作成更新削除,3,15,45,850,650,125
 * </pre>
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
public class FeatureLocAggregator {
    private final CallGraphGenerator cg = new CallGraphGenerator();
    private final FunctionLocCounter counter = new FunctionLocCounter();
    private final EntrypointDetector detector = new EntrypointDetector();

    /**
     * 機能別LOC集計を実行し、結果のリストを返します。
     * 
     * <p>このメソッドは以下の処理を実行します：
     * <ol>
     *   <li>機能定義ファイルから機能設定を読み込み</li>
     *   <li>ソースコードから関数・クラスのLOCを計測</li>
     *   <li>コールグラフを生成して到達可能性を分析</li>
     *   <li>機能別に集計結果を計算</li>
     * </ol>
     * 
     * @param sourcePath ソースコードのディレクトリパス
     * @param jarPath コールグラフ生成用のJARファイルまたはクラスファイルディレクトリ
     * @param entryFile 機能定義ファイル（YAML/JSON形式）
     * @return 機能別LOC集計結果のリスト
     * @throws IOException ファイル読み込みエラーまたはコールグラフ生成エラー
     */
    public List<FeatureLocResult> aggregate(String sourcePath, String jarPath, File entryFile) throws IOException {
        Map<String, FeatureConfig> featureConfigs = detector.detectFromFile(entryFile);
        Map<String, Integer> functionLocMap = counter.countFunctionLines(sourcePath, Collections.emptyList());
        Map<String, Integer> classLocMap = counter.countClassLines(sourcePath, Collections.emptyList());
        CallGraphResult callGraph = cg.buildCallGraph(jarPath);
        
        List<FeatureLocResult> results = new ArrayList<>();
        
        for (Map.Entry<String, FeatureConfig> entry : featureConfigs.entrySet()) {
            FeatureConfig config = entry.getValue();
            
            Set<String> reachableFunctions = findReachableFunctions(
                config.getEntryPoints(), 
                callGraph,
                config.getPackages()
            );
            
            Set<String> reachableClasses = extractClassesFromFunctions(reachableFunctions);
            
            int totalFunctionLoc = calculateTotalLoc(reachableFunctions, functionLocMap);
            int totalClassLoc = calculateTotalLoc(reachableClasses, classLocMap);
            
            int callGraphEdges = countRelevantEdges(reachableFunctions, callGraph);
            
            FeatureLocResult result = new FeatureLocResult(
                config.getName(),
                config.getDescription(),
                config.getEntryPoints().size(),
                reachableClasses.size(),
                reachableFunctions.size(),
                totalClassLoc,
                totalFunctionLoc,
                callGraphEdges
            );
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 機能別LOC集計を実行し、CSV出力用の文字列配列リストを返します。
     * 
     * @param sourcePath ソースコードのディレクトリパス
     * @param jarPath コールグラフ生成用のJARファイルまたはクラスファイルディレクトリ
     * @param entryFile 機能定義ファイル（YAML/JSON形式）
     * @return CSV出力用の文字列配列のリスト
     * @throws IOException ファイル読み込みエラーまたはコールグラフ生成エラー
     */
    public List<String[]> aggregateToStringArray(String sourcePath, String jarPath, File entryFile) throws IOException {
        List<FeatureLocResult> results = aggregate(sourcePath, jarPath, entryFile);
        List<String[]> stringResults = new ArrayList<>();
        
        for (FeatureLocResult result : results) {
            stringResults.add(result.toCsvRow());
        }
        
        return stringResults;
    }
    
    /**
     * 機能別LOC集計を実行し、結果をCSVファイルに出力します。
     * 
     * <p>出力されるCSVファイルには以下の列が含まれます：
     * <ul>
     *   <li>機能名</li>
     *   <li>機能説明</li>
     *   <li>エントリーポイント数</li>
     *   <li>対象クラス数</li>
     *   <li>対象関数数</li>
     *   <li>クラス総LOC</li>
     *   <li>関数総LOC</li>
     *   <li>コールグラフエッジ数</li>
     * </ul>
     * 
     * @param sourcePath ソースコードのディレクトリパス
     * @param jarPath コールグラフ生成用のJARファイルまたはクラスファイルディレクトリ
     * @param entryFile 機能定義ファイル（YAML/JSON形式）
     * @param outputPath 出力CSVファイルのパス
     * @throws IOException ファイル読み込み・書き込みエラーまたはコールグラフ生成エラー
     */
    public void exportToCsv(String sourcePath, String jarPath, File entryFile, String outputPath) throws IOException {
        List<FeatureLocResult> results = aggregate(sourcePath, jarPath, entryFile);
        
        try (FileWriter writer = new FileWriter(outputPath)) {
            String[] headers = FeatureLocResult.getCsvHeader();
            writer.write(String.join(",", headers) + "\n");
            
            for (FeatureLocResult result : results) {
                String[] row = result.toCsvRow();
                writer.write(String.join(",", escapeCsvFields(row)) + "\n");
            }
        }
    }
    
    /**
     * エントリーポイントから到達可能な関数を特定します。
     * 
     * <p>幅優先探索を使用してコールグラフを辿り、指定されたパッケージ内の
     * 到達可能な関数をすべて収集します。
     * 
     * @param entryPoints 機能のエントリーポイント関数のリスト
     * @param callGraph コールグラフ
     * @param targetPackages 対象パッケージのリスト（空の場合はすべてのパッケージが対象）
     * @return 到達可能な関数の完全修飾名のセット
     */
    private Set<String> findReachableFunctions(List<String> entryPoints, CallGraphResult callGraph, List<String> targetPackages) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>(entryPoints);
        reachable.addAll(entryPoints);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            Set<String> callees = callGraph.getCallees(current);
            if (callees != null) {
                for (String callee : callees) {
                    if (!reachable.contains(callee) && isInTargetPackages(callee, targetPackages)) {
                        reachable.add(callee);
                        queue.offer(callee);
                    }
                }
            }
        }
        
        return reachable;
    }
    
    /**
     * 指定された完全修飾名が対象パッケージに含まれるかを判定します。
     * 
     * @param fqcn 判定対象の完全修飾名
     * @param targetPackages 対象パッケージのリスト
     * @return 対象パッケージに含まれる場合true、そうでなければfalse
     */
    private boolean isInTargetPackages(String fqcn, List<String> targetPackages) {
        if (targetPackages == null || targetPackages.isEmpty()) {
            return true;
        }
        
        for (String targetPackage : targetPackages) {
            if (fqcn.startsWith(targetPackage)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 関数の完全修飾名からクラス名を抽出します。
     * 
     * @param functions 関数の完全修飾名のセット
     * @return クラス名のセット
     */
    private Set<String> extractClassesFromFunctions(Set<String> functions) {
        Set<String> classes = new HashSet<>();
        for (String function : functions) {
            int lastDotIndex = function.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String className = function.substring(0, lastDotIndex);
                classes.add(className);
            }
        }
        return classes;
    }
    
    /**
     * 指定されたアイテムの総LOC数を計算します。
     * 
     * @param items LOCを計算する対象のアイテム（クラス名または関数名）のセット
     * @param locMap アイテム名をキーとしたLOCマップ
     * @return 総LOC数
     */
    private int calculateTotalLoc(Set<String> items, Map<String, Integer> locMap) {
        return items.stream()
                .mapToInt(item -> locMap.getOrDefault(item, 0))
                .sum();
    }
    
    /**
     * 到達可能な関数間のコールグラフエッジ数をカウントします。
     * 
     * @param reachableFunctions 到達可能な関数のセット
     * @param callGraph コールグラフ
     * @return 関連するエッジの数
     */
    private int countRelevantEdges(Set<String> reachableFunctions, CallGraphResult callGraph) {
        int count = 0;
        for (String function : reachableFunctions) {
            Set<String> callees = callGraph.getCallees(function);
            if (callees != null) {
                for (String callee : callees) {
                    if (reachableFunctions.contains(callee)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    /**
     * CSV出力用にフィールドをエスケープします。
     * 
     * <p>カンマ、ダブルクォート、改行文字を含むフィールドを
     * ダブルクォートで囲み、内部のダブルクォートを二重化します。
     * 
     * @param fields エスケープ対象のフィールド配列
     * @return エスケープされたフィールド配列
     */
    private String[] escapeCsvFields(String[] fields) {
        String[] escaped = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                field = "\"" + field.replace("\"", "\"\"") + "\"";
            }
            escaped[i] = field;
        }
        return escaped;
    }
} 