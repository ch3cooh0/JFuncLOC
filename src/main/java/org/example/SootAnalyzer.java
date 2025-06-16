package org.example;

import org.sootup.core.inputlocation.AnalysisInputLocation;
import org.sootup.core.inputlocation.AnalysisInputLocationMaker;
import org.sootup.core.model.SootMethod;
import org.sootup.core.Scene;
import org.sootup.java.core.JavaSootClassSource;
import org.sootup.callgraph.ClassHierarchyAnalysis;
import org.sootup.callgraph.CallGraph;

import java.io.File;
import java.util.*;

/**
 * SootUp を用いた呼び出し解析と LOC 集計を行うユーティリティクラス。
 */
public class SootAnalyzer {

    private final Scene scene;
    private final CallGraph callGraph;

    public SootAnalyzer(File jarFile) {
        AnalysisInputLocation location = AnalysisInputLocationMaker.make(jarFile.getAbsolutePath());
        this.scene = new Scene(location, List.of());
        this.callGraph = new ClassHierarchyAnalysis(scene).buildCallGraph();
    }

    public Scene getScene() {
        return scene;
    }

    public Set<SootMethod> reachableMethods(SootMethod entry) {
        Set<SootMethod> result = new HashSet<>();
        Deque<SootMethod> work = new ArrayDeque<>();
        work.push(entry);
        result.add(entry);
        while(!work.isEmpty()) {
            SootMethod m = work.pop();
            for(SootMethod tgt : callGraph.getCalleesOf(m)) {
                if(result.add(tgt)) {
                    work.push(tgt);
                }
            }
        }
        return result;
    }

    public int countLOC(Set<SootMethod> methods) {
        int loc = 0;
        for(SootMethod m : methods) {
            m.ensureActiveBody();
            loc += m.getBody().getUnits().size();
        }
        return loc;
    }
}
