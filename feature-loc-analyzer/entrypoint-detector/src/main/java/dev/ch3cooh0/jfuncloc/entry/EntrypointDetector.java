package dev.ch3cooh0.jfuncloc.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.ch3cooh0.jfuncloc.shared.FqcnUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * エントリーポイント検出機能を提供するクラス。
 * 
 * <p>このクラスは以下の2つの方法でエントリーポイントを検出できます：
 * <ol>
 *   <li><strong>ファイルベース検出</strong>: YAML/JSONファイルから機能定義を読み込み</li>
 *   <li><strong>アノテーションベース検出</strong>: @EntryPointアノテーションが付与されたメソッドを検出</li>
 * </ol>
 * 
 * <h3>サポートするファイル形式</h3>
 * <ul>
 *   <li>YAML形式 (.yaml, .yml)</li>
 *   <li>JSON形式 (.json)</li>
 * </ul>
 * 
 * <h3>アノテーション検出の対象</h3>
 * <ul>
 *   <li>JARファイル内のクラス</li>
 *   <li>クラスファイルディレクトリ内のクラス</li>
 * </ul>
 * 
 * <h3>使用例</h3>
 * <pre>
 * EntrypointDetector detector = new EntrypointDetector();
 * 
 * // ファイルから機能定義を読み込み
 * File configFile = new File("features.yaml");
 * Map&lt;String, FeatureConfig&gt; configs = detector.detectFromFile(configFile);
 * 
 * // アノテーションからエントリーポイントを検出
 * List&lt;String&gt; packages = Arrays.asList("com.example");
 * Map&lt;String, Set&lt;String&gt;&gt; annotations = detector.detectFromAnnotations("app.jar", packages);
 * </pre>
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
public class EntrypointDetector {
    
    /**
     * YAML/JSONファイルから機能定義を読み込みます。
     * 
     * <p>ファイルの拡張子に基づいて自動的にフォーマットを判定し、
     * 適切なパーサーでファイルを解析します。
     * 
     * @param file 機能定義ファイル（YAMLまたはJSON形式）
     * @return 機能キーをキーとした機能設定のマップ
     * @throws IOException ファイル読み込みエラーまたはパースエラー
     */
    @SuppressWarnings("unchecked")
    public Map<String, FeatureConfig> detectFromFile(File file) throws IOException {
        ObjectMapper mapper;
        
        if (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml")) {
            mapper = new ObjectMapper(new YAMLFactory());
        } else {
            mapper = new ObjectMapper();
        }
        
        Map<String, Object> rootMap = (Map<String, Object>) mapper.readValue(file, Map.class);
        Map<String, Object> featuresMap = (Map<String, Object>) rootMap.get("features");
        
        Map<String, FeatureConfig> result = new HashMap<>();
        
        if (featuresMap == null || featuresMap.isEmpty()) {
            return result;
        }
        
        for (Map.Entry<String, Object> entry : featuresMap.entrySet()) {
            String featureName = entry.getKey();
            Map<String, Object> featureData = (Map<String, Object>) entry.getValue();
            
            FeatureConfig config = new FeatureConfig();
            config.setName((String) featureData.get("name"));
            config.setDescription((String) featureData.get("description"));
            config.setEntryPoints((List<String>) featureData.get("entry-points"));
            config.setPackages((List<String>) featureData.get("packages"));
            
            result.put(featureName, config);
        }
        
        return result;
    }
    
    /**
     * JARファイルまたはクラスファイルディレクトリから@EntryPointアノテーションを検出します。
     * 
     * <p>指定されたパッケージ内のクラスをスキャンし、
     * @EntryPointアノテーションが付与されたメソッドを特定します。
     * 
     * @param jarPath JARファイルまたはクラスファイルディレクトリのパス
     * @param targetPackages 検出対象のパッケージリスト（空の場合は全パッケージが対象）
     * @return 機能名をキーとし、エントリーポイントのFQCNセットを値とするマップ
     * @throws IOException ファイル読み込みエラーまたはクラスロードエラー
     */
    public Map<String, Set<String>> detectFromAnnotations(String jarPath, List<String> targetPackages) throws IOException {
        return detectFromAnnotations(jarPath, targetPackages, new AnnotationConfig());
    }
    
    /**
     * JARファイルまたはクラスファイルディレクトリから@EntryPointアノテーションを検出します（AnnotationConfig使用）。
     * 
     * @param jarPath JARファイルまたはクラスファイルディレクトリのパス
     * @param targetPackages 検出対象のパッケージリスト（空の場合は全パッケージが対象）
     * @param annotationConfig アノテーション検出設定
     * @return 機能名をキーとし、エントリーポイントのFQCNセットを値とするマップ
     * @throws IOException ファイル読み込みエラーまたはクラスロードエラー
     */
    public Map<String, Set<String>> detectFromAnnotations(String jarPath, List<String> targetPackages, AnnotationConfig annotationConfig) throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        
        if (jarPath.endsWith(".jar")) {
            result.putAll(detectFromJar(jarPath, targetPackages, annotationConfig));
        } else {
            result.putAll(detectFromClassPath(jarPath, targetPackages, annotationConfig));
        }
        
        return result;
    }
    
    private Map<String, Set<String>> detectFromJar(String jarPath, List<String> targetPackages) throws IOException {
        return detectFromJar(jarPath, targetPackages, new AnnotationConfig());
    }
    
    private Map<String, Set<String>> detectFromJar(String jarPath, List<String> targetPackages, AnnotationConfig annotationConfig) throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        
        try (JarFile jarFile = new JarFile(jarPath)) {
            URL[] urls = {new File(jarPath).toURI().toURL()};
            try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    
                    if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                        String className = entry.getName()
                                .replace('/', '.')
                                .replace(".class", "");
                        
                        if (isTargetPackage(className, targetPackages)) {
                            try {
                                Class<?> clazz = classLoader.loadClass(className);
                                processClassForEntryPoints(clazz, result, annotationConfig);
                            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                                System.err.println("Could not load class: " + className + " - " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private Map<String, Set<String>> detectFromClassPath(String classPath, List<String> targetPackages) throws IOException {
        return detectFromClassPath(classPath, targetPackages, new AnnotationConfig());
    }
    
    private Map<String, Set<String>> detectFromClassPath(String classPath, List<String> targetPackages, AnnotationConfig annotationConfig) throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        
        Path rootPath = Paths.get(classPath);
        if (!Files.exists(rootPath)) {
            return result;
        }
        
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{rootPath.toUri().toURL()})) {
            Files.walk(rootPath)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            String relativePath = rootPath.relativize(path).toString();
                            String className = relativePath
                                    .replace(File.separatorChar, '.')
                                    .replace(".class", "");
                            
                            if (isTargetPackage(className, targetPackages)) {
                                Class<?> clazz = classLoader.loadClass(className);
                                processClassForEntryPoints(clazz, result, annotationConfig);
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            System.err.println("Could not load class from path: " + path + " - " + e.getMessage());
                        }
                    });
        }
        
        return result;
    }
    
    private void processClassForEntryPoints(Class<?> clazz, Map<String, Set<String>> result) {
        processClassForEntryPoints(clazz, result, new AnnotationConfig());
    }
    
    private void processClassForEntryPoints(Class<?> clazz, Map<String, Set<String>> result, AnnotationConfig annotationConfig) {
        // 1. 既存の @EntryPoint アノテーション検出
        for (Method method : clazz.getDeclaredMethods()) {
            EntryPoint annotation = method.getAnnotation(EntryPoint.class);
            if (annotation != null) {
                String featureName = annotation.value();
                String methodFqcn = fqcn(method);
                
                result.computeIfAbsent(featureName, k -> new HashSet<>()).add(methodFqcn);
            }
        }
        
        // 2. 設定ベースのアノテーション検出
        processConfigurableAnnotations(clazz, result, annotationConfig);
    }
    
    /**
     * 設定可能なアノテーションを検出してエントリーポイントとして追加します。
     * 
     * @param clazz 検査対象のクラス
     * @param result 結果を格納するマップ
     * @param annotationConfig アノテーション設定
     */
    private void processConfigurableAnnotations(Class<?> clazz, Map<String, Set<String>> result, AnnotationConfig annotationConfig) {
        if (!annotationConfig.isEnabled()) {
            return;
        }
        
        String className = clazz.getSimpleName();
        
        // デバッグ用ログ
        System.out.println("Processing class: " + clazz.getName());
        
        // クラスレベルのアノテーション確認
        boolean isController = false;
        for (String annotation : annotationConfig.getClassLevelAnnotations()) {
            if (hasAnnotationByName(clazz, annotation)) {
                isController = true;
                System.out.println("  Found class-level annotation: " + annotation);
                break;
            }
        }
        
        if (!isController) {
            System.out.println("  Skipping - not a controller class");
            return;
        }
        
        System.out.println("  Processing as controller");
        
        // メソッドレベルのアノテーション検出
        for (Method method : clazz.getDeclaredMethods()) {
            String featureName = null;
            
            for (AnnotationMapping mapping : annotationConfig.getMethodLevelAnnotations()) {
                if (hasAnnotationByName(method, mapping.getAnnotation()) ||
                    (mapping.getAliases() != null && mapping.getAliases().stream().anyMatch(alias -> hasAnnotationByName(method, alias)))) {
                    
                    featureName = generateFeatureName(method, className, mapping);
                    System.out.println("    Found method annotation: " + mapping.getAnnotation() + " -> " + featureName);
                    break;
                }
            }
            
            if (featureName != null) {
                String methodFqcn = fqcn(method);
                result.computeIfAbsent(featureName, k -> new HashSet<>()).add(methodFqcn);
            }
        }
    }
    
    /**
     * Spring Framework のアノテーションを検出してエントリーポイントとして追加します。
     * 
     * @param clazz 検査対象のクラス
     * @param result 結果を格納するマップ
     */
    private void processSpringAnnotations(Class<?> clazz, Map<String, Set<String>> result) {
        String className = clazz.getSimpleName();
        
        // デバッグ用ログ
        System.out.println("Processing class: " + clazz.getName());
        
        // クラスレベルのアノテーション確認（柔軟な文字列ベースマッチング）
        boolean isRestController = hasAnnotationByName(clazz, "RestController") ||
                                 hasAnnotationByName(clazz, "ApiController") ||
                                 hasAnnotationByName(clazz, "WebController");
        boolean isController = hasAnnotationByName(clazz, "Controller") ||
                             hasAnnotationByName(clazz, "RestController") ||
                             hasAnnotationByName(clazz, "Endpoint") ||
                             hasAnnotationByName(clazz, "Service") ||
                             hasAnnotationByName(clazz, "Component");
        
        System.out.println("  isRestController: " + isRestController + ", isController: " + isController);
        
        if (!isRestController && !isController) {
            System.out.println("  Skipping - not a controller");
            return; // Controller でない場合はスキップ
        }
        
        System.out.println("  Processing as controller");
        
        // メソッドレベルのSpringアノテーション検出
        for (Method method : clazz.getDeclaredMethods()) {
            String featureName = null;
            
            // 柔軟なアノテーション検出（プロジェクト固有のアノテーションにも対応）
            if (hasAnnotationByName(method, "RequestMapping") || hasAnnotationByName(method, "Mapping")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "RequestMapping");
            } else if (hasAnnotationByName(method, "GetMapping") || hasAnnotationByName(method, "Get")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "GetMapping");
            } else if (hasAnnotationByName(method, "PostMapping") || hasAnnotationByName(method, "Post")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "PostMapping");
            } else if (hasAnnotationByName(method, "PutMapping") || hasAnnotationByName(method, "Put")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "PutMapping");
            } else if (hasAnnotationByName(method, "DeleteMapping") || hasAnnotationByName(method, "Delete")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "DeleteMapping");
            } else if (hasAnnotationByName(method, "PatchMapping") || hasAnnotationByName(method, "Patch")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "PatchMapping");
            } else if (hasAnnotationByName(method, "ApiEndpoint") || hasAnnotationByName(method, "Endpoint")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "ApiEndpoint");
            } else if (hasAnnotationByName(method, "BusinessLogic") || hasAnnotationByName(method, "Logic")) {
                featureName = extractFeatureNameFromSpringMapping(method, className, "BusinessLogic");
            }
            
            if (featureName != null) {
                String methodFqcn = fqcn(method);
                result.computeIfAbsent(featureName, k -> new HashSet<>()).add(methodFqcn);
            }
        }
    }
    
    /**
     * クラスまたはメソッドが指定されたアノテーションを持つかチェックします。
     * 
     * @param element クラスまたはメソッド
     * @param annotationClassName アノテーションのFQCN
     * @return アノテーションが存在する場合true
     */
    /**
     * 文字列ベースでアノテーション検出を行います（ClassLoader問題を回避）。
     * 
     * @param element クラスまたはメソッド
     * @param annotationSimpleName アノテーションのシンプル名
     * @return アノテーションが存在する場合true
     */
    private boolean hasAnnotationByName(java.lang.reflect.AnnotatedElement element, String annotationSimpleName) {
        return java.util.Arrays.stream(element.getAnnotations())
                .anyMatch(ann -> ann.annotationType().getSimpleName().equals(annotationSimpleName));
    }
    
    /**
     * FQCN またはシンプル名でアノテーション検出を行います。
     * 
     * @param element クラスまたはメソッド
     * @param annotationPattern アノテーションのFQCNまたはシンプル名
     * @return アノテーションが存在する場合true
     */
    private boolean hasAnnotationPattern(java.lang.reflect.AnnotatedElement element, String annotationPattern) {
        return java.util.Arrays.stream(element.getAnnotations())
                .anyMatch(ann -> {
                    String fullName = ann.annotationType().getName();
                    String simpleName = ann.annotationType().getSimpleName();
                    return fullName.equals(annotationPattern) || 
                           simpleName.equals(annotationPattern) ||
                           fullName.endsWith("." + annotationPattern);
                });
    }
    
    /**
     * AnnotationMappingに基づいて機能名を生成します。
     * 
     * @param method メソッド
     * @param className クラス名
     * @param mapping アノテーションマッピング
     * @return 生成された機能名
     */
    private String generateFeatureName(Method method, String className, AnnotationMapping mapping) {
        String pattern = mapping.getFeaturePattern();
        
        if (pattern == null || pattern.isEmpty()) {
            pattern = mapping.getDefaultFeature();
        }
        
        if (pattern == null || pattern.isEmpty()) {
            // フォールバック: 従来のロジック使用
            return extractFeatureNameFromSpringMapping(method, className, mapping.getAnnotation());
        }
        
        // パターン変数を置換
        String controllerName = className.replace("Controller", "").toLowerCase();
        String methodName = method.getName();
        String action = determineAction(methodName);
        
        return pattern
                .replace("{controller}", controllerName)
                .replace("{method}", methodName)
                .replace("{action}", action)
                .replace("{class}", className);
    }
    
    /**
     * メソッド名からアクション名を決定します。
     */
    private String determineAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "creation";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "modification";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "deletion";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("list")) {
            return "retrieval";
        } else {
            return "management";
        }
    }
    
    // 下位互換性のため既存メソッドも保持
    private boolean hasAnnotation(java.lang.reflect.AnnotatedElement element, String annotationClassName) {
        // まず文字列ベースで試行
        if (hasAnnotationPattern(element, annotationClassName)) {
            return true;
        }
        
        // 従来のClassLoader方式も試行（フォールバック）
        try {
            @SuppressWarnings("unchecked")
            Class<? extends java.lang.annotation.Annotation> annotationClass = 
                (Class<? extends java.lang.annotation.Annotation>) Class.forName(annotationClassName);
            return element.getAnnotation(annotationClass) != null;
        } catch (ClassNotFoundException | ClassCastException e) {
            return false;
        }
    }
    
    /**
     * Spring マッピングアノテーションから機能名を生成します。
     * 
     * @param method メソッド
     * @param className クラス名
     * @param mappingType マッピングの種類
     * @return 生成された機能名
     */
    private String extractFeatureNameFromSpringMapping(Method method, String className, String mappingType) {
        // 機能名の生成ルール（カスタマイズ可能）
        // 例: UserController.createUser -> "user-management"
        //     OrderController.processOrder -> "order-processing"
        
        String controllerName = className.replace("Controller", "").toLowerCase();
        String methodName = method.getName();
        
        // 汎用的な機能名生成
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return controllerName + "-creation";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return controllerName + "-modification";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return controllerName + "-deletion";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("list")) {
            return controllerName + "-retrieval";
        } else {
            // その他の場合はコントローラー名ベースの機能名
            return controllerName + "-management";
        }
    }
    
    private boolean isTargetPackage(String className, List<String> targetPackages) {
        if (targetPackages == null || targetPackages.isEmpty()) {
            return true;
        }
        
        for (String targetPackage : targetPackages) {
            if (className.startsWith(targetPackage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたパスからエントリーポイント情報を検出します。
     * 
     * <p>このメソッドはアノテーションベースの検出を実行し、
     * 結果をEntryPointInfoオブジェクトのリストとして返します。
     * 
     * @param path JARファイルまたはクラスファイルディレクトリのパス
     * @param targetPackages 検出対象のパッケージリスト
     * @return 検出されたエントリーポイント情報のリスト
     */
    public List<EntryPointInfo> detectEntryPoints(String path, List<String> targetPackages) {
        return detectEntryPoints(path, targetPackages, new AnnotationConfig());
    }
    
    /**
     * 指定されたパスからエントリーポイント情報を検出します（AnnotationConfig使用）。
     * 
     * @param path JARファイルまたはクラスファイルディレクトリのパス
     * @param targetPackages 検出対象のパッケージリスト（空の場合は全パッケージが対象）
     * @param annotationConfig アノテーション検出設定
     * @return 検出されたエントリーポイント情報のリスト
     */
    public List<EntryPointInfo> detectEntryPoints(String path, List<String> targetPackages, AnnotationConfig annotationConfig) {
        List<EntryPointInfo> result = new ArrayList<>();
        
        try {
            Map<String, Set<String>> entryPoints = detectFromAnnotations(path, targetPackages, annotationConfig);
            
            for (Map.Entry<String, Set<String>> entry : entryPoints.entrySet()) {
                String featureName = entry.getKey();
                for (String methodFqcn : entry.getValue()) {
                    result.add(new EntryPointInfo(featureName, methodFqcn));
                }
            }
        } catch (IOException e) {
            System.err.println("Error detecting entry points: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Methodオブジェクトから完全修飾名（FQCN）を生成します。
     * 
     * @param method FQCNを生成する対象のMethodオブジェクト
     * @return メソッドの完全修飾名（クラス名.メソッド名の形式）
     */
    public static String fqcn(Method method) {
        return FqcnUtils.toFqcn(method.getDeclaringClass().getName(), method.getName());
    }
} 