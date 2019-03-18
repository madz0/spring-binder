package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.model.IBaseModel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class SpringProxySerializer extends StdSerializer {

    private final JsonSerializer serializer;

    public SpringProxySerializer(Class<?> t, JsonSerializer beanSerializer) {
        super(t);
        this.serializer = beanSerializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if(!BaseGroups.IGroup.class.isAssignableFrom(BindUtils.group.get())) {
            serializer.serialize(value, gen, provider);
            return;
        }
        String beanName = null;
        Map<String, ?> beans = ContextAwareObjectMapper.getContext().getBeansOfType(value.getClass());
        for (String s : beans.keySet()) {
            Object bean = beans.get(s);
            if(bean == value){
                beanName = s;
                break;
            }
        }
        if(beanName == null){
            throw new IllegalStateException("can not find bean name of bean "+value);
        }

        gen.writeStartObject();
        gen.writeStringField("beanName", beanName);
        gen.writeStringField(IBaseModel.RECORD_TYPE_FIELD, "bean");
        gen.writeEndObject();
    }
}
