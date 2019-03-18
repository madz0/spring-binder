package com.github.madz0.springbinder.binding.property;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.util.Set;

public class ComputedModelProperty extends IModelProperty {
    private Method method;
    private Set<IProperty> fields;

    @SneakyThrows
    private ComputedModelProperty(Class<?> clazz, String funcName, Set<IProperty> fields) {
        this.method = clazz.getMethod(funcName);
        this.name = BeanUtils.findPropertyForMethod(this.method).getName();
        this.fields = fields;
    }

    public static ComputedModelProperty of(Class<?> clazz, String funcName, Set<IProperty> fields){
        return new ComputedModelProperty(clazz, funcName, fields);
    }

    public Set<IProperty> getFields() {
        return fields;
    }
}
