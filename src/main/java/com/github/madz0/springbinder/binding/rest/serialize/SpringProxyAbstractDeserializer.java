package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.AbstractDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;

import static com.github.madz0.springbinder.binding.rest.serialize.SpringProxyBeanDeserializer.parseForBeanNode;

@Slf4j
public class SpringProxyAbstractDeserializer extends AbstractDeserializer implements ResolvableDeserializer {
    private final JsonDeserializer deserializer;

    SpringProxyAbstractDeserializer(AbstractDeserializer deserializer, BeanDescription beanDesc) {
        super(beanDesc);
        this.deserializer = deserializer;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
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
}
