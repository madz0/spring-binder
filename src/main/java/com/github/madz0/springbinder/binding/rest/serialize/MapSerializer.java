package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Map;

public class MapSerializer<T, S> extends StdSerializer<Map<T, S>> {

    public MapSerializer() {
        super(Map.class, false);
    }

    protected MapSerializer(Class<Map<T, S>> t) {
        super(t);
    }

    @Override
    public void serialize(Map<T, S> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        IterableSerializer iterableSerializer = new IterableSerializer(null, true, null);
        iterableSerializer.serialize(value.entrySet(), gen, provider);
    }
}
