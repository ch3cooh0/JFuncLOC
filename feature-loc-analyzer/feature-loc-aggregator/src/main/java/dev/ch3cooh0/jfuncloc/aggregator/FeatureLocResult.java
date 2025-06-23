package dev.ch3cooh0.jfuncloc.aggregator;

/**
 * 機能別LOC集計結果を保持するデータクラス。
 * 
 * <p>このクラスは、単一の機能に関するLOC計測結果をカプセル化します。
 * 以下の情報が含まれます：
 * <ul>
 *   <li>機能の基本情報（名前、説明）</li>
 *   <li>エントリーポイント数</li>
 *   <li>対象となるクラス・関数の数</li>
 *   <li>クラス・関数のLOC合計</li>
 *   <li>コールグラフのエッジ数</li>
 * </ul>
 * 
 * <p>このクラスはイミュータブルであり、インスタンス生成後に状態を変更することはできません。
 * CSV出力機能も提供し、集計結果の可視化をサポートします。
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
public class FeatureLocResult {
    private final String featureName;
    private final String featureDescription;
    private final int entryPointCount;
    private final int targetClassCount;
    private final int targetFunctionCount;
    private final int totalClassLoc;
    private final int totalFunctionLoc;
    private final int callGraphEdgeCount;
    
    /**
     * FeatureLocResultのコンストラクタ。
     * 
     * @param featureName 機能名
     * @param featureDescription 機能の説明
     * @param entryPointCount エントリーポイントの数
     * @param targetClassCount 対象クラスの数
     * @param targetFunctionCount 対象関数の数
     * @param totalClassLoc クラスの総LOC数
     * @param totalFunctionLoc 関数の総LOC数
     * @param callGraphEdgeCount コールグラフのエッジ数
     */
    public FeatureLocResult(String featureName, String featureDescription, 
                           int entryPointCount, int targetClassCount, int targetFunctionCount,
                           int totalClassLoc, int totalFunctionLoc, int callGraphEdgeCount) {
        this.featureName = featureName;
        this.featureDescription = featureDescription;
        this.entryPointCount = entryPointCount;
        this.targetClassCount = targetClassCount;
        this.targetFunctionCount = targetFunctionCount;
        this.totalClassLoc = totalClassLoc;
        this.totalFunctionLoc = totalFunctionLoc;
        this.callGraphEdgeCount = callGraphEdgeCount;
    }
    
    public String getFeatureName() {
        return featureName;
    }
    
    public String getFeatureDescription() {
        return featureDescription;
    }
    
    public int getEntryPointCount() {
        return entryPointCount;
    }
    
    public int getTargetClassCount() {
        return targetClassCount;
    }
    
    public int getTargetFunctionCount() {
        return targetFunctionCount;
    }
    
    public int getTotalClassLoc() {
        return totalClassLoc;
    }
    
    public int getTotalFunctionLoc() {
        return totalFunctionLoc;
    }
    
    public int getCallGraphEdgeCount() {
        return callGraphEdgeCount;
    }
    
    /**
     * この結果をCSV行として表現する文字列配列を返します。
     * 
     * @return CSV行を表す文字列配列
     */
    public String[] toCsvRow() {
        return new String[]{
            featureName,
            featureDescription != null ? featureDescription : "",
            String.valueOf(entryPointCount),
            String.valueOf(targetClassCount),
            String.valueOf(targetFunctionCount),
            String.valueOf(totalClassLoc),
            String.valueOf(totalFunctionLoc),
            String.valueOf(callGraphEdgeCount)
        };
    }
    
    /**
     * CSV出力用のヘッダー行を返します。
     * 
     * @return CSVヘッダーを表す文字列配列
     */
    public static String[] getCsvHeader() {
        return new String[]{
            "機能名",
            "機能説明", 
            "エントリーポイント数",
            "対象クラス数",
            "対象関数数",
            "クラス総LOC",
            "関数総LOC",
            "コールグラフエッジ数"
        };
    }
    
    @Override
    public String toString() {
        return "FeatureLocResult{" +
                "featureName='" + featureName + '\'' +
                ", featureDescription='" + featureDescription + '\'' +
                ", entryPointCount=" + entryPointCount +
                ", targetClassCount=" + targetClassCount +
                ", targetFunctionCount=" + targetFunctionCount +
                ", totalClassLoc=" + totalClassLoc +
                ", totalFunctionLoc=" + totalFunctionLoc +
                ", callGraphEdgeCount=" + callGraphEdgeCount +
                '}';
    }
}