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

## 実行例

```bash
java -jar feature-loc-aggregator/target/feature-loc-aggregator.jar --source=src --entry=entrypoints.yaml
```

CSV 形式のレポート `feature-loc.csv` が生成されます。
