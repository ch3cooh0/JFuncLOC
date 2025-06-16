package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.RefType;
import soot.ShortType;
import soot.Type;

/**
 * JVMメソッド記述子を解析してSootのパラメータ型リストを生成するユーティリティ。
 */
public class DescriptorUtils {
    /**
     * メソッド記述子からパラメータ型リストを取得する。
     *
     * @param descriptor JVMメソッド記述子
     * @return パラメータ型のリスト
     */
    public static List<Type> parseParameterTypes(String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            return Collections.emptyList();
        }
        int start = descriptor.indexOf('(');
        int end = descriptor.indexOf(')');
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
        }
        String params = descriptor.substring(start + 1, end);
        List<Type> types = new ArrayList<>();
        for (int i = 0; i < params.length();) {
            int arrayDim = 0;
            char c = params.charAt(i);
            while (c == '[') {
                arrayDim++;
                i++;
                c = params.charAt(i);
            }
            Type baseType;
            switch (c) {
                case 'B' -> baseType = ByteType.v();
                case 'C' -> baseType = CharType.v();
                case 'D' -> baseType = DoubleType.v();
                case 'F' -> baseType = FloatType.v();
                case 'I' -> baseType = IntType.v();
                case 'J' -> baseType = LongType.v();
                case 'S' -> baseType = ShortType.v();
                case 'Z' -> baseType = BooleanType.v();
                case 'L' -> {
                    int semi = params.indexOf(';', i);
                    if (semi == -1) {
                        throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
                    }
                    String cls = params.substring(i + 1, semi).replace('/', '.');
                    baseType = RefType.v(cls);
                    i = semi; // ';' の位置まで進める
                }
                default -> throw new IllegalArgumentException("Unsupported descriptor char: " + c);
            }
            i++; // 現在の型記述を読み飛ばす
            Type t = baseType;
            if (arrayDim > 0) {
                t = ArrayType.v(baseType, arrayDim);
            }
            types.add(t);
        }
        return types;
    }
}
