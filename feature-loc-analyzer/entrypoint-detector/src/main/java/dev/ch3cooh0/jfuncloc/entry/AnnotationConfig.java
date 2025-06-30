package dev.ch3cooh0.jfuncloc.entry;

import java.util.List;
import java.util.ArrayList;

/**
 * アノテーション検出の設定を管理するクラス。
 * 
 * <p>このクラスは、エントリーポイント検出で使用するアノテーションの
 * 設定を管理します。YAML/JSONファイルから読み込んだり、
 * プログラムで設定したりできます。
 * 
 * <h3>YAML設定例</h3>
 * <pre>
 * annotation-config:
 *   class-level-annotations:
 *     - "RestController"
 *     - "Controller"
 *     - "ApiController"
 *   method-level-annotations:
 *     - annotation: "GetMapping"
 *       feature-pattern: "{controller}-retrieval"
 *       aliases: ["Get", "HttpGet"]
 *     - annotation: "PostMapping"
 *       feature-pattern: "{controller}-creation" 
 *       aliases: ["Post", "HttpPost"]
 * </pre>
 */
public class AnnotationConfig {
    
    /** クラスレベルで検出するアノテーション名のリスト */
    private List<String> classLevelAnnotations;
    
    /** メソッドレベルで検出するアノテーションのマッピング設定 */
    private List<AnnotationMapping> methodLevelAnnotations;
    
    /** デフォルトの機能名生成パターン */
    private String defaultFeaturePattern = "{controller}-{action}";
    
    /** 検出を有効にするかどうか */
    private boolean enabled = true;
    
    public AnnotationConfig() {
        this.classLevelAnnotations = new ArrayList<>();
        this.methodLevelAnnotations = new ArrayList<>();
        initializeDefaults();
    }
    
    /**
     * デフォルトのアノテーション設定を初期化します。
     */
    private void initializeDefaults() {
        // デフォルトのクラスレベルアノテーション
        classLevelAnnotations.add("RestController");
        classLevelAnnotations.add("Controller");
        classLevelAnnotations.add("ApiController");
        classLevelAnnotations.add("WebController");
        classLevelAnnotations.add("Endpoint");
        classLevelAnnotations.add("Service");
        classLevelAnnotations.add("Component");
        
        // デフォルトのメソッドレベルアノテーション
        methodLevelAnnotations.add(createMapping("GetMapping", "{controller}-retrieval", "Get", "HttpGet"));
        methodLevelAnnotations.add(createMapping("PostMapping", "{controller}-creation", "Post", "HttpPost"));
        methodLevelAnnotations.add(createMapping("PutMapping", "{controller}-modification", "Put", "HttpPut"));
        methodLevelAnnotations.add(createMapping("DeleteMapping", "{controller}-deletion", "Delete", "HttpDelete"));
        methodLevelAnnotations.add(createMapping("PatchMapping", "{controller}-modification", "Patch", "HttpPatch"));
        methodLevelAnnotations.add(createMapping("RequestMapping", "{controller}-management", "Mapping"));
        methodLevelAnnotations.add(createMapping("ApiEndpoint", "{controller}-api", "Endpoint"));
        methodLevelAnnotations.add(createMapping("BusinessLogic", "{controller}-logic", "Logic"));
    }
    
    /**
     * AnnotationMappingオブジェクトを作成するヘルパーメソッド。
     */
    private AnnotationMapping createMapping(String annotation, String featurePattern, String... aliases) {
        AnnotationMapping mapping = new AnnotationMapping();
        mapping.setAnnotation(annotation);
        mapping.setFeaturePattern(featurePattern);
        mapping.setClassLevel(false);
        mapping.setDetectWhenPresent(true);
        
        if (aliases.length > 0) {
            List<String> aliasesList = new ArrayList<>();
            for (String alias : aliases) {
                aliasesList.add(alias);
            }
            mapping.setAliases(aliasesList);
        }
        
        return mapping;
    }
    
    // Getters and Setters
    public List<String> getClassLevelAnnotations() {
        return classLevelAnnotations;
    }
    
    public void setClassLevelAnnotations(List<String> classLevelAnnotations) {
        this.classLevelAnnotations = classLevelAnnotations;
    }
    
    public List<AnnotationMapping> getMethodLevelAnnotations() {
        return methodLevelAnnotations;
    }
    
    public void setMethodLevelAnnotations(List<AnnotationMapping> methodLevelAnnotations) {
        this.methodLevelAnnotations = methodLevelAnnotations;
    }
    
    public String getDefaultFeaturePattern() {
        return defaultFeaturePattern;
    }
    
    public void setDefaultFeaturePattern(String defaultFeaturePattern) {
        this.defaultFeaturePattern = defaultFeaturePattern;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * クラスレベルアノテーションを追加します。
     */
    public void addClassLevelAnnotation(String annotation) {
        if (!classLevelAnnotations.contains(annotation)) {
            classLevelAnnotations.add(annotation);
        }
    }
    
    /**
     * メソッドレベルアノテーションマッピングを追加します。
     */
    public void addMethodLevelAnnotation(AnnotationMapping mapping) {
        methodLevelAnnotations.add(mapping);
    }
    
    /**
     * 指定されたアノテーション名がクラスレベルアノテーションにマッチするかチェックします。
     */
    public boolean isClassLevelAnnotation(String annotationName) {
        return classLevelAnnotations.contains(annotationName);
    }
    
    /**
     * 指定されたアノテーション名にマッチするメソッドレベルマッピングを取得します。
     */
    public AnnotationMapping findMethodLevelMapping(String annotationName) {
        for (AnnotationMapping mapping : methodLevelAnnotations) {
            if (mapping.matches(annotationName)) {
                return mapping;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "AnnotationConfig{" +
                "classLevelAnnotations=" + classLevelAnnotations +
                ", methodLevelAnnotations=" + methodLevelAnnotations.size() + " mappings" +
                ", defaultFeaturePattern='" + defaultFeaturePattern + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}