package com.github.madz0.springbinder.binding.property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.github.madz0.springbinder.model.Model;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(of="name")
public abstract class IProperty {
    protected String name;
    public String getName(){
        return name;
    }

    public void serialize(Model<?> value, Map<String, PropertyWriter> propertyMap, JsonGenerator gen, SerializerProvider provider)
            throws Throwable{
        propertyMap.get(getName()).serializeAsField(value, gen, provider);
    }
}
