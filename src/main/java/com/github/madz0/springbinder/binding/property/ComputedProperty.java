package com.github.madz0.springbinder.binding.property;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class ComputedProperty extends IProperty {

    @SneakyThrows
    private ComputedProperty(Class<?> clazz, String funcName) {
        Method method = clazz.getMethod(funcName);
        PropertyDescriptor propertyForMethod = BeanUtils.findPropertyForMethod(method);
        if (propertyForMethod == null) {
            throw new IllegalStateException("computed method's name must start with get");
        }
        this.name = propertyForMethod.getName();
    }

    public static ComputedProperty of(Class<?> clazz, String funcName){
        return new ComputedProperty(clazz, funcName);
    }
}
