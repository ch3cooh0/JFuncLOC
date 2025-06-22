package dev.ch3cooh0.jfuncloc.entry;

/**
 * エントリーポイント情報を保持するPOJOクラス。
 */
public class EntryPointInfo {
    private final String name;
    private final String className;
    private final String methodName;
    private final String description;

    public EntryPointInfo(String name, String className, String methodName, String description) {
        this.name = name;
        this.className = className;
        this.methodName = methodName;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDescription() {
        return description;
    }
} 