package com.github.madz0.springbinder.binding.rest.serialize;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.Model;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;

@Slf4j
public class IdModelSerializer<T extends Model> extends StdSerializer<T> {

    private final Map<String, PropertyWriter> propertyMap;
    private final BeanSerializer serializer;

    public IdModelSerializer(Class<T> t, BeanSerializer beanSerializer) {
        super(t);
        this.serializer = beanSerializer;
        this.propertyMap = IteratorUtils.toList(beanSerializer.properties())
            .stream()
            .collect(Collectors.toMap(PropertyWriter::getName, x -> x));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (BindingUtils.group.get() == null) {
            serializer.serialize(value, gen, provider);
            return;
        }
        gen.writeStartObject();
        serializeFields(value, gen, provider);
        gen.writeEndObject();
    }

    private void serializeFields(T value, JsonGenerator gen, SerializerProvider provider) {
        Set<IProperty> properties = BindingUtils.peekProperties();
        if (properties == null) {
            properties = BindingUtils.getPropertiesOfCurrentGroup();
        }

        for (IProperty property : properties) {
            if (!propertyMap.containsKey(property.getName())) {
                continue;
            }
            try {
                property.serialize(value, propertyMap, gen, provider);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer)
        throws IOException {
        // note: method to call depends on whether this type is serialized as JSON scalar, object or Array!
        if (BindingUtils.group.get() == null) {
            serializer.serializeWithType(value, gen, provider, typeSer);
            return;
        }
        WritableTypeId typeId = typeSer.typeId(value, START_OBJECT);
        typeSer.writeTypePrefix(gen, typeId);
        serializeFields(value, gen, provider);
        typeSer.writeTypeSuffix(gen, typeId);
    }
}
