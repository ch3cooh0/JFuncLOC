package dev.ch3cooh0.jfuncloc.entry;

/**
 * アノテーションとエントリーポイント検出ルールのマッピング設定。
 * 
 * <p>このクラスは、検出対象のアノテーションとそのアノテーションから
 * 機能名を抽出するルールを定義します。
 */
public class AnnotationMapping {
    
    /** アノテーションのFQCN */
    private String annotation;
    
    /** 機能名を取得するアノテーションの属性名 */
    private String featureAttribute;
    
    /** デフォルトの機能名 */
    private String defaultFeature;
    
    /** クラスレベルのアノテーションかどうか */
    private boolean classLevel = false;
    
    /** 機能名生成パターン */
    private String featurePattern;
    
    /** アノテーションが存在する場合に検出するかどうか */
    private boolean detectWhenPresent = true;
    
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
    
    @Override
    public String toString() {
        return "AnnotationMapping{" +
                "annotation='" + annotation + '\'' +
                ", defaultFeature='" + defaultFeature + '\'' +
                ", classLevel=" + classLevel +
                '}';
    }
}