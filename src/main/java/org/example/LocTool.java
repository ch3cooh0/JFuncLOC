package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import org.example.DescriptorUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LocTool {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Function("機能単位LOC計測")
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("使用方法: java -jar loc-tool.jar <mode> <target-jar> [entrypoints.json] [dependencies...]");
            System.exit(1);
        }

        String mode = args[0];
        String targetJar = args[1];
        String jsonPath = args.length > 2 ? args[2] : null;
        
        // 依存関係のjarファイルを取得
        String[] dependencies = args.length > 3 ? 
            Arrays.copyOfRange(args, 3, args.length) : new String[0];

        try {
            setupSoot(targetJar, dependencies);
            Map<String, Set<SootMethod>> functionEntryPoints = new HashMap<>();

            switch (mode) {
                case "annotation" -> collectAnnotationEntryPoints(functionEntryPoints);
                case "external" -> {
                    if (jsonPath == null) {
                        throw new IllegalArgumentException("外部エントリーポイントモードではJSONファイルが必要です");
                    }
                    collectExternalEntryPoints(jsonPath, functionEntryPoints);
                }
                case "hybrid" -> {
                    if (jsonPath == null) {
                        throw new IllegalArgumentException("ハイブリッドモードではJSONファイルが必要です");
                    }
                    collectAnnotationEntryPoints(functionEntryPoints);
                    collectExternalEntryPoints(jsonPath, functionEntryPoints);
                }
                default -> throw new IllegalArgumentException("不明なモード: " + mode);
            }

            Set<SootMethod> allEntries = functionEntryPoints.values().stream()
                .flatMap(Set::stream)
                .collect(java.util.stream.Collectors.toSet());
            Scene.v().setEntryPoints(new ArrayList<>(allEntries));
            PackManager.v().runPacks();

            analyzeAndPrintLoc(functionEntryPoints);

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setupSoot(String targetJar, String[] dependencies) {
        // 基本設定
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        
        // クラスパス設定
        StringBuilder classpath = new StringBuilder(targetJar);
        for (String dep : dependencies) {
            classpath.append(File.pathSeparator).append(dep);
        }
        
        // Java 9以降のモジュールパスを設定
        String javaHome = System.getProperty("java.home");
        String modulesPath = javaHome + File.separator + "lib" + File.separator + "modules";
        if (new File(modulesPath).exists()) {
            classpath.append(File.pathSeparator).append(modulesPath);
        }
        
        // Java 17/21のシステムモジュールを追加
        String systemModules = System.getProperty("java.class.path");
        if (systemModules != null && !systemModules.isEmpty()) {
            classpath.append(File.pathSeparator).append(systemModules);
        }
        
        Options.v().set_soot_classpath(classpath.toString());
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(targetJar));
        
        // コールグラフ設定
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.cha", "enabled:true");
        
        // Java 9以降のモジュールシステムの設定
        Options.v().set_include_all(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        
        // シーンの初期化
        Scene.v().loadNecessaryClasses();
        
        // デバッグ情報の出力
        Options.v().set_verbose(true);
        Options.v().set_debug(true);
        
        // コールグラフ生成は PackManager 実行時に行う
    }

    private static void collectAnnotationEntryPoints(Map<String, Set<SootMethod>> functionEntryPoints) {
        for (SootClass sc : Scene.v().getApplicationClasses()) {
            Function classAnnotation = (Function) sc.getTag("Function");
            if (classAnnotation != null) {
                String functionName = classAnnotation.value();
                functionEntryPoints.computeIfAbsent(functionName, k -> new HashSet<>())
                    .addAll(sc.getMethods().stream()
                        .filter(m -> m.isPublic())
                        .collect(java.util.stream.Collectors.toSet()));
            }

            for (SootMethod method : sc.getMethods()) {
                Function methodAnnotation = (Function) method.getTag("Function");
                if (methodAnnotation != null) {
                    String functionName = methodAnnotation.value();
                    functionEntryPoints.computeIfAbsent(functionName, k -> new HashSet<>())
                        .add(method);
                }
            }
        }
    }

    private static void collectExternalEntryPoints(String jsonPath, Map<String, Set<SootMethod>> functionEntryPoints) throws IOException {
        ExternalEntry[] entries = mapper.readValue(new File(jsonPath), ExternalEntry[].class);
        for (ExternalEntry entry : entries) {
            SootClass sc = Scene.v().getSootClass(entry.className());
            List<Type> paramTypes = DescriptorUtils.parseParameterTypes(entry.descriptor());
            SootMethod method = sc.getMethod(entry.method(), paramTypes);
            functionEntryPoints.computeIfAbsent(entry.function(), k -> new HashSet<>())
                .add(method);
        }
    }

    private static void analyzeAndPrintLoc(Map<String, Set<SootMethod>> functionEntryPoints) {
        CallGraph cg = Scene.v().getCallGraph();
        Map<String, Set<SootMethod>> reachableMethods = new HashMap<>();

        for (Map.Entry<String, Set<SootMethod>> entry : functionEntryPoints.entrySet()) {
            String functionName = entry.getKey();
            Set<SootMethod> methods = new HashSet<>();
            Queue<SootMethod> worklist = new LinkedList<>(entry.getValue());

            while (!worklist.isEmpty()) {
                SootMethod method = worklist.poll();
                if (!method.getDeclaringClass().isApplicationClass()) {
                    continue;
                }
                if (methods.add(method)) {
                    Iterator<Edge> edges = cg.edgesOutOf(method);
                    while (edges.hasNext()) {
                        SootMethod tgt = edges.next().tgt();
                        if (!tgt.getDeclaringClass().isApplicationClass()) {
                            continue;
                        }
                        worklist.add(tgt);
                    }
                }
            }

            reachableMethods.put(functionName, methods);
        }

        for (Map.Entry<String, Set<SootMethod>> entry : reachableMethods.entrySet()) {
            int totalLoc = entry.getValue().stream()
                .mapToInt(m -> m.getActiveBody().getUnits().size())
                .sum();
            System.out.printf("機能: %s, 有効LOC: %d%n", entry.getKey(), totalLoc);
        }
    }
}
