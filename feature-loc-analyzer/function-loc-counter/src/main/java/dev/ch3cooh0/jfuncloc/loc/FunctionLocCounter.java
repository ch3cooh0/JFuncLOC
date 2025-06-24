package dev.ch3cooh0.jfuncloc.loc;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * Java ソースコードの関数・クラス単位のLOC（Lines of Code）計測機能を提供するクラス。
 * 
 * <p>このクラスはSpoonライブラリを使用してJavaソースコードを解析し、
 * 以下の単位でLOCを計測します：
 * <ul>
 *   <li><strong>関数レベル</strong>: メソッドとコンストラクタのLOC</li>
 *   <li><strong>クラスレベル</strong>: クラス全体のLOC（フィールド、メソッド、内部クラス、静的ブロックを含む）</li>
 * </ul>
 * 
 * <h3>LOC計測の詳細</h3>
 * <h4>関数レベル LOC に含まれるもの：</h4>
 * <ul>
 *   <li>メソッドのシグネチャから終了ブレースまで</li>
 *   <li>コンストラクタのシグネチャから終了ブレースまで</li>
 *   <li>抽象メソッドの場合は1行としてカウント</li>
 * </ul>
 * 
 * <h4>クラスレベル LOC に含まれるもの：</h4>
 * <ul>
 *   <li>フィールド宣言</li>
 *   <li>すべてのメソッド（継承元を除く）</li>
 *   <li>すべてのコンストラクタ</li>
 *   <li>内部クラス・インタフェース</li>
 *   <li>静的初期化ブロック</li>
 *   <li>匿名ブロック</li>
 * </ul>
 * 
 * <h3>パッケージフィルタリング</h3>
 * <p>対象パッケージを指定することで、特定のパッケージ配下のクラス・関数のみを
 * LOC計測の対象とすることができます。パッケージ名の前方一致で判定されます。
 * 
 * <h3>使用例</h3>
 * <pre>
 * FunctionLocCounter counter = new FunctionLocCounter();
 * 
 * // 全体のLOC計測（関数+クラス）
 * Map&lt;String, Integer&gt; allLoc = counter.count("/path/to/source");
 * 
 * // 関数のみのLOC計測
 * List&lt;String&gt; packages = Arrays.asList("com.example.service");
 * Map&lt;String, Integer&gt; functionLoc = counter.countFunctionLines("/path/to/source", packages);
 * 
 * // クラスのみのLOC計測
 * Map&lt;String, Integer&gt; classLoc = counter.countClassLines("/path/to/source", packages);
 * </pre>
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
public class FunctionLocCounter {
    
    /**
     * 指定されたパスの全クラス・関数のLOCを計測します。
     * 
     * <p>このメソッドは関数レベルLOCとクラスレベルLOCを結合した結果を返します。
     * パッケージフィルタリングは適用されません。
     * 
     * @param path ソースコードのディレクトリパス
     * @return クラス名/関数名をキー、LOCを値とするマップ
     */
    public Map<String, Integer> count(String path) {
        Map<String, Integer> functionLoc = countFunctionLines(path, Collections.emptyList());
        Map<String, Integer> classLoc = countClassLines(path, Collections.emptyList());
        
        Map<String, Integer> result = new HashMap<>();
        result.putAll(functionLoc);
        result.putAll(classLoc);
        
        return result;
    }
    
    /**
     * 指定されたパスの関数（メソッド・コンストラクタ）のLOCを計測します。
     * 
     * <p>対象パッケージが指定された場合、そのパッケージに属する
     * 関数のみが計測対象となります。
     * 
     * @param path ソースコードのディレクトリパス
     * @param targetPackages 対象パッケージのリスト（空の場合は全パッケージが対象）
     * @return 関数の完全修飾名をキー、LOCを値とするマップ
     */
    public Map<String, Integer> countFunctionLines(String path, List<String> targetPackages) {
        Map<String, Integer> result = new HashMap<>();
        
        try {
            Launcher launcher = new Launcher();
            launcher.addInputResource(path);
            launcher.getEnvironment().setAutoImports(true);
            launcher.getEnvironment().setCommentEnabled(false);
            
            CtModel model = launcher.buildModel();
            
            for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
                String packageName = method.getParent(CtPackage.class).getQualifiedName();
                
                if (targetPackages.isEmpty() || isTargetPackage(packageName, targetPackages)) {
                    String fqcn = getFqcnForMethod(method);
                    int loc = calculateMethodLoc(method);
                    result.put(fqcn, loc);
                }
            }
            
            for (CtConstructor<?> constructor : model.getElements(new TypeFilter<>(CtConstructor.class))) {
                String packageName = constructor.getParent(CtPackage.class).getQualifiedName();
                
                if (targetPackages.isEmpty() || isTargetPackage(packageName, targetPackages)) {
                    String fqcn = getFqcnForConstructor(constructor);
                    int loc = calculateConstructorLoc(constructor);
                    result.put(fqcn, loc);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing path: " + path + " - " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 指定されたパスのクラスのLOCを計測します。
     * 
     * <p>クラスLOCにはフィールド、メソッド、コンストラクタ、内部クラス、
     * 静的ブロックなどが含まれます。
     * 
     * @param path ソースコードのディレクトリパス
     * @param targetPackages 対象パッケージのリスト（空の場合は全パッケージが対象）
     * @return クラスの完全修飾名をキー、LOCを値とするマップ
     */
    public Map<String, Integer> countClassLines(String path, List<String> targetPackages) {
        Map<String, Integer> result = new HashMap<>();
        
        try {
            Launcher launcher = new Launcher();
            launcher.addInputResource(path);
            launcher.getEnvironment().setAutoImports(true);
            launcher.getEnvironment().setCommentEnabled(false);
            
            CtModel model = launcher.buildModel();
            
            for (CtType<?> type : model.getElements(new TypeFilter<>(CtType.class))) {
                if (type.getParent(CtPackage.class) == null) continue;
                
                String packageName = type.getParent(CtPackage.class).getQualifiedName();
                
                if (targetPackages.isEmpty() || isTargetPackage(packageName, targetPackages)) {
                    String className = type.getQualifiedName();
                    int loc = calculateClassLoc(type);
                    result.put(className, loc);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing path: " + path + " - " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean isTargetPackage(String packageName, List<String> targetPackages) {
        for (String targetPackage : targetPackages) {
            if (packageName.startsWith(targetPackage)) {
                return true;
            }
        }
        return false;
    }
    
    private String getFqcnForMethod(CtMethod<?> method) {
        CtType<?> declaringType = method.getDeclaringType();
        return declaringType.getQualifiedName() + "." + method.getSimpleName();
    }
    
    private String getFqcnForConstructor(CtConstructor<?> constructor) {
        CtType<?> declaringType = constructor.getDeclaringType();
        return declaringType.getQualifiedName() + ".<init>";
    }
    
    private int calculateMethodLoc(CtMethod<?> method) {
        if (method.getBody() == null) {
            return 1;
        }
        
        int startLine = method.getPosition().getLine();
        int endLine = method.getPosition().getEndLine();
        
        return Math.max(1, endLine - startLine + 1);
    }
    
    private int calculateConstructorLoc(CtConstructor<?> constructor) {
        if (constructor.getBody() == null) {
            return 1;
        }
        
        int startLine = constructor.getPosition().getLine();
        int endLine = constructor.getPosition().getEndLine();
        
        return Math.max(1, endLine - startLine + 1);
    }
    
    private int calculateClassLoc(CtType<?> type) {
        int totalLoc = 0;
        
        for (CtField<?> field : type.getFields()) {
            totalLoc += calculateFieldLoc(field);
        }
        
        for (CtMethod<?> method : type.getMethods()) {
            totalLoc += calculateMethodLoc(method);
        }
        
        for (CtConstructor<?> constructor : type.getTypeMembers().stream()
                .filter(member -> member instanceof CtConstructor)
                .map(member -> (CtConstructor<?>) member)
                .collect(java.util.stream.Collectors.toList())) {
            totalLoc += calculateConstructorLoc(constructor);
        }
        
        for (CtType<?> nestedType : type.getNestedTypes()) {
            totalLoc += calculateClassLoc(nestedType);
        }
        
        for (CtAnonymousExecutable anonymousBlock : type.getTypeMembers().stream()
                .filter(member -> member instanceof CtAnonymousExecutable)
                .map(member -> (CtAnonymousExecutable) member)
                .collect(java.util.stream.Collectors.toList())) {
            totalLoc += calculateAnonymousBlockLoc(anonymousBlock);
        }
        
        return Math.max(1, totalLoc);
    }
    
    private int calculateFieldLoc(CtField<?> field) {
        int startLine = field.getPosition().getLine();
        int endLine = field.getPosition().getEndLine();
        
        return Math.max(1, endLine - startLine + 1);
    }
    
    private int calculateAnonymousBlockLoc(CtAnonymousExecutable block) {
        int startLine = block.getPosition().getLine();
        int endLine = block.getPosition().getEndLine();
        
        return Math.max(1, endLine - startLine + 1);
    }
} 