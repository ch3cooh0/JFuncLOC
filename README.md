# 機能単位LOC計測ツール

## 📖 概要

本ツールは、Javaプロジェクトにおける「機能」ごとのコード規模（LOC: Lines of Code）を自動集計します。  
1つの機能に対し、複数の起点関数を定義でき、それらから呼び出される関数のLOCを合算します。

## 🧩 構成コンポーネント

| モジュール名 | 説明 |
|-------------|------|
| `callgraph-generator` | Sootを使用してコールグラフを構築し、起点関数からの到達関数を抽出 |
| `function-loc-counter` | Spoonを使用してJava関数ごとのLOC（開始行〜終了行）を計測 |
| `entrypoint-detector` | 起点関数を以下の2通りで収集：<ul><li>外部定義ファイル（YAML/JSON）</li><li>アノテーション（例：`@EntryPoint("featureA")`）</li></ul> |
| `feature-loc-aggregator` | 起点関数 + コールグラフ + 関数LOCの情報を統合して、機能ごとの総LOCを集計 |

## 📂 入力

### 🔹 外部定義ファイル（YAML）

```yaml
featureA:
  - com.example.controller.FooController#getList
  - com.example.service.FooService#process
featureB:
  - com.example.batch.BatchMain#run
```

### 🔹 アノテーション

```java
@EntryPoint("featureA")
public void getList() { ... }
```

## 📤 出力

### 📄 機能別LOCレポート（CSV形式）

| 機能名      | 総LOC | 起点関数数 | 呼出関数数 |
|------------|-------|------------|------------|
| featureA   | 245   | 2          | 18         |
| featureB   | 130   | 1          | 10         |

## ⚙️ 設定オプション（予定）

- 除外対象クラス（標準ライブラリなど）
- オーバーロード判定に引数型も含めるか
- LOC重複の合算可否（純増・延べLOCの切替）

## 📦 依存ライブラリ

- Soot：コールグラフ構築用
- Spoon：ソースコードの静的解析とLOC測定用

## 🏁 実行方法

```bash
java -jar loc-feature-analyzer.jar --mode=analyze --source=./src --entry=entrypoints.yaml
```

## 📌 ライセンス

MIT License