package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.github.madz0.springbinder.model.IdModel;
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

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.setSerializerModifier(new ModelBeanSerializerModifier());
        simpleModule.setDeserializerModifier(new ModelBeanDeserializerModifier());
        this.registerModule(simpleModule);
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

    @SuppressWarnings("all")
    public static class ModelBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
            JsonSerializer<?> serializer) {
            if (IdModel.class.isAssignableFrom(beanDesc.getBeanClass()) &&
                serializer instanceof BeanSerializer) {
                return new IdModelSerializer(beanDesc.getBeanClass(), (BeanSerializer) serializer);
            }
            if (RestResultFactory.class.isAssignableFrom(beanDesc.getBeanClass())
                && serializer instanceof BeanSerializer) {
                return new RestResultSerializer((BeanSerializer) serializer);
            }
            return super.modifySerializer(config, beanDesc, serializer);
        }
    }

    @SuppressWarnings("unchecked")
    public static class ModelBeanDeserializerModifier extends BeanDeserializerModifier {

        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
            JsonDeserializer<?> deserializer) {
            ObjectMapper objectMapper = ContextAwareObjectMapper.getBean(ObjectMapper.class);
            if (IdModel.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new IdModelDeserializer(beanDesc.getBeanClass(), objectMapper, deserializer);
            }
            return super.modifyDeserializer(config, beanDesc, deserializer);
        }
    }
}
