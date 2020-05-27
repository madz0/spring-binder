package com.github.madz0.springbinder.binding.form;

import com.github.madz0.ognl2.*;
import com.github.madz0.ognl2.extended.*;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.property.FieldProperty;
import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.Groups;
import com.github.madz0.springbinder.model.IdModel;
import com.github.madz0.springbinder.model.IdModelFields;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

@Slf4j
public class EntityModelObjectConstructor extends DefaultObjectConstructor {

    private EntityManager entityManager;
    private EntityGraph<?> graph;
    private IdClassMapper idClassMapper;
    private Stack<Set<IProperty>> groupStack = new Stack<>();
    private Boolean dtoBinding;

    public EntityModelObjectConstructor(EntityManager entityManager,
                                        EntityGraph<?> graph,
                                        Class<? extends Groups.IGroup> group,
                                        IdClassMapper idClassMapper,
                                        Boolean dtoBinding) {
        this.entityManager = entityManager;
        this.graph = graph;
        if (group != Groups.IGroup.class) {
            groupStack.push(BindingUtils.getPropertiesFromGroup(group));
        }
        this.idClassMapper = idClassMapper;
        this.dtoBinding = dtoBinding;
    }

    @Override
    public Object createObject(Class<?> cls, Class<?> componentType, MapNode node) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (!dtoBinding && IdModel.class.isAssignableFrom(cls) && node != null && node.getIsRoot()) {
            Object id = getId(node, getIdClass(cls));
            if (id != null) {
                if (graph != null) {
                    return entityManager.find(cls, id, Collections.singletonMap("javax.persistence.loadgraph", graph));
                } else {
                    return entityManager.find(cls, id);
                }
            }
        }
        return super.createObject(cls, componentType, node);
    }

    @Override
    public Object processObjectForGet(OgnlContext context, Object root, OgnlPropertyDescriptor propertyDescriptor,
                                      Object propertyObject, MapNode node) {
        IProperty currentProp = null;
        if (((root instanceof IdModel && !dtoBinding) || !(root instanceof Collection)) && !groupStack.isEmpty()) {
            currentProp = getIProperty(groupStack.peek(), propertyDescriptor.getPropertyName());
            if (currentProp == null) {
                return propertyObject;
            }
        }

        if (!dtoBinding && propertyDescriptor != null && root instanceof IdModel) {
            if (node.isCollection()) {
                Type genericType = propertyDescriptor.getReadMethod().getGenericReturnType();
                ParameterizedType parameterizedType = (ParameterizedType) genericType; //If not let it throws exception
                if (propertyObject instanceof List || propertyObject instanceof Set) {
                    Class<?> genericClazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Collection dest = (Collection) propertyObject;
                    if (node.getChildren().size() == 0 || (node.getContainsEmptyChildValue() && node.getChildren().size() == 1)) {
                        dest.clear();
                    } else {
                        Collection<MapNode> src = node.getChildren().values();
                        if (propertyDescriptor.getAnnotation(OneToMany.class) != null) {
                            dest.removeIf(modelObject -> src.stream().noneMatch(mapNode -> matchId(mapNode, modelObject)));
                            for (MapNode collectionNode : node.getChildren().values()) {
                                Optional model = dest.stream()
                                        .filter(x -> matchId(collectionNode, x))
                                        .findAny();
                                final OgnlContext contextFinal = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                                contextFinal.extend();
                                contextFinal.setObjectConstructor(this);
                                Object obj = null;
                                if (model.isPresent()) {
                                    obj = model.get();
                                } else if (!collectionNode.getContainsValue() || collectionNode.getValue() != null) {
                                    try {
                                        Object finalObj = obj = OgnlRuntime.createProperObject(contextFinal, genericClazz, genericClazz.getComponentType(), node);
                                        pushPopPropFields(Collections.singleton(FieldProperty.of(root.getClass().getSimpleName().toLowerCase())), () -> {
                                            try {
                                                OgnlRuntime.setProperty(contextFinal, finalObj, root.getClass().getSimpleName().toLowerCase(), root);
                                            } catch (Exception e) {
                                                log.error("", e);
                                            }
                                            return null;
                                        });
                                        dest.add(obj);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                }

                                if (!collectionNode.getContainsValue() || collectionNode.getValue() != null) {
                                    doFieldBinding(currentProp, collectionNode, contextFinal, obj);
                                }
                            }
                        } else if (propertyDescriptor.getAnnotation(ManyToMany.class) != null) {
                            dest.clear();
                            for (MapNode collectionNode : node.getChildren().values()) {
                                dest.add(entityManager.getReference(genericClazz, collectionNode.getMapping(IdModelFields.ID)));
                            }
                        } else {
                            throw new IllegalStateException("collection field must be OneToMany or ManyToMany");
                        }
                    }
                } else if (propertyObject instanceof Map) {
                    Class<?> genericClazz = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    Map dest = (Map) propertyObject;
                    if (node.getChildren().size() == 0) {
                        dest.clear();
                    } else {
                        Collection<MapNode> src = node.getChildren().values();
                        if (propertyDescriptor.getAnnotation(OneToMany.class) != null) {
                            for (Iterator<Map.Entry> it = dest.entrySet().iterator(); it.hasNext(); ) {
                                String key = it.next().getKey().toString();
                                if (node.getMapping('[' + key + ']') == null) {
                                    it.remove();
                                }
                            }

                            for (MapNode collectionNode : node.getChildren().values()) {
                                String key = collectionNode.getName().substring(1, collectionNode.getName().length() - 1);
                                Object model = dest.get(key);
                                Object obj = model;
                                OgnlContext contextFinal = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                                contextFinal.extend();
                                contextFinal.setObjectConstructor(this);
                                if (model == null) {
                                    try {
                                        obj = OgnlRuntime.createProperObject(contextFinal, genericClazz, genericClazz.getComponentType(), node);
                                        OgnlRuntime.setProperty(contextFinal, obj, root.getClass().getSimpleName().toLowerCase(), root);
                                        dest.put(key, obj);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                }
                                doFieldBinding(currentProp, collectionNode, contextFinal, obj);
                            }
                        } else {
                            throw new IllegalStateException("Map field must be OneToMany");
                        }
                    }
                }
            } else if (propertyDescriptor.getAnnotation(ManyToOne.class) != null) {
                if (propertyObject instanceof HibernateProxy) {
                    return propertyObject;
                }

                Object id = getId(node, getIdClass(propertyObject.getClass()));
                Object fieldValue = propertyObject;
                if (id == null) {
                    fieldValue = null;
                } else {
                    try {
                        fieldValue = entityManager.getReference(propertyObject.getClass(), id);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
                return fieldValue;
            } else if (propertyDescriptor.getAnnotation(OneToOne.class) != null) {
                OneToOne oneToOne = propertyDescriptor.getAnnotation(OneToOne.class);
                if (!StringUtils.isEmpty(oneToOne.mappedBy())) {
                    //bind reference
                    Object id = null;
                    try {
                        id = getId(node, getIdClass(propertyObject.getClass()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Object fieldValue;
                    if (id == null) {
                        fieldValue = null;
                    } else {
                        fieldValue = entityManager.getReference(propertyObject.getClass(), id);
                    }
                    return fieldValue;
                } else {
                    OgnlContext contextFinal = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                    contextFinal.extend();
                    contextFinal.setObjectConstructor(this);
                    doFieldBinding(currentProp, node, contextFinal, propertyObject);
                }
            } else {
                OgnlContext contextFinal = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                contextFinal.extend();
                contextFinal.setObjectConstructor(this);
                final Object finalObj = propertyObject;
                pushPopPropFields(currentProp, () -> {
                    try {
                        Ognl.getValue(node, contextFinal, finalObj);
                        return null;
                    } catch (OgnlException e) {
                        log.error("", e);
                        return null;
                    }
                });
            }
            return propertyObject;
        }
        return pushPopPropFields(currentProp, () -> super.processObjectForGet(context, root, propertyDescriptor, propertyObject, node));
    }

    protected void doFieldBinding(IProperty currentProp, MapNode collectionNode, OgnlContext contextFinal, Object obj) {
        final Object finalObj = obj;
        pushPopPropFields(currentProp, () -> {
            try {
                Ognl.getValue(collectionNode, contextFinal, finalObj);
            } catch (OgnlException e) {
                log.error("", e);
            }
            return null;
        });
    }

    @Override
    public Object processObjectForSet(OgnlContext context, Object root, OgnlPropertyDescriptor propertyDescriptor, Object propertyObject, MapNode node) throws PropertySetIgnoreException {
        if (!groupStack.isEmpty()) {
            IProperty currentProp = getIProperty(groupStack.peek(), propertyDescriptor.getPropertyName());
            if (currentProp == null) {
                throw new PropertySetIgnoreException();
            }
        }

        return super.processObjectForSet(context, root, propertyDescriptor, propertyObject, node);
    }

    private boolean matchId(MapNode node, Object model) {
        if (model instanceof IdModel) {
            IdModel baseModel = (IdModel) model;
            return baseModel.getId() != null && Objects.equals(getId(node, baseModel.getId().getClass()), baseModel.getId());
        }
        return false;
    }

    private Object getId(MapNode node, Class idClass) {
        if (node != null) {
            MapNode idNode = node.getMapping(IdModelFields.ID);
            if (idNode != null) {
                if (idNode.getContainsValue()) {
                    return OgnlOps.convertValue(idNode.getValue(), idClass);
                } else if (idNode.getChildren().size() > 0) {
                    try {
                        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                        context.extend();
                        //We don't need to set objectConstructor to this becase embedded ids are simple serializable
                        return Ognl.getValue(idNode, context, OgnlRuntime.createProperObject(idClass, idClass));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private IProperty getIProperty(Set<IProperty> iProperties, String propName) {
        if (iProperties != null) {
            for (IProperty property : iProperties) {
                if (property.getName().equals(propName)) {
                    return property;
                }
            }
        }
        return null;
    }

    private <T> T pushPopPropFields(IProperty prop, Callable<T> r) {
        if (prop != null) {
            return pushPopPropFields(prop instanceof IModelProperty ?
                    ((IModelProperty) prop).getFields() : Collections.singleton(prop), r);
        } else {
            try {
                return r.call();
            } catch (Exception e) {
                return null;
            }
        }
    }

    private <T> T pushPopPropFields(Set<IProperty> propertySet, Callable<T> r) {
        try {
            groupStack.push(propertySet);
            return r.call();
        } catch (Exception e) {
            return null;
        } finally {
            groupStack.pop();
        }
    }

    private Class<?> getIdClass(Class<?> cls) {
        try {
            return idClassMapper != null ? idClassMapper.getIdClassOf(cls) :
                    OgnlRuntime.getPropertyDescriptor(cls, IdModelFields.ID).getReadMethod().getReturnType();
        } catch (Exception e) {
            log.error("",e);
            return null;
        }
    }
}
