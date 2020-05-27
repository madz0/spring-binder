package com.github.madz0.springbinder.processor;

import java.util.SortedMap;

public class JMethod {
    private StringBuilder builder = new StringBuilder();
    private boolean firstParam = true;
    private boolean forInterface;

    public JMethod forInterface() {
        forInterface = true;
        return this;
    }

    public JMethod defineSignature(String accessModifier, boolean asStatic, String returnType) {
        builder.append(forInterface ? "" : accessModifier)
                .append(asStatic? " static ": " ")
                .append(returnType)
                .append(" ");
        return this;
    }

    public JMethod name(String name) {
        builder.append(name)
                .append("(");
        return this;
    }

    public JMethod addParam(String type, String identifier) {
        if (!firstParam) {
            builder.append(", ");
        } else {
            firstParam = false;
        }
        builder.append(type)
                .append(" ")
                .append(identifier);

        return this;
    }

    public JMethod defineBody(String body) {
        if (forInterface) {
            throw new IllegalArgumentException("Interface cannot define a body");
        }
        builder.append(") {")
                .append(JClass.LINE_BREAK)
                .append(body)
                .append(JClass.LINE_BREAK)
                .append("}")
                .append(JClass.LINE_BREAK);
        return this;
    }

    public String end() {
        return forInterface ? ");" : builder.toString();
    }
}