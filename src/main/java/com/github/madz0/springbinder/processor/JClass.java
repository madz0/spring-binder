package com.github.madz0.springbinder.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class only works if we add elements in proper sequence.
 */

public class JClass {

    public static final String LINE_BREAK = System.getProperty("line.separator");
    private StringBuilder builder = new StringBuilder();
    private String className;
    private Map<String, String> fields = new LinkedHashMap<>();

    public JClass() {

    }


    public JClass definePackage(String packageName) {
        if (packageName != null) {
            builder.append("package ")
                .append(packageName)
                .append(";")
                .append(LINE_BREAK);
        }
        return this;
    }

    public JClass addImport(String importPackage) {
        builder.append("import ")
            .append(importPackage)
            .append(";");
        return this;
    }

    public JClass defineClass(String startPart, String name, String extendPart) {
        className = name;
        builder.append(LINE_BREAK).append(LINE_BREAK)
            .append(startPart)
            .append(" ")
            .append(name);
        if (extendPart != null) {
            builder.append(" ")
                .append(extendPart);
        }

        builder.append(" {")
            .append(LINE_BREAK);
        return this;
    }

    public JClass addFields(Map<String, String> identifierToTypeMap) {
        for (Map.Entry<String, String> entry : identifierToTypeMap.entrySet()) {
            addField(entry.getValue(), entry.getKey());
        }
        return this;
    }

    public JClass addField(String type, String identifier) {
        fields.put(identifier, type);
        builder.append("public final static String ")
            .append(" ")
            .append(identifier.replaceAll("([a-z1-9])([A-Z]+)", "$1_$2").toUpperCase())
            .append(" = ")
            .append(" \"")
            .append(identifier)
            .append("\"")
            .append(";")
            .append(LINE_BREAK);

        return this;
    }

    public JClass addMethod(JMethod method) {
        builder.append(LINE_BREAK)
            .append(method.end())
            .append(LINE_BREAK);
        return this;
    }

    public JClass createGetterForField(String name) {
        if (!fields.containsKey(name)) {
            throw new IllegalArgumentException("Field not found for Getter: " + name);
        }
        addMethod(new JMethod()
            .defineSignature("public", false, fields.get(name))
            .name("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
            .defineBody(" return this." + name + ";"));
        return this;
    }

    public String end() {
        builder.append(LINE_BREAK + "}");
        return builder.toString();

    }
}