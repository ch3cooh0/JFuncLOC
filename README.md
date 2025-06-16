# JFuncLOC
機能単位でLOC計測
# Java CLI アプリケーション: 機能単位のLOC集計ツール

## 📦 プロジェクト概要

Soot(https://soot-oss.github.io/soot/) を用いて Java アプリケーションのコールグラフを解析し、機能単位の LOC（行数）を集計する CLI ツールを Maven プロジェクトとして実装してください。

---

## 🎯 要件

- Java 17 以上
- Maven 管理
- コマンドライン引数でモード切り替え可能
- jarファイル（対象アプリ）を静的解析
- JSONファイルで外部起点指定が可能（external/hybrid モード時）

---

## 🧩 モード仕様

```bash
# annotation モード
java -jar loc-tool.jar annotation <target-jar> [dependencies...]

# external / hybrid モード
java -jar loc-tool.jar <mode> <target-jar> <entrypoints.json> [dependencies...]
```

| パラメータ | 説明 |
|------------|------|
| `mode` | 実行モード（annotation/external/hybrid） |
| `target-jar` | 分析対象のjarファイル |
| `entrypoints.json` | external/hybrid モードで使用するJSONファイル |
| `dependencies...` | 分析対象のjarファイルの依存関係（オプション） |

| モード       | 説明 |
|--------------|------|
| `annotation` | アノテーション `@Function` に基づいて起点メソッドを抽出 |
| `external`   | 外部 JSON ファイルで指定されたメソッド群を起点として解析 |
| `hybrid`     | 両方を組み合わせて重複排除した起点群で解析 |

### 実行例

```bash
# アノテーションモードで実行（依存関係なし）
java -jar loc-tool.jar annotation target.jar

# 外部エントリーポイントモードで実行（依存関係あり）
java -jar loc-tool.jar external target.jar entrypoints.json dependency1.jar dependency2.jar

# ハイブリッドモードで実行（依存関係あり）
java -jar loc-tool.jar hybrid target.jar entrypoints.json dependency1.jar dependency2.jar
```

---

## 🔍 機能

- Soot によりコールグラフを構築
- 起点から到達可能なメソッドの LOC を合算
- 同一機能に複数起点があっても OK
- 未使用メソッド（コールグラフに到達しない）は除外
- 標準出力で `機能名: 有効LOC` を出力

---

## 📄 アノテーション仕様

```java
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Function {
    String value();
}
```

- クラス/メソッドのいずれかに付与可能
- クラスに付いていた場合はそのクラス内の `public` メソッドを起点候補とする

---

## 📄 JSONファイル仕様

```json
[
  {
    "function": "ユーザー出力",
    "class": "com.example.batch.ExportUserBatch",
    "method": "execute",
    "descriptor": "()V"
  }
]
```

| フィールド | 説明 |
|------------|------|
| `function` | 機能名（日本語も可） |
| `class`    | 完全修飾クラス名 |
| `method`   | メソッド名 |
| `descriptor` | JVMメソッド記述子（例：`(Ljava/lang/String;)V`） |

---

## 📦 依存ライブラリ（pom.xml に含める）

- `org.soot-oss:sootup.core`
- `org.soot-oss:sootup.java.core`
- `org.soot-oss:sootup.callgraph`
- `com.fasterxml.jackson.core:jackson-databind`

---

## 📁 出力例

```
機能: ユーザー出力, 有効LOC: 42
機能: 勤怠集計, 有効LOC: 85
```

---

## 📄 その他補助クラス

```java
record ExternalEntry(String function, String className, String method, String descriptor) {}
```

---

## 🔧 Codex への指示

この仕様に基づいて以下を含む Maven プロジェクトを作成してください：

- `@Function` アノテーション定義
- JSON読込みクラス
- SootUpを使った呼び出し解析とLOC集計ロジック
- CLIアプリ本体（mainメソッド）

## ⚠️ 注意事項

- 分析対象のjarファイルは、実行時のクラスパスに含まれている必要はありません
- 依存関係のjarファイルは、必要に応じて指定してください
- アノテーションモードでは、`@Function`アノテーションが付与されたクラスやメソッドがエントリーポイントとして使用されます
- 外部エントリーポイントモードでは、JSONファイルで指定されたメソッドがエントリーポイントとして使用されます
- ハイブリッドモードでは、アノテーションとJSONファイルの両方で指定されたエントリーポイントが使用されます
