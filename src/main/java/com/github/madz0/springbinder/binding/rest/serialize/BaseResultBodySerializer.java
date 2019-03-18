package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.madz0.springbinder.binding.BindUtils;

import java.io.IOException;

public class BaseResultBodySerializer extends StdSerializer<RestResultFactory> {
    private BeanSerializer beanSerializer;
    public BaseResultBodySerializer(BeanSerializer beanSerializer) {
        super(beanSerializer);
        this.beanSerializer = beanSerializer;
    }

    @Override
    public void serialize(RestResultFactory value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        BindUtils.group.set(value.group);
        beanSerializer.serialize(value, gen, provider);
        BindUtils.group.remove();
    }
}
