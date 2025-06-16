package org.example;

import org.example.annotation.Function;
import org.sootup.core.model.SootClass;
import org.sootup.core.model.SootMethod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * CLI アプリ本体
 */
public class LocTool {

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.err.println("Usage: java -jar loc-tool.jar <target.jar> <mode> [entrypoints.json]");
            return;
        }
        File jar = new File(args[0]);
        String mode = args[1];
        File json = args.length >=3 ? new File(args[2]) : null;

        SootAnalyzer analyzer = new SootAnalyzer(jar);
        Map<String, List<SootMethod>> entries = new HashMap<>();

        if("annotation".equals(mode) || "hybrid".equals(mode)) {
            entries.putAll(findAnnotationEntries(analyzer));
        }
        if(("external".equals(mode) || "hybrid".equals(mode)) && json != null) {
            for(ExternalEntry e : EntryLoader.load(json)) {
                SootMethod m = analyzer.getScene().getMethod(e.className()+":"+e.method()+e.descriptor());
                if(m != null) {
                    entries.computeIfAbsent(e.function(), k->new ArrayList<>()).add(m);
                }
            }
        }

        for(var entry : entries.entrySet()) {
            int loc = 0;
            for(SootMethod m : entry.getValue()) {
                Set<SootMethod> reachable = analyzer.reachableMethods(m);
                loc += analyzer.countLOC(reachable);
            }
            System.out.printf("機能: %s, 有効LOC: %d%n", entry.getKey(), loc);
        }
    }

    private static Map<String, List<SootMethod>> findAnnotationEntries(SootAnalyzer analyzer) {
        Map<String, List<SootMethod>> map = new HashMap<>();
        for(SootClass cls : analyzer.getScene().getApplicationClasses()) {
            if(cls.hasAnnotation(Function.class.getName())) {
                String func = cls.getAnnotation(Function.class.getName()).getMemberValue("value").getValue().toString();
                for(SootMethod m : cls.getMethods()) {
                    if(Modifier.isPublic(m.getModifiers())) {
                        map.computeIfAbsent(func, k->new ArrayList<>()).add(m);
                    }
                }
            }
            for(SootMethod m : cls.getMethods()) {
                if(m.hasAnnotation(Function.class.getName())) {
                    String func = m.getAnnotation(Function.class.getName()).getMemberValue("value").getValue().toString();
                    map.computeIfAbsent(func, k->new ArrayList<>()).add(m);
                }
            }
        }
        return map;
    }
}
