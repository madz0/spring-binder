package com.github.madz0.springbinder.binding.property;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.metamodel.Attribute;
import java.util.Set;

@Slf4j
public class ModelProperty extends IModelProperty {
    private Set<IProperty> fields;

    private ModelProperty(Attribute attribute, Set<IProperty> fields) {
        this.name = attribute.getName();
        this.fields = fields;
    }

    private ModelProperty(String name, Set<IProperty> fields) {
        this.name = name;
        this.fields = fields;
    }

    public static ModelProperty of(Attribute attribute, Set<IProperty> fields){
        return new ModelProperty(attribute, fields);
    }

    public static ModelProperty of(String name, Set<IProperty> fields){
        return new ModelProperty(name, fields);
    }

    public Set<IProperty> getFields() {
        return fields;
    }
}
