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
        Map<String, Set<String>> result = new HashMap<>();
        
        if (jarPath.endsWith(".jar")) {
            result.putAll(detectFromJar(jarPath, targetPackages));
        } else {
            result.putAll(detectFromClassPath(jarPath, targetPackages));
        }
        
        return result;
    }
    
    private Map<String, Set<String>> detectFromJar(String jarPath, List<String> targetPackages) throws IOException {
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
                                processClassForEntryPoints(clazz, result);
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
                                processClassForEntryPoints(clazz, result);
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            System.err.println("Could not load class from path: " + path + " - " + e.getMessage());
                        }
                    });
        }
        
        return result;
    }
    
    private void processClassForEntryPoints(Class<?> clazz, Map<String, Set<String>> result) {
        for (Method method : clazz.getDeclaredMethods()) {
            EntryPoint annotation = method.getAnnotation(EntryPoint.class);
            if (annotation != null) {
                String featureName = annotation.value();
                String methodFqcn = fqcn(method);
                
                result.computeIfAbsent(featureName, k -> new HashSet<>()).add(methodFqcn);
            }
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
        List<EntryPointInfo> result = new ArrayList<>();
        
        try {
            Map<String, Set<String>> entryPoints = detectFromAnnotations(path, targetPackages);
            
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