package dev.ch3cooh0.jfuncloc.entry;

import java.util.List;

/**
 * アノテーションとエントリーポイント検出ルールのマッピング設定。
 * 
 * <p>このクラスは、検出対象のアノテーションとそのアノテーションから
 * 機能名を抽出するルールを定義します。
 * 
 * <h3>使用例</h3>
 * <pre>
 * # YAML設定ファイル
 * annotation-config:
 *   class-level-annotations:
 *     - "RestController"
 *     - "Controller" 
 *     - "ApiController"
 *   method-level-annotations:
 *     - annotation: "GetMapping"
 *       feature-pattern: "{controller}-retrieval"
 *     - annotation: "PostMapping"
 *       feature-pattern: "{controller}-creation"
 * </pre>
 */
public class AnnotationMapping {
    
    /** アノテーション名（シンプル名またはFQCN） */
    private String annotation;
    
    /** 機能名を取得するアノテーションの属性名 */
    private String featureAttribute;
    
    /** デフォルトの機能名 */
    private String defaultFeature;
    
    /** クラスレベルのアノテーションかどうか */
    private boolean classLevel = false;
    
    /** 機能名生成パターン（{controller}, {method}等の変数使用可能） */
    private String featurePattern;
    
    /** アノテーションが存在する場合に検出するかどうか */
    private boolean detectWhenPresent = true;
    
    /** アノテーションの別名リスト */
    private List<String> aliases;
    
    public AnnotationMapping() {
    }
    
    public AnnotationMapping(String annotation, String defaultFeature) {
        this.annotation = annotation;
        this.defaultFeature = defaultFeature;
    }
    
    public AnnotationMapping(String annotation, String defaultFeature, boolean classLevel) {
        this.annotation = annotation;
        this.defaultFeature = defaultFeature;
        this.classLevel = classLevel;
    }
    
    // Getters and Setters
    public String getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    
    public String getFeatureAttribute() {
        return featureAttribute;
    }
    
    public void setFeatureAttribute(String featureAttribute) {
        this.featureAttribute = featureAttribute;
    }
    
    public String getDefaultFeature() {
        return defaultFeature;
    }
    
    public void setDefaultFeature(String defaultFeature) {
        this.defaultFeature = defaultFeature;
    }
    
    public boolean isClassLevel() {
        return classLevel;
    }
    
    public void setClassLevel(boolean classLevel) {
        this.classLevel = classLevel;
    }
    
    public String getFeaturePattern() {
        return featurePattern;
    }
    
    public void setFeaturePattern(String featurePattern) {
        this.featurePattern = featurePattern;
    }
    
    public boolean isDetectWhenPresent() {
        return detectWhenPresent;
    }
    
    public void setDetectWhenPresent(boolean detectWhenPresent) {
        this.detectWhenPresent = detectWhenPresent;
    }
    
    public List<String> getAliases() {
        return aliases;
    }
    
    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }
    
    /**
     * 指定されたアノテーション名がこのマッピングにマッチするかチェックします。
     * 
     * @param annotationName チェック対象のアノテーション名
     * @return マッチする場合true
     */
    public boolean matches(String annotationName) {
        if (annotation != null && annotation.equals(annotationName)) {
            return true;
        }
        if (aliases != null) {
            return aliases.contains(annotationName);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "AnnotationMapping{" +
                "annotation='" + annotation + '\'' +
                ", defaultFeature='" + defaultFeature + '\'' +
                ", classLevel=" + classLevel +
                ", featurePattern='" + featurePattern + '\'' +
                ", aliases=" + aliases +
                '}';
    }
}