package com.example.loc;

import com.example.shared.FqcnUtils;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;

import java.util.HashMap;
import java.util.Map;

public class FunctionLocCounter {
    public Map<String, Integer> count(String sourceDir) {
        Map<String, Integer> result = new HashMap<>();
        Launcher launcher = new Launcher();
        launcher.addInputResource(sourceDir);
        CtModel model = launcher.buildModel();
        for (CtMethod<?> m : model.getElements(ct -> ct instanceof CtMethod)) {
            int loc = m.getPosition().getLine() == -1 ? 0 : m.getPosition().getEndLine() - m.getPosition().getLine() + 1;
            result.put(FqcnUtils.toFqcn(m.getDeclaringType().getQualifiedName(), m.getSimpleName()), loc);
        }
        return result;
    }
}
