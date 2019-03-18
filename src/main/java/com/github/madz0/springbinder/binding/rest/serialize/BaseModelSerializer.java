package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.IBaseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

@Slf4j
public class BaseModelSerializer<T extends IBaseModel> extends StdSerializer<T> {

    private final Map<String, PropertyWriter> propertyMap;
    private final BeanSerializer serializer;

    public BaseModelSerializer(Class<T> t, BeanSerializer beanSerializer) {
        super(t);
        this.serializer = beanSerializer;
        this.propertyMap = IteratorUtils.toList(beanSerializer.properties())
                .stream()
                .collect(Collectors.toMap(PropertyWriter::getName, x -> x));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (BindUtils.group.get() == null) {
            serializer.serialize(value, gen, provider);
            return;
        }
        gen.writeStartObject();
        serializeFields(value, gen, provider);
        gen.writeEndObject();
    }

    private void serializeFields(T value, JsonGenerator gen, SerializerProvider provider) {
        Set<IProperty> properties = BindUtils.peekProperties();
        if (properties == null) {
            properties = BindUtils.getPropertiesOfCurrentGroup();
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
    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        // note: method to call depends on whether this type is serialized as JSON scalar, object or Array!
        if (BindUtils.group.get() == null) {
            serializer.serializeWithType(value, gen, provider, typeSer);
            return;
        }
        WritableTypeId typeId = typeSer.typeId(value, START_OBJECT);
        typeSer.writeTypePrefix(gen, typeId);
        serializeFields(value, gen, provider);
        typeSer.writeTypeSuffix(gen, typeId);
    }

    @SuppressWarnings("unchecked")
    public static class BaseModelBeanSerializerModifier extends BeanSerializerModifier {
        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (IBaseModel.class.isAssignableFrom(beanDesc.getBeanClass()) && serializer instanceof BeanSerializer) {
                return new BaseModelSerializer(beanDesc.getBeanClass(), (BeanSerializer) serializer);
            }
            if (RestResultFactory.class.isAssignableFrom(beanDesc.getBeanClass()) && serializer instanceof BeanSerializer) {
                return new BaseResultBodySerializer((BeanSerializer) serializer);
            }
            if (ContextAwareObjectMapper.getContext().getBeansOfType(beanDesc.getBeanClass()).size() > 0) {
                return new SpringProxySerializer(beanDesc.getBeanClass(), serializer);
            }
            return super.modifySerializer(config, beanDesc, serializer);
        }
    }
}
