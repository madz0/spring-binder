package com.github.madz0.springbinder.binding.property;

import javax.persistence.metamodel.Attribute;

public class FieldProperty extends IProperty {
    private FieldProperty(Attribute attribute){
        this.name = attribute.getName();
    }
    private FieldProperty(String name){
        this.name = name;
    }

    public static FieldProperty of(Attribute attribute){
        return new FieldProperty(attribute);
    }
    public static FieldProperty of(String name){
        return new FieldProperty(name);
    }
}
