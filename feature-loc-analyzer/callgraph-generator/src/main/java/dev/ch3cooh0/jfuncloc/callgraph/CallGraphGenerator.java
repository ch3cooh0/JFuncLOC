package dev.ch3cooh0.jfuncloc.callgraph;

import dev.ch3cooh0.jfuncloc.shared.FqcnUtils;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.PackManager;

import java.io.File;
import java.util.*;

/**
 * ソースコードからコールグラフを生成するクラス。
 * Sootフレームワークを使用して、Javaソースコードのメソッド間の呼び出し関係を解析し、
 * コールグラフを構築します。
 */
public class CallGraphGenerator {
    private final String targetPackage;
    private final SootConfigurator sootConfigurator;

    /**
     * デフォルトコンストラクタ。
     * パッケージフィルタリングなしでコールグラフを生成します。
     */
    public CallGraphGenerator() {
        this(null);
    }

    /**
     * 指定されたパッケージに限定してコールグラフを生成するコンストラクタ。
     *
     * @param targetPackage 解析対象のパッケージ名（例: "com.example"）
     */
    public CallGraphGenerator(String targetPackage) {
        this.targetPackage = targetPackage;
        this.sootConfigurator = new SootConfigurator(targetPackage);
    }

    /**
     * 指定されたソースディレクトリからコールグラフを構築します。
     * 
     * このメソッドは以下の処理を行います：
     * 1. Sootの設定を行い、ソースディレクトリを指定
     * 2. コールグラフの生成を有効化
     * 3. 指定されたパッケージに基づいてフィルタリング（targetPackageが設定されている場合）
     * 4. コールグラフの走査とメソッド間の呼び出し関係の抽出
     * 5. ライブラリクラス（java.*, javax.*, sun.*, com.sun.*, jdk.*）の呼び出しを除外
     *
     * @param sourcePath 解析対象のソースコードが格納されているディレクトリのパス、またはjarファイルのパス
     * @return メソッド間の呼び出し関係を表すCallGraphResultオブジェクト
     */
    public CallGraphResult buildCallGraph(String sourcePath) {
        System.out.println("デバッグ: Soot設定開始");
        
        // Sootの設定
        sootConfigurator.configure(sourcePath);
        
        System.out.println("デバッグ: クラス読み込み開始");
        Scene.v().loadNecessaryClasses();
        System.out.println("デバッグ: クラス読み込み完了");
        
        // 解析フェーズの実行
        System.out.println("デバッグ: 解析フェーズ実行開始");
        PackManager.v().runPacks();
        System.out.println("デバッグ: 解析フェーズ実行完了");
        
        // コールグラフの構築と解析
        System.out.println("デバッグ: コールグラフ構築開始");
        CallGraph cg = Scene.v().getCallGraph();
        CallGraphAnalyzer analyzer = new CallGraphAnalyzer(targetPackage);
        Map<String, Set<String>> callRelations = analyzer.analyze(cg);
        
        return new CallGraphResult(callRelations);
    }

    /**
     * Sootの設定を行う内部クラス
     */
    private static class SootConfigurator {
        private final String targetPackage;

        public SootConfigurator(String targetPackage) {
            this.targetPackage = targetPackage;
        }

        public void configure(String sourcePath) {
            Options.v().set_prepend_classpath(true);
            
            // 入力パスがjarファイルかディレクトリかを判定
            File inputFile = new File(sourcePath);
            if (inputFile.isFile() && sourcePath.toLowerCase().endsWith(".jar")) {
                System.out.println("デバッグ: JARファイルを処理: " + sourcePath);
                Options.v().set_process_dir(Collections.singletonList(sourcePath));
                Options.v().set_soot_classpath(sourcePath);
            } else {
                System.out.println("デバッグ: ディレクトリを処理: " + sourcePath);
                Options.v().set_process_dir(Collections.singletonList(sourcePath));
            }
            
            // 全プログラム解析を有効にする設定
            // これにより、プログラム全体を対象にした解析が可能になります。
            Options.v().set_whole_program(true);
            
            // コールグラフ生成を有効にする設定
            // コールグラフの生成を行うための基本的な設定です。
            Options.v().setPhaseOption("cg", "enabled:true");
            
            // Sparkフレームワークを用いたコールグラフ生成を有効にする設定
            // Sparkを使用することで、より効率的なコールグラフ生成が可能になります。
            Options.v().setPhaseOption("cg.spark", "enabled:true");
            
            // オンザフライでのコールグラフ生成を有効にする設定
            // コールグラフを動的に生成し、メモリ使用量を削減します。
            Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
            
            // Rapid Type Analysisを無効にする設定
            // RTAを無効にすることで、より詳細な解析が可能になります。
            Options.v().setPhaseOption("cg.spark", "rta:false");
            
            // 文字列定数の解析を有効にする設定
            // 文字列定数を解析することで、より正確なコールグラフが生成されます。
            Options.v().setPhaseOption("cg.spark", "string-constants:true");
            
            // ネイティブメソッドのシミュレーションを有効にする設定
            // ネイティブメソッドの挙動をシミュレートし、解析の精度を向上させます。
            Options.v().setPhaseOption("cg.spark", "simulate-natives:true");
            
            // サイトごとの型解析を有効にする設定
            // 各呼び出しサイトでの型情報を考慮することで、解析の精度を向上させます。
            Options.v().setPhaseOption("cg.spark", "types-for-sites:true");
            
            // StringBufferのマージを有効にする設定
            // StringBufferの操作を最適化し、解析の効率を向上させます。
            Options.v().setPhaseOption("cg.spark", "merge-stringbuffer:true");
            
            // Jimple化前の処理を有効にする設定
            // Jimple化前に最適化を行うことで、解析の効率を向上させます。
            Options.v().setPhaseOption("cg.spark", "pre-jimplify:true");
            
            // パッケージフィルタリングの設定
            if (targetPackage != null) {
                System.out.println("デバッグ: パッケージフィルタリング設定: " + targetPackage);
                Options.v().set_include(Collections.singletonList(targetPackage + ".*"));
                // 除外パッケージの設定を追加
                List<String> excludePackages = Arrays.asList(
                    "java.*",
                    "javax.*",
                    "sun.*",
                    "com.sun.*",
                    "jdk.*",
                    "com.fasterxml.*"  // Jacksonライブラリを除外
                );
                Options.v().set_exclude(excludePackages);
            }
        }
    }

    /**
     * コールグラフの解析を行う内部クラス
     */
    private static class CallGraphAnalyzer {
        private final String targetPackage;

        public CallGraphAnalyzer(String targetPackage) {
            this.targetPackage = targetPackage;
        }

        public Map<String, Set<String>> analyze(CallGraph cg) {
            Map<String, Set<String>> callRelations = new HashMap<>();
            int edgeCount = 0;

            for (Iterator<Edge> it = cg.iterator(); it.hasNext();) {
                Edge edge = it.next();
                SootMethod src = edge.src();
                SootMethod tgt = edge.tgt();
                
                if (shouldSkipEdge(src, tgt)) {
                    continue;
                }
                
                String srcFqcn = fqcn(src.getDeclaringClass().getName(), src.getName());
                String tgtFqcn = fqcn(tgt.getDeclaringClass().getName(), tgt.getName());
                
                callRelations.computeIfAbsent(srcFqcn, k -> new HashSet<>()).add(tgtFqcn);
                edgeCount++;
            }
            
            System.out.println("デバッグ: コールグラフ構築完了 - " + edgeCount + " エッジを処理");
            return callRelations;
        }

        private boolean shouldSkipEdge(SootMethod src, SootMethod tgt) {
            if (targetPackage != null) {
                // 呼び出し元のクラスのみをパッケージチェック
                return !src.getDeclaringClass().getName().startsWith(targetPackage);
            }
            return isLibraryClass(src.getDeclaringClass()) || isLibraryClass(tgt.getDeclaringClass());
        }

        private boolean isLibraryClass(SootClass clazz) {
            String name = clazz.getName();
            return name.startsWith("java.") || 
                   name.startsWith("javax.") || 
                   name.startsWith("sun.") || 
                   name.startsWith("com.sun.") ||
                   name.startsWith("jdk.") ||
                   name.startsWith("com.fasterxml.");  // Jacksonライブラリを追加
        }
    }

    /**
     * クラス名とメソッド名から完全修飾名（FQCN）を生成します。
     *
     * @param className クラス名
     * @param methodName メソッド名
     * @return 完全修飾名（例: "com.example.MyClass.myMethod"）
     */
    public static String fqcn(String className, String methodName) {
        return FqcnUtils.toFqcn(className, methodName);
    }
}
