package dev.ch3cooh0.jfuncloc.entry;

import java.util.List;

/**
 * 機能定義設定を保持するデータクラス。
 * 
 * <p>このクラスは、YAML/JSONファイルから読み込まれる機能定義情報を格納します。
 * 各機能は以下の情報を持ちます：
 * <ul>
 *   <li><strong>name</strong>: 機能の表示名</li>
 *   <li><strong>description</strong>: 機能の詳細説明</li>
 *   <li><strong>entryPoints</strong>: 機能のエントリーポイントとなる関数のFQCNリスト</li>
 *   <li><strong>packages</strong>: LOC計測対象となるパッケージのリスト</li>
 * </ul>
 * 
 * <h3>使用例</h3>
 * <pre>
 * FeatureConfig config = new FeatureConfig();
 * config.setName("ユーザー管理機能");
 * config.setDescription("ユーザーの作成、更新、削除を行う機能");
 * config.setEntryPoints(Arrays.asList("com.example.UserController.createUser"));
 * config.setPackages(Arrays.asList("com.example.user"));
 * </pre>
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
public class FeatureConfig {
    private String name;
    private String description;
    private List<String> entryPoints;
    private List<String> packages;
    
    /**
     * デフォルトコンストラクタ。
     * JSON/YAMLデシリアライゼーションで使用されます。
     */
    public FeatureConfig() {}
    
    /**
     * 全フィールドを指定するコンストラクタ。
     * 
     * @param name 機能名
     * @param description 機能の説明
     * @param entryPoints エントリーポイント関数のFQCNリスト
     * @param packages 対象パッケージのリスト
     */
    public FeatureConfig(String name, String description, List<String> entryPoints, List<String> packages) {
        this.name = name;
        this.description = description;
        this.entryPoints = entryPoints;
        this.packages = packages;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getEntryPoints() {
        return entryPoints;
    }
    
    public void setEntryPoints(List<String> entryPoints) {
        this.entryPoints = entryPoints;
    }
    
    public List<String> getPackages() {
        return packages;
    }
    
    public void setPackages(List<String> packages) {
        this.packages = packages;
    }
    
    @Override
    public String toString() {
        return "FeatureConfig{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", entryPoints=" + entryPoints +
                ", packages=" + packages +
                '}';
    }
}