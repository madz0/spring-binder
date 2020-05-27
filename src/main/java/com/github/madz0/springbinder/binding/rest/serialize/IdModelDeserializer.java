package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.github.madz0.ognl2.OgnlOps;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.IdModel;
import com.github.madz0.springbinder.model.IdModelFields;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.StringUtils;

@Slf4j
public class IdModelDeserializer<T extends IdModel> extends StdDeserializer<T> implements ResolvableDeserializer {

    private final ObjectMapper objectMapper;
    private final JsonDeserializer deserializer;
    private final BeanDeserializer beanDeserializer;
    private final BeanWrapperImpl beanWrapper;
    private final EntityManager em;
    private IdClassMapper idClassMapper = null;

    IdModelDeserializer(Class<?> vc, ObjectMapper objectMapper, JsonDeserializer deserializer) {
        super(vc);
        this.objectMapper = objectMapper;
        this.deserializer = deserializer;
        if (deserializer instanceof BeanDeserializer) {
            this.beanDeserializer = (BeanDeserializer) deserializer;
            this.beanWrapper = BindingUtils.getBeanWrapper(vc);
        } else {
            this.beanDeserializer = null;
            this.beanWrapper = null;
        }
        this.em = ContextAwareObjectMapper.getBean(EntityManager.class);
        try {
            this.idClassMapper = ContextAwareObjectMapper.getBean(IdClassMapper.class);
        } catch (Exception e) {
            log.warn("Could not find bean of type {}. Please create one if you appreciate a little more speed!",
                IdClassMapper.class);
        }
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext context) {

        if (BindingUtils.getGroup(context) == null || BindingUtils.dtoBinding.get()) {
            return (T) deserializer.deserialize(p, context);
        }
        JsonNode root = p.getCodec().readTree(p);
        T value;
        if (BindingUtils.updating.get()) {
            try {
                Object id = getId(root, getIdClass(_valueClass));
                EntityGraph<?> graph = BindingUtils.entityGraph.get();
                if (graph != null) {
                    value = (T) em
                        .find(_valueClass, id, Collections.singletonMap("javax.persistence.loadgraph", graph));
                } else {
                    value = (T) em.find(_valueClass, id);
                }
            } catch (Exception e) {
                throw new InvalidDataAccessApiUsageException(IdModelFields.ID);
            }
            if (value == null) {
                throw new InvalidDataAccessApiUsageException(IdModelFields.ID);
            }
        } else {
            if (beanDeserializer != null) {
                value = (T) beanDeserializer.getValueInstantiator().createUsingDefault(context);
            } else {
                value = (T) BindingUtils.createModel(_valueClass);
            }
        }
        return bind(p, root, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt, T value) throws IOException {
        if (BindingUtils.group.get() == null) {
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
        Set<IProperty> properties = BindingUtils.peekProperties();
        if (properties == null) {
            properties = BindingUtils.getPropertiesOfCurrentGroup();
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
                model = BindingUtils.createModel(beanProperty.getType().getRawClass());
            }
            final Object finalModel = model;
            try {
                BindingUtils.pushPopProperties(modelProperty.getFields(),
                    () -> objectMapper.readerForUpdating(finalModel).readValue(node)
                );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            beanProperty.set(value, model);
        }
    }

    private boolean matchId(JsonNode node, Object model) {
        if (model instanceof IdModel) {
            IdModel baseModel = (IdModel) model;
            return baseModel.getId() != null && Objects
                .equals(getId(node, baseModel.getId().getClass()), baseModel.getId());
        }
        return false;
    }

    private Object getId(JsonNode node, Class idClass) {
        if (node != null) {
            JsonNode idNode = node.get(IdModelFields.ID);
            if (idNode != null) {
                if (idNode.isTextual()) {
                    return getIdByType(idClass, idNode.textValue());
                } else if (idNode.isNumber()) {
                    return getIdByType(idClass, String.valueOf(idNode.numberValue()));
                } else if (idNode.isObject()) {
                    try {
                        return objectMapper.readerForUpdating(BindingUtils.createModel(idClass)).readValue(idNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

    private void setPropertyValue(JsonParser p, SettableBeanProperty beanProperty, JsonNode node, IProperty property,
        T value) {
        try {
            if (property instanceof IModelProperty) {
                IModelProperty modelProperty = (IModelProperty) property;
                if (beanProperty.getType().isCollectionLikeType()) {
                    JavaType genericType = beanProperty.getType().getBindings().getBoundType(0);
                    Collection dest = (Collection) beanWrapper.getPropertyDescriptor(modelProperty.getName())
                        .getReadMethod().invoke(value);
                    if (node == null || node.size() == 0) {
                        if (dest != null) {
                            dest.clear();
                        }
                    } else {
                        if (!node.isArray()) {
                            throw new InvalidFormatException(p, "content must be array for field " + modelProperty,
                                value, _valueClass);
                        }
                        List<JsonNode> src = IteratorUtils.toList(node.iterator());
                        if (dest == null) {
                            dest = BindingUtils.createCollection(beanProperty.getType().getRawClass());
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
                                BindingUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                    Object modelObj = null;
                                    if (model.isPresent()) {
                                        modelObj = model.get();
                                        objectMapper.readerForUpdating(modelObj).readValue(collectionNode);
                                    } else {
                                        //instantiate and add new
                                        modelObj = BindingUtils.createModel(genericType.getRawClass());
                                        finalDest
                                            .add(objectMapper.readerForUpdating(modelObj).readValue(collectionNode));
                                    }
                                    BindingUtils.getBeanWrapper(modelObj.getClass()).
                                        getPropertyDescriptor(_valueClass.getSimpleName())
                                        .getWriteMethod().invoke(modelObj, value);
                                });
                            }
                        } else if (beanProperty.getAnnotation(ManyToMany.class) != null) {
                            dest.clear();
                            for (JsonNode jsonNode : node) {
                                dest.add(em.getReference(genericType.getRawClass(),
                                    getId(jsonNode, getIdClass(genericType.getRawClass()))));
                            }
                        } else {
                            throw new IllegalStateException("collection field must be OneToMany or ManyToMany");
                        }
                    }
                } else if (beanProperty.getType().isMapLikeType()) {
                    JavaType genericType = beanProperty.getType().getBindings().getBoundType(1);
                    Map dest = (Map) beanWrapper.getPropertyDescriptor(modelProperty.getName()).getReadMethod()
                        .invoke(value);
                    if (node == null || node.size() == 0) {
                        if (dest != null) {
                            dest.clear();
                        }
                    } else {
                        if (dest == null) {
                            dest = BindingUtils.createMap(beanProperty.getType().getRawClass());
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
                                    BindingUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                        if (model != null) {
                                            objectMapper.readerForUpdating(model).readValue(jsonNode.get("value"));
                                        } else {
                                            //instantiate and add new
                                            finalDest.put(key, objectMapper
                                                .readerForUpdating(BindingUtils.createModel(genericType.getRawClass()))
                                                .readValue(jsonNode.get("value")));
                                        }
                                    });
                                }
                            } else {
                                Iterator<String> iterator = node.fieldNames();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    Object model = dest.get(key);
                                    final Map finalDest = dest;
                                    BindingUtils.pushPopProperties(modelProperty.getFields(), () -> {
                                        if (model != null) {
                                            objectMapper.readerForUpdating(model).readValue(node.get(key));
                                        } else {
                                            //instantiate and add new
                                            finalDest.put(key, objectMapper
                                                .readerForUpdating(BindingUtils.createModel(genericType.getRawClass()))
                                                .readValue(node.get(key)));
                                        }
                                    });

                                }
                            }
                        } else {
                            throw new IllegalStateException("map field must be OneToMany");
                        }
                    }
                } else if (beanProperty.getAnnotation(ManyToOne.class) != null) {
                    getReferenceForNode(beanProperty, node, value);
                } else if (beanProperty.getAnnotation(OneToOne.class) != null) {
                    OneToOne oneToOne = beanProperty.getAnnotation(OneToOne.class);
                    if (!StringUtils.isEmpty(oneToOne.mappedBy())) {
                        //bind reference
                        getReferenceForNode(beanProperty, node, value);
                    } else {
                        bindRecursive(value, node, beanProperty, modelProperty);
                    }
                } else {
                    bindRecursive(value, node, beanProperty, modelProperty);
                }
            } else {
                if (node != null) {
                    if (node.isArray() && beanProperty.getType().isMapLikeType()) {
                        Map result = new LinkedHashMap();
                        for (JsonNode jsonNode : node) {
                            JsonParser nodeParser = new TreeTraversingParser(jsonNode, p.getCodec());
                            JavaType type = objectMapper.getTypeFactory()
                                .constructParametricType(Entry.class,
                                    beanProperty.getType().getKeyType(), beanProperty.getType().getContentType());
                            Entry item = objectMapper.readValue(nodeParser, type);
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

    private void getReferenceForNode(SettableBeanProperty beanProperty, JsonNode node, T value) throws IOException {
        Class<?> entityCls = beanProperty.getType().getRawClass();
        Object id = getId(node, getIdClass(entityCls));
        Object fieldValue;
        if (id == null) {
            fieldValue = null;
        } else {
            fieldValue = em.getReference(entityCls, id);
        }
        beanProperty.set(value, fieldValue);
    }

    private <T> T getIdByType(Class<T> idClass, String id) {
        return (T) OgnlOps.convertValue(id, idClass);
    }

    private Class<?> getIdClass(Class<?> cls) {
        return idClassMapper != null ? idClassMapper.getIdClassOf(cls) :
            BindingUtils.getBeanWrapper(cls).getPropertyDescriptor(IdModelFields.ID)
                .getReadMethod().getReturnType();
    }
}
