package dev.ch3cooh0.jfuncloc.aggregator;

import dev.ch3cooh0.jfuncloc.callgraph.CallGraphGenerator;
import dev.ch3cooh0.jfuncloc.callgraph.CallGraphResult;
import dev.ch3cooh0.jfuncloc.entry.EntrypointDetector;
import dev.ch3cooh0.jfuncloc.loc.FunctionLocCounter;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeatureLocAggregator {
    private final CallGraphGenerator cg = new CallGraphGenerator();
    private final FunctionLocCounter counter = new FunctionLocCounter();
    private final EntrypointDetector detector = new EntrypointDetector();

    public List<String[]> aggregate(String source, File entryFile) throws IOException {
        Map<String, Set<String>> entry = detector.detectFromFile(entryFile);
        Map<String, Integer> locMap = counter.count(source);
        CallGraphResult callGraph = cg.buildCallGraph(source);
        List<String[]> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : entry.entrySet()) {
            String feature = e.getKey();
            Set<String> functions = e.getValue();
            int total = 0;
            for (String f : functions) {
                Integer loc = locMap.get(f);
                if (loc != null) {
                    total += loc;
                }
            }
            result.add(new String[]{feature, String.valueOf(total), String.valueOf(functions.size()), String.valueOf(callGraph.getCallRelations().size())});
        }
        return result;
    }
} 