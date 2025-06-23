# JFuncLOC - 機能単位LOC計測ツール

JFuncLOCは、Javaアプリケーションの機能別LOC（Lines of Code）計測を行うMavenマルチモジュールプロジェクトです。SpringFrameworkを含む一般的なJavaアプリケーションを対象とし、エントリーポイントから到達可能な関数・クラスのLOCを機能別に集計します。

## 特徴

- **エントリーポイント指定**: `@EntryPoint`アノテーションやYAML/JSONファイルで機能の起点を定義
- **コールグラフ解析**: Sootライブラリを使用してJAR/クラスファイルからコールグラフを生成
- **精密なLOC計測**: Spoonライブラリによるソースコード解析で関数・クラスレベルのLOC計測
- **パッケージフィルタリング**: 指定パッケージ配下のみを計測対象とする機能
- **CSV出力**: 機能別の集計結果をCSV形式で出力

## 動作環境

- Java 17以上
- Maven 3.6以上

## モジュール構成

| モジュール | 説明 | 主要技術 |
|-----------|------|----------|
| **shared-utils** | 共通ユーティリティ（FQCN生成、設定ファイル読み込み） | Jackson |
| **callgraph-generator** | コールグラフ生成 | Soot |
| **function-loc-counter** | 関数・クラス単位LOC計測 | Spoon |
| **entrypoint-detector** | エントリーポイント検出 | Reflection, Jackson |
| **feature-loc-aggregator** | 機能別LOC集計・統合CLI | PicoCLI |

## ビルド方法

```bash
cd feature-loc-analyzer
mvn clean install
```

## 基本的な使用方法

### 1. 機能定義ファイルの作成

機能とエントリーポイントをYAML形式で定義します：

```yaml
# features.yaml
features:
  user-management:
    name: "ユーザー管理機能"
    description: "ユーザーの作成、更新、削除、認証を行う機能"
    entry-points:
      - "com.example.controller.UserController.createUser"
      - "com.example.controller.UserController.updateUser"
      - "com.example.controller.UserController.deleteUser"
    packages:
      - "com.example.user"
      - "com.example.auth"
  
  order-processing:
    name: "注文処理機能"
    description: "商品の注文、決済、配送を管理する機能"
    entry-points:
      - "com.example.controller.OrderController.createOrder"
      - "com.example.controller.OrderController.processPayment"
    packages:
      - "com.example.order"
      - "com.example.payment"
      - "com.example.shipping"
```

### 2. 機能別LOC集計の実行

```bash
java -jar feature-loc-aggregator/target/feature-loc-aggregator.jar \
  --source /path/to/source \
  --jar /path/to/application.jar \
  --entry features.yaml \
  --output feature-loc-result.csv
```

### 3. 出力結果

```csv
機能名,機能説明,エントリーポイント数,対象クラス数,対象関数数,クラス総LOC,関数総LOC,コールグラフエッジ数
ユーザー管理機能,ユーザーの作成更新削除認証,3,15,45,850,650,125
注文処理機能,商品の注文決済配送管理,2,12,38,720,580,98
```

## 個別モジュールの使用方法

### callgraph-generator

JARファイルまたはクラスファイルからコールグラフを生成します。

#### 使用方法

```bash
java -jar callgraph-generator/target/callgraph-generator.jar [オプション]
```

#### オプション

- `-i, --input <パス>`: JARファイルまたはクラスファイルディレクトリ（必須）
- `-o, --output <パス>`: 出力CSVファイルパス（デフォルト: callgraph.csv）
- `-p, --package <名前>`: 対象パッケージ名（複数指定可能）

#### 出力形式

```csv
呼び出し元関数,呼び出し先関数
com.example.ClassA.method1,com.example.ClassB.method2
com.example.ClassA.method1,com.example.ClassC.method3
```

#### 実行例

```bash
# 基本的な使用方法
java -jar callgraph-generator/target/callgraph-generator.jar -i app.jar

# パッケージを指定
java -jar callgraph-generator/target/callgraph-generator.jar -i app.jar -p com.example.service

# 出力ファイルを指定
java -jar callgraph-generator/target/callgraph-generator.jar -i app.jar -o my-callgraph.csv
```

### function-loc-counter

Javaソースコードから関数・クラス単位のLOCを計測します。

#### LOC計測の対象

**関数レベルLOC:**
- メソッドのシグネチャから終了ブレースまで
- コンストラクタのシグネチャから終了ブレースまで
- 抽象メソッドは1行としてカウント

**クラスレベルLOC:**
- フィールド宣言
- すべてのメソッド（継承元を除く）
- すべてのコンストラクタ
- 内部クラス・インタフェース
- 静的初期化ブロック
- 匿名実行ブロック

#### 使用方法

```bash
java -jar function-loc-counter/target/function-loc-counter.jar [オプション]
```

#### プログラマティック使用例

```java
FunctionLocCounter counter = new FunctionLocCounter();

// 全体のLOC計測（関数+クラス）
Map<String, Integer> allLoc = counter.count("/path/to/source");

// 関数のみのLOC計測（パッケージフィルタ付き）
List<String> packages = Arrays.asList("com.example.service");
Map<String, Integer> functionLoc = counter.countFunctionLines("/path/to/source", packages);

// クラスのみのLOC計測
Map<String, Integer> classLoc = counter.countClassLines("/path/to/source", packages);
```

### entrypoint-detector

エントリーポイントの検出を2つの方法で行います。

#### 1. アノテーションベース検出

`@EntryPoint`アノテーションが付与されたメソッドを検出：

```java
@RestController
public class UserController {
    
    @EntryPoint("user-management")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }
    
    @EntryPoint("user-management")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
}
```

#### 2. ファイルベース検出

YAML/JSONファイルから機能定義を読み込み：

```yaml
features:
  user-management:
    name: "ユーザー管理機能"
    entry-points:
      - "com.example.controller.UserController.createUser"
      - "com.example.controller.UserController.updateUser"
    packages:
      - "com.example.user"
```

JSON形式でも同様に定義可能です。

#### プログラマティック使用例

```java
EntrypointDetector detector = new EntrypointDetector();

// ファイルから機能定義を読み込み
File configFile = new File("features.yaml");
Map<String, FeatureConfig> configs = detector.detectFromFile(configFile);

// アノテーションからエントリーポイントを検出
List<String> packages = Arrays.asList("com.example");
Map<String, Set<String>> annotations = detector.detectFromAnnotations("app.jar", packages);
```

### feature-loc-aggregator

すべてのモジュールを統合して機能別LOC集計を実行します。

#### 使用方法

```bash
java -jar feature-loc-aggregator/target/feature-loc-aggregator.jar [オプション]
```

#### オプション

- `--source <パス>`: ソースコードディレクトリ（必須）
- `--jar <パス>`: JARファイルまたはクラスファイルディレクトリ（必須）
- `--entry <パス>`: 機能定義ファイル（YAML/JSON形式、必須）
- `--output <パス>`: 出力CSVファイルパス（デフォルト: feature-loc.csv）

#### 処理フロー

1. 機能定義ファイルから機能設定とエントリーポイントを読み込み
2. ソースコードから関数・クラスのLOCを計測
3. JARファイルからコールグラフを生成
4. エントリーポイントから到達可能な関数・クラスを特定
5. 機能別にLOCを集計してCSV形式で出力

## SpringFrameworkとの連携

JFuncLOCはSpringFrameworkアプリケーションでの使用に最適化されています：

- **@RestController、@Service等のコンポーネントに@EntryPointアノテーションを併用**
- **Spring BootアプリケーションのJARファイルを直接解析可能**
- **パッケージ構造に基づく機能分離に対応**

## エラー処理

- 入力パスが存在しない場合はエラーを出力して終了
- パッケージ名が無効な場合はエラーを出力して終了
- 機能定義ファイルの形式が不正な場合はエラーを出力
- 出力ファイルは既存の場合上書き

## ライセンス

このプロジェクトのライセンス情報については、LICENSEファイルを参照してください。
