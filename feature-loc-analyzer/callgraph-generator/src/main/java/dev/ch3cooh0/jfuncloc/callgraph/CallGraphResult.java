package dev.ch3cooh0.jfuncloc.callgraph;

import java.util.Map;
import java.util.Set;

public class CallGraphResult {
    private final Map<String, Set<String>> callRelations;

    public CallGraphResult(Map<String, Set<String>> callRelations) {
        this.callRelations = callRelations;
    }

    public Map<String, Set<String>> getCallRelations() {
        return callRelations;
    }

    public Set<String> getCallees(String callerMethod) {
        return callRelations.getOrDefault(callerMethod, Set.of());
    }
} 