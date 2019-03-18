package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.model.IBaseModel;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Slf4j
public class SpringProxyBeanDeserializer extends BeanDeserializer implements ResolvableDeserializer {
    private final JsonDeserializer deserializer;

    SpringProxyBeanDeserializer(BeanDeserializer deserializer) {
        super(deserializer);
        this.deserializer = deserializer;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        ImmutablePair<Object, JsonParser> parseResult = parseForBeanNode(p);
        if (parseResult.getKey() != null) {
            return parseResult.getKey();
        }
        return deserializer.deserialize(parseResult.getValue(), ctxt);
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        if (deserializer instanceof BeanDeserializer) {
            ((BeanDeserializer) deserializer).resolve(ctxt);
        }
    }


    public static ImmutablePair<Object, JsonParser> parseForBeanNode(JsonParser p) throws Exception {
        Object object = null;
        Class<? extends BaseGroups.IGroup> clazz = BindUtils.group.get();
        if (clazz != null && BaseGroups.IGroup.class.isAssignableFrom(clazz)) {
            JsonNode root = p.readValueAsTree();
            JsonNode recordType = root.get(IBaseModel.RECORD_TYPE_FIELD);
            if (recordType != null && recordType.isTextual() && recordType.asText().equals("bean")) {
                JsonNode beanName = root.get("beanName");
                if (beanName != null && beanName.isTextual()) {
                    object = ContextAwareObjectMapper.getContext().getBean(beanName.asText());
                } else {
                    throw new IllegalArgumentException("beanRecord without beanName :" + root.asText());
                }
            } else {
                p = new TreeTraversingParser(root, p.getCodec());
                p.nextToken();
            }
        }
        return new ImmutablePair<>(object,p);
    }
}
