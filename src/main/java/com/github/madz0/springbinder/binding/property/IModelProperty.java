package com.github.madz0.springbinder.binding.property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.model.IModel;

import java.util.Map;
import java.util.Set;

public abstract class IModelProperty extends IProperty {
    public abstract Set<IProperty> getFields();

    @Override
    public void serialize(IModel value, Map<String, PropertyWriter> propertyMap, JsonGenerator gen, SerializerProvider provider) throws Throwable {
        BindingUtils.pushPopProperties(getFields(),
                () -> propertyMap.get(getName()).serializeAsField(value, gen, provider)
        );

    }
}
