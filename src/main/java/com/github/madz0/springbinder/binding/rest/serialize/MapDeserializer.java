package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapDeserializer extends StdDeserializer<Map<?, ?>> implements ContextualDeserializer {

    private JavaType keyType;
    private JavaType contentType;

    public MapDeserializer() {
        super((Class<?>) null);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        keyType = property.getType().getKeyType();
        contentType = property.getType().getContentType();
        return this;
    }

    @Override
    public Map<Object, Object> deserialize(JsonParser p, DeserializationContext context) throws IOException {
        ObjectMapper objectMapper = ContextAwareObjectMapper.getBean(ObjectMapper.class);
        Map<Object, Object> result = new HashMap<>();
        JsonNode root = p.getCodec().readTree(p);

        if (root.isArray()) {
            for (JsonNode node : root) {
                JsonParser nodeParser = new TreeTraversingParser(node, p.getCodec());
                JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(Entry.class, keyType, contentType);
                Entry<Object, Object> item = objectMapper.readValue(nodeParser, type);
                result.put(item.getKey(), item.getValue());
            }
        } else {
            throw new IllegalStateException("Wrong element type for map");
        }
        return result;
    }
}
