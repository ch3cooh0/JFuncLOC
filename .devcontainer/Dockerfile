FROM maven:3.9-eclipse-temurin-17

# 必要なパッケージのインストール
RUN apt-get update && apt-get install -y \
    git \
    && rm -rf /var/lib/apt/lists/*

# 作業ディレクトリの設定
WORKDIR /workspace

# Mavenの設定
ENV MAVEN_CONFIG=/workspace/.m2 