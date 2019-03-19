package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.model.IBaseModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import ognl.OgnlOps;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class BaseModelDeserializer<T extends IBaseModel> extends StdDeserializer<T> implements ResolvableDeserializer {
    private final ObjectMapper objectMapper;
    private final JsonDeserializer deserializer;
    private final BeanDeserializer beanDeserializer;
    private final BeanWrapperImpl beanWrapper;
    private final EntityManager em;

    BaseModelDeserializer(Class<?> vc, ObjectMapper objectMapper, JsonDeserializer deserializer) {
        super(vc);
        this.objectMapper = objectMapper;
        this.deserializer = deserializer;
        if (deserializer instanceof BeanDeserializer) {
            this.beanDeserializer = (BeanDeserializer) deserializer;
            this.beanWrapper = BindUtils.getBeanWrapper(vc);
        } else {
            this.beanDeserializer = null;
            this.beanWrapper = null;
        }
        this.em = ContextAwareObjectMapper.getBean(EntityManager.class);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        if (BindUtils.group.get() == null) {
            return (T) deserializer.deserialize(p, ctxt);
        }
        JsonNode root = p.getCodec().readTree(p);
        if (BaseGroups.IDto.class.isAssignableFrom(BindUtils.group.get())) {
            JsonNode clazzNode = root.get(IBaseModel.RECORD_TYPE_FIELD);
            JsonNode idNode = root.get(IBaseModel.ID_FIELD);
            if (clazzNode.isTextual() && idNode.isTextual()) {
                String clazzName = clazzNode.asText();
                Object id = getIdByType(BindUtils.idClass.get(), idNode.asText());
                Class<?> clazz = Class.forName(clazzName);
                return (T) em.getReference(clazz, id);
            }
            throw new IllegalStateException("can not determine id or class");
        }
        T value;
        if (BindUtils.updating.get()) {
            JsonNode idNode = root.get(IBaseModel.ID_FIELD);
            try {
                Object id = getIdByType(BindUtils.idClass.get(), idNode.asText());
                value = (T) em.find(_valueClass, id);
            } catch (Exception e) {
                throw new InvalidDataAccessApiUsageException(IBaseModel.ID_FIELD);
            }
            if (value == null) {
                throw new InvalidDataAccessApiUsageException(IBaseModel.ID_FIELD);
            }
        } else {
            if (beanDeserializer != null) {
                value = (T) beanDeserializer.getValueInstantiator().createUsingDefault(ctxt);
            } else {
                value = (T) BindUtils.createModel(_valueClass);
            }
        }
        return bind(p, root, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt, T value) throws IOException {
        if (BindUtils.group.get() == null) {
            return (T) deserializer.deserialize(p, ctxt);
        }
        JsonNode root = p.getCodec().readTree(p);
        return bind(p, root, value);
    }

    @SuppressWarnings("unchecked")
    private T bind(JsonParser p, JsonNode root, T value) throws IOException {
        if (beanDeserializer == null) {
            throw new IllegalStateException("can not bind into non bean using BaseModelDeserializer");
        }
        Set<IProperty> properties = BindUtils.peekProperties();
        if (properties == null) {
            properties = BindUtils.getPropertiesOfCurrentGroup();
        }
        for (IProperty property : properties) {
            JsonNode node = root.get(property.getName());
            SettableBeanProperty beanProperty = beanDeserializer.findProperty(property.getName());
            if (beanProperty == null) {
                continue;
            }
            setPropertyValue(p, beanProperty, node, property, value);
        }
        return value;
    }

    private void bindRecursive(T value, JsonNode node, SettableBeanProperty beanProperty, IModelProperty modelProperty)
            throws IOException, IllegalAccessException, InvocationTargetException {
        //bind recursive
        if (node == null) {
            beanProperty.set(value, null);
        } else {
            Object model = beanWrapper.getPropertyDescriptor(modelProperty.getName()).getReadMethod().invoke(value);
            if (model == null) {
                model = BindUtils.createModel(beanProperty.getType().getRawClass());
            }
            final Object finalModel = model;
            try {
                BindUtils.pushPopProperties(modelProperty.getFields(),
                        () -> objectMapper.readerForUpdating(finalModel).readValue(node)
                );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            beanProperty.set(value, model);
        }
    }

    private boolean matchId(JsonNode node, Object model) {
        if (model instanceof IBaseModel) {
            IBaseModel baseModel = (IBaseModel) model;
            return baseModel.getId() != null && Objects.equals(getId(node), baseModel.getId());
        }
        return false;
    }

    private Object getId(JsonNode node) {
        if (node != null) {
            JsonNode idNode = node.get(IBaseModel.ID_FIELD);
            if (idNode != null) {
                return getIdByType(BindUtils.idClass.get(), idNode.textValue());
            }
        }
        return null;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        if (beanDeserializer != null) {
            beanDeserializer.resolve(ctxt);
        }
    }

    @SuppressWarnings("unchecked")
    public static class BaseModelBeanDeserializerModifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
            ObjectMapper objectMapper = ContextAwareObjectMapper.getBean(ObjectMapper.class);
            if (IBaseModel.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new BaseModelDeserializer(beanDesc.getBeanClass(), objectMapper, deserializer);
            }
            if (deserializer instanceof BeanDeserializer) {
                return new SpringProxyBeanDeserializer((BeanDeserializer) deserializer);
            }
            if (deserializer instanceof AbstractDeserializer) {
                return new SpringProxyAbstractDeserializer((AbstractDeserializer) deserializer, beanDesc);
            }
            return super.modifyDeserializer(config, beanDesc, deserializer);
        }
    }

    private void setPropertyValue(JsonParser p, SettableBeanProperty beanProperty, JsonNode node, IProperty property, T value) {
        try {
            if (property instanceof IModelProperty) {
                IModelProperty modelProperty = (IModelProperty) property;
                if (beanProperty.getType().isCollectionLikeType()) {
                    JavaType genericType = beanProperty.getType().getBindings().getBoundType(0);
                    Collection dest = (Collection) beanWrapper.getPropertyDescriptor(modelProperty.getName()).getReadMethod().invoke(value);
                    if (node == null || node.size() == 0) {
                        if (dest != null) {
                            dest.clear();
                        }
                    } else {
                        if (!node.isArray()) {
                            throw new InvalidFormatException(p, "content must be array for field " + modelProperty, value, _valueClass);
                        }
                        List<JsonNode> src = IteratorUtils.toList(node.iterator());
                        if (dest == null) {
                            dest = BindUtils.createCollection(beanProperty.getType().getRawClass());
                            beanProperty.set(value, dest);
                        }
                        if (beanProperty.getAnnotation(OneToMany.class) != null) {
                            //remove not sent
                            for (Object modelObject : new ArrayList<>(dest)) {
                                if (src.stream().noneMatch(x -> matchId(x, modelObject))) {
                                    dest.remove(modelObject);
                                }
                            }
                            //edit existing
                            for (JsonNode collectionNode : src) {
                                Optional model = dest.stream()
                                        .filter(x -> matchId(collectionNode, x))
                                        .findAny();
                                final Collection finalDest = dest;
                                BindUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                    if (model.isPresent()) {
                                        objectMapper.readerForUpdating(model.get()).readValue(collectionNode);
                                    } else {
                                        //instantiate and add new
                                        finalDest.add(objectMapper.readerForUpdating(BindUtils.createModel(genericType.getRawClass())).readValue(collectionNode));
                                    }
                                });

                            }
                        } else if (beanProperty.getAnnotation(ManyToMany.class) != null) {
                            dest.clear();
                            for (JsonNode jsonNode : node) {
                                dest.add(em.getReference(genericType.getRawClass(), getId(jsonNode)));
                            }
                        } else {
                            throw new IllegalStateException("collection field must be OneToMany or ManyToMany");
                        }
                    }
                } else if (beanProperty.getType().isMapLikeType()) {
                    JavaType genericType = beanProperty.getType().getBindings().getBoundType(1);
                    Map dest = (Map) beanWrapper.getPropertyDescriptor(modelProperty.getName()).getReadMethod().invoke(value);
                    if (node == null || node.size() == 0) {
                        if (dest != null) {
                            dest.clear();
                        }
                    } else {
                        if (dest == null) {
                            dest = BindUtils.createMap(beanProperty.getType().getRawClass());
                            beanProperty.set(value, dest);
                        }
                        if (beanProperty.getAnnotation(OneToMany.class) != null) {
                            //remove not sent
                            for (Object keyObj : new HashMap<>(dest).keySet()) {
                                String key = objectMapper.convertValue(keyObj, String.class);
                                if (node.get(key) == null) {
                                    dest.remove(key);
                                }
                            }
                            //edit existing
                            if (node.isArray()) {
                                for (JsonNode jsonNode : node) {
                                    String key = jsonNode.get("key").asText();
                                    Object model = dest.get(key);
                                    final Map finalDest = dest;
                                    BindUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                        if (model != null) {
                                            objectMapper.readerForUpdating(model).readValue(jsonNode.get("value"));
                                        } else {
                                            //instantiate and add new
                                            finalDest.put(key, objectMapper.readerForUpdating(BindUtils.createModel(genericType.getRawClass())).readValue(jsonNode.get("value")));
                                        }
                                    });
                                }
                            } else {
                                Iterator<String> iterator = node.fieldNames();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    Object model = dest.get(key);
                                    final Map finalDest = dest;
                                    BindUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                        if (model != null) {
                                            objectMapper.readerForUpdating(model).readValue(node.get(key));
                                        } else {
                                            //instantiate and add new
                                            finalDest.put(key, objectMapper.readerForUpdating(BindUtils.createModel(genericType.getRawClass())).readValue(node.get(key)));
                                        }
                                    });

                                }
                            }
                        } else {
                            throw new IllegalStateException("map field must be OneToMany");
                        }
                    }
                } else if (beanProperty.getAnnotation(ManyToOne.class) != null) {
                    Object id = getId(node);
                    Object fieldValue;
                    if (id == null) {
                        fieldValue = null;
                    } else {
                        fieldValue = em.getReference(beanProperty.getType().getRawClass(), id);
                    }
                    beanProperty.set(value, fieldValue);
                } else if (beanProperty.getAnnotation(OneToOne.class) != null) {
                    OneToOne oneToOne = beanProperty.getAnnotation(OneToOne.class);
                    if (!StringUtils.isEmpty(oneToOne.mappedBy())) {
                        //bind reference
                        Object id = getId(node);
                        Object fieldValue;
                        if (id == null) {
                            fieldValue = null;
                        } else {
                            fieldValue = em.getReference(beanProperty.getType().getRawClass(), id);
                        }
                        beanProperty.set(value, fieldValue);
                    } else {
                        bindRecursive(value, node, beanProperty, modelProperty);
                    }
                } else {
                    bindRecursive(value, node, beanProperty, modelProperty);
                }
            } else {
                if (node != null) {
                    if (node.isArray() && beanProperty.getType().isMapLikeType()) {
                        // beanProperty.getType()
                        Map result = new LinkedHashMap();
                        for (JsonNode jsonNode : node) {
                            JsonParser nodeParser = new TreeTraversingParser(jsonNode, p.getCodec());
                            JavaType type = objectMapper.getTypeFactory().constructParametricType(SpringFormMapDeserializer.MapItem.class, beanProperty.getType().getKeyType(), beanProperty.getType().getContentType());
                            SpringFormMapDeserializer.MapItem item = objectMapper.readValue(nodeParser, type);
                            result.put(item.getKey(), item.getValue());
                        }
                        beanProperty.set(value, result);
                    } else {
                        beanProperty.set(value, objectMapper.readerFor(beanProperty.getType()).readValue(node));
                    }
                } else {
                    beanProperty.set(value, null);
                }
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("can not access field " + property, e);
        } catch (Throwable t) {
            log.error("Deserialization Error", t);
        }
    }

    private <ID> ID getIdByType(Class<ID> idClass, String id) {
        return (ID) OgnlOps.convertValue(id, idClass);
    }
}
