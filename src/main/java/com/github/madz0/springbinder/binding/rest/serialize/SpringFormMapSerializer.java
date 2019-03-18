package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpringFormMapSerializer<T, S> extends StdSerializer<Map<T, S>> {


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public class MapItem {
        private T key;
        private S value;
    }

    public SpringFormMapSerializer() {
        super(Map.class, false);
    }

    protected SpringFormMapSerializer(Class<Map<T, S>> t) {
        super(t);
    }


    @Override
    public void serialize(Map<T, S> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        List<MapItem> mapItems = new ArrayList<>();
        Set<Map.Entry<T, S>> entries = value.entrySet();
        entries.forEach(x -> mapItems.add(new MapItem(x.getKey(), x.getValue())));

        IterableSerializer iterableSerializer = new IterableSerializer(null, true, null);
        iterableSerializer.serialize(mapItems, gen, provider);
    }


}
