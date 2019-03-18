package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.context.ApplicationContext;

public class ContextAwareObjectMapper extends ObjectMapper {
    private static ApplicationContext context;
    public ContextAwareObjectMapper(ApplicationContext context) {
        ContextAwareObjectMapper.context = context;
        this.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.setDateFormat(new StdDateFormat());

        SimpleModule sm = new SimpleModule();
        sm.setSerializerModifier(new BaseModelSerializer.BaseModelBeanSerializerModifier());
        sm.setDeserializerModifier(new BaseModelDeserializer.BaseModelBeanDeserializerModifier());
        this.registerModule(sm);
    }

    public static ApplicationContext getContext(){
        return context;
    }
    public static <T> T getBean(Class<T> clazz){
        return context.getBean(clazz);
    }
    public static <T> T getBean(String name, Class<T> clazz){
        return context.getBean(name, clazz);
    }
}
