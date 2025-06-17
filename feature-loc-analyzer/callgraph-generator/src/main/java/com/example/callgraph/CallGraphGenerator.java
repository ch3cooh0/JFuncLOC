package com.example.callgraph;

import com.example.shared.FqcnUtils;
import soot.Scene;
import soot.options.Options;

import java.util.Collections;
import java.util.Set;

public class CallGraphGenerator {
    public Set<String> buildCallGraph(String sourceDir) {
        // Soot configuration placeholder
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(sourceDir));
        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg", "enabled:true");
        Scene.v().loadNecessaryClasses();
        // 実際のコールグラフ構築ロジックは割愛
        return Collections.emptySet();
    }

    public static String fqcn(String className, String methodName) {
        return FqcnUtils.toFqcn(className, methodName);
    }
}
