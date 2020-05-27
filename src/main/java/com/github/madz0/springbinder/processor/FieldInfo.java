package com.github.madz0.springbinder.processor;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

/**
 * Converts getters to field
 */
public class FieldInfo {

    private final Map<String, String> fields;

    public FieldInfo(Map<String, String> fields) {

        this.fields = fields;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public static FieldInfo get(Element element) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();

        for (ExecutableElement executableElement :
            ElementFilter.methodsIn(element.getEnclosedElements())) {

            if (executableElement.getKind() == ElementKind.METHOD) {
                String methodName = executableElement.getSimpleName().toString();

                String fieldName = methodToFieldName(methodName);
                if (fieldName != null) {
                    String returnType = executableElement.getReturnType().toString();
                    if (!"void".equals(returnType)) {
                        fields.put(fieldName, returnType);
                    }
                }
            }
        }

        return new FieldInfo(fields);
    }

    private static String methodToFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            String str = methodName.substring(3);
            if (str.length() == 0) {
                return null;
            } else if (str.length() == 1) {
                return str.toLowerCase();
            } else {
                return Character.toLowerCase(str.charAt(0)) + str.substring(1);
            }
        }
        return null;
    }
}