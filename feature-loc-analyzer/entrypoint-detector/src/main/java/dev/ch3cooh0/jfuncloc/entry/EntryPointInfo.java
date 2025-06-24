package dev.ch3cooh0.jfuncloc.entry;

/**
 * エントリーポイント情報を保持するPOJOクラス。
 */
public class EntryPointInfo {
    private final String name;
    private final String className;
    private final String methodName;
    private final String description;

    public EntryPointInfo(String name, String fqcn) {
        this.name = name;
        int lastDotIndex = fqcn.lastIndexOf('.');
        if (lastDotIndex > 0) {
            this.className = fqcn.substring(0, lastDotIndex);
            this.methodName = fqcn.substring(lastDotIndex + 1);
        } else {
            this.className = fqcn;
            this.methodName = "";
        }
        this.description = "";
    }

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
    
    public String getFqcn() {
        return className + "." + methodName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntryPointInfo that = (EntryPointInfo) obj;
        return name.equals(that.name) && className.equals(that.className) && methodName.equals(that.methodName);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + className.hashCode() * 17 + methodName.hashCode();
    }
    
    @Override
    public String toString() {
        return "EntryPointInfo{" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 