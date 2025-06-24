FROM node:20-bullseye

# Mavenのインストール
RUN apt-get update && \
    apt-get install -y maven git && \
    rm -rf /var/lib/apt/lists/*

# @anthropic-ai/claude-codeのグローバルインストール
RUN npm install -g @anthropic-ai/claude-code

# 作業ディレクトリの設定
WORKDIR /workspace

# Mavenの設定
ENV MAVEN_CONFIG=/workspace/.m2 