# 機能単位LOC計測ツール

このリポジトリには Maven マルチモジュールプロジェクト **feature-loc-analyzer** が含まれています。各モジュールは Java 17 で動作し、Soot と Spoon を利用して関数単位の呼び出し関係と LOC を解析します。

## モジュール構成

- **shared-utils**: 共通ユーティリティ（FQCN生成、設定ファイル読み込み）
- **callgraph-generator**: Soot を用いたコールグラフ生成
- **function-loc-counter**: Spoon によるメソッド LOC 計測
- **entrypoint-detector**: `@EntryPoint` アノテーションや YAML/JSON から起点関数を検出
- **feature-loc-aggregator**: 上記モジュールを統合して機能別 LOC を集計する CLI

## ビルド方法

```bash
mvn clean install
```

## モジュール詳細

### callgraph-generator

Javaコードのコールグラフを生成するモジュールです。

#### 使用方法

```bash
java -jar callgraph-generator/target/callgraph-generator.jar <入力パス> [出力ファイル] [-p|--package <パッケージ名>]
```

##### 引数

- `<入力パス>`: .jarまたはclassファイルのディレクトリ（必須）
- `[出力ファイル]`: 出力するCSVファイルのパス（オプション、デフォルト: callgraph.csv）
- `-p, --package`: 対象とするパッケージ名（オプション）

##### 出力形式

CSVファイルに以下の形式で出力されます：
```
呼び出し元関数,呼び出し先関数
```

例：
```
com.example.ClassA.method1,com.example.ClassB.method2
com.example.ClassA.method1,com.example.ClassC.method3
```

#### エラー処理

- 入力パスが存在しない場合はエラーを出力して終了
- パッケージ名が無効な場合はエラーを出力して終了
- 出力ファイルは既存の場合も上書き

## 実行例

```bash
# 基本的な使用方法
java -jar callgraph-generator/target/callgraph-generator.jar /path/to/input

# 出力ファイルを指定
java -jar callgraph-generator/target/callgraph-generator.jar /path/to/input output.csv

# パッケージを指定
java -jar callgraph-generator/target/callgraph-generator.jar /path/to/input -p com.example
```

CSV 形式のレポート `feature-loc.csv` が生成されます。
