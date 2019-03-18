package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpringFormMapDeserializer extends StdDeserializer<Map> implements ContextualDeserializer {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class MapItem<T, S> {
        private T key;
        private S value;
    }

    private JavaType keyType;
    private JavaType contentType;

    public SpringFormMapDeserializer() {
        super((Class)null);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        keyType = property.getType().getKeyType();
        contentType = property.getType().getContentType();
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = ContextAwareObjectMapper.getBean(ObjectMapper.class);

        Map result = new HashMap<>();

        JsonNode root = p.getCodec().readTree(p);

        if(root.isArray()){
            for (JsonNode node : root) {
                JsonParser nodeParser = new TreeTraversingParser(node, p.getCodec());
                JavaType type = objectMapper.getTypeFactory().constructParametricType(MapItem.class, keyType, contentType);
                MapItem item = objectMapper.readValue(nodeParser, type);
                result.put(item.key, item.value);
            }
        }else{
            throw new IllegalStateException("must be array");
        }
        return result;
    }


}
