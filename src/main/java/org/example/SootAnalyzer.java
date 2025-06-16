package org.example;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Soot を用いた呼び出し解析と LOC 集計を行うユーティリティクラス。
 * 並列処理をサポートし、パフォーマンスを最適化。
 */
public class SootAnalyzer implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SootAnalyzer.class);
    private final ExecutorService executorService;
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final Map<SootMethod, Integer> locCache;

    public SootAnalyzer(File jarFile) {
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JARファイルが存在しません: " + jarFile.getAbsolutePath());
        }

        try {
            // Sootの設定
            Options.v().set_whole_program(true);
            Options.v().set_app(true);
            Options.v().set_allow_phantom_refs(true);
            Options.v().set_output_format(Options.output_format_none);
            Options.v().set_soot_classpath(jarFile.getAbsolutePath());
            
            // クラスパスの設定
            Scene.v().addBasicClass("java.lang.Object", SootClass.BODIES);
            Scene.v().addBasicClass("java.lang.System", SootClass.BODIES);
            Scene.v().loadNecessaryClasses();
            
            this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            this.locCache = new ConcurrentHashMap<>();
        } catch (Exception e) {
            throw new RuntimeException("SootAnalyzerの初期化中にエラーが発生", e);
        }
    }

    public Set<SootMethod> reachableMethods(SootMethod entry) {
        Set<SootMethod> result = Collections.synchronizedSet(new HashSet<>());
        Deque<SootMethod> work = new ArrayDeque<>();
        work.push(entry);
        result.add(entry);

        CallGraph cg = Scene.v().getCallGraph();

        while (!work.isEmpty()) {
            SootMethod m = work.pop();
            try {
                Iterator<Edge> edges = cg.edgesOutOf(m);
                while (edges.hasNext()) {
                    SootMethod tgt = edges.next().tgt();
                    if (result.add(tgt)) {
                        work.push(tgt);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("メソッド {} の解析中にエラーが発生", m.getSignature(), e);
            }
        }
        return result;
    }

    public int countLOC(Set<SootMethod> methods) {
        if (methods == null || methods.isEmpty()) {
            return 0;
        }

        try {
            List<Future<Integer>> futures = new ArrayList<>();
            
            for (SootMethod m : methods) {
                futures.add(executorService.submit(() -> {
                    try {
                        return locCache.computeIfAbsent(m, method -> {
                            try {
                                method.retrieveActiveBody();
                                return method.getActiveBody().getUnits().size();
                            } catch (Exception e) {
                                LOGGER.warn("メソッド {} のLOC計算中にエラーが発生", method.getSignature(), e);
                                return 0;
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.error("メソッド {} の処理中にエラーが発生", m.getSignature(), e);
                        return 0;
                    }
                }));
            }

            return futures.stream()
                    .mapToInt(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            LOGGER.error("LOC計算の結果取得中にエラーが発生", e);
                            return 0;
                        }
                    })
                    .sum();
        } catch (Exception e) {
            throw new RuntimeException("LOC計算中にエラーが発生", e);
        }
    }

    public Map<String, Integer> analyzeClassComplexity() {
        Map<String, Integer> complexity = new HashMap<>();
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            int classLoc = 0;
            for (SootMethod method : cls.getMethods()) {
                try {
                    method.retrieveActiveBody();
                    classLoc += method.getActiveBody().getUnits().size();
                } catch (Exception e) {
                    LOGGER.warn("メソッド {} の解析中にエラーが発生", method.getSignature(), e);
                }
            }
            complexity.put(cls.getName(), classLoc);
        }
        return complexity;
    }

    @Override
    public void close() {
        try {
            executorService.shutdown();
            locCache.clear();
        } catch (Exception e) {
            LOGGER.error("リソースの解放中にエラーが発生", e);
        }
    }
}
