package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.madz0.springbinder.binding.BindingUtils;
import java.io.IOException;

public class RestResultSerializer<T> extends StdSerializer<RestResultFactory<T>> {

    private BeanSerializer beanSerializer;

    public RestResultSerializer(BeanSerializer beanSerializer) {
        super(beanSerializer);
        this.beanSerializer = beanSerializer;
    }

    @Override
    public void serialize(RestResultFactory<T> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        BindingUtils.setGroup(provider, value.group);
        beanSerializer.serialize(value, gen, provider);
        //should remove the group?
    }
}
