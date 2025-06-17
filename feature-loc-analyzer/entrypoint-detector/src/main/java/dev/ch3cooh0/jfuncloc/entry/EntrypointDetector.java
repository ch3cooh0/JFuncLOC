package dev.ch3cooh0.jfuncloc.entry;

import dev.ch3cooh0.jfuncloc.shared.ConfigLoader;
import dev.ch3cooh0.jfuncloc.shared.FqcnUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class EntrypointDetector {
    public Map<String, Set<String>> detectFromFile(File file) throws IOException {
        Map<String, Object> raw = ConfigLoader.load(file);
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            List<String> list = (List<String>) e.getValue();
            result.put(e.getKey(), new HashSet<>(list));
        }
        return result;
    }

    public Map<String, Set<String>> detectFromAnnotations(ClassLoader cl, String basePackage) throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        Enumeration<URL> resources = cl.getResources(basePackage.replace('.', '/'));
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            // クラスパス探索ロジックは簡略化
        }
        return result;
    }

    public static String fqcn(Method method) {
        return FqcnUtils.toFqcn(method.getDeclaringClass().getName(), method.getName());
    }
} 