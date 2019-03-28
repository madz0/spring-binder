package com.github.madz0.springbinder.binding.form;

import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.property.FieldProperty;
import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.model.IBaseModel;
import lombok.extern.slf4j.Slf4j;
import ognl.*;
import ognl.extended.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

@Slf4j
public class EntityModelObjectConstructor<ID> extends DefaultObjectConstructor {

    EntityManager entityManager;
    Class<ID> idClass;
    EntityGraph<?> graph;
    //private Set<IProperty> iGroupSet = null;
    private Stack<Set<IProperty>> groupStack = new Stack<>();

    public EntityModelObjectConstructor(EntityManager entityManager, Class<ID> idClass,
                                        EntityGraph<?> graph, Class<BaseGroups.IGroup> group) {
        this.entityManager = entityManager;
        this.idClass = idClass;
        this.graph = graph;
        if (group != BaseGroups.IGroup.class) {
            groupStack.push(BindUtils.getPropertiesFromGroup(group));
        }
    }

    @Override
    public Object createObject(Class<?> cls, Class<?> componentType, MapNode node) throws InstantiationException, IllegalAccessException {
        if (IBaseModel.class.isAssignableFrom(cls) && node != null && node.getIsRoot()) {
            ID id = getId(node);
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
        if (!groupStack.isEmpty()) {
            currentProp = getIProperty(groupStack.peek(), propertyDescriptor.getPropertyName());
            if (currentProp == null) {
                return propertyObject;
            }
        }

        if (propertyDescriptor != null && root instanceof IBaseModel && idClass != null) {

            if (node.isCollection()) {
                Type genericType = propertyDescriptor.getReadMethod().getGenericReturnType();
                ParameterizedType parameterizedType = (ParameterizedType) genericType; //If not let it throws exception
                if (propertyObject instanceof List || propertyObject instanceof Set) {
                    Class<?> genericClazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Collection dest = (Collection) propertyObject;
                    if (node.getChildren().size() == 0) {
                        dest.clear();
                    } else {
                        Collection<MapNode> src = node.getChildren().values();
                        if (propertyDescriptor.getAnnotation(OneToMany.class) != null) {
                            for (Iterator<?> it = dest.iterator(); it.hasNext(); ) {
                                Object modelObject = it.next();
                                if (src.stream().noneMatch(mapNode -> matchId(mapNode, modelObject))) {
                                    it.remove();
                                }
                            }
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
                                } else {
                                    try {
                                        Object finalObj = obj = OgnlRuntime.createProperObject(contextFinal, genericClazz, genericClazz.getComponentType(), node);
                                        pushPopPropFields(Collections.singleton(FieldProperty.of(root.getClass().getSimpleName().toLowerCase())), ()-> {
                                            try {
                                                OgnlRuntime.setProperty(contextFinal, finalObj, root.getClass().getSimpleName().toLowerCase(), root);
                                            } catch (Exception e) {
                                                log.error("", e);
                                            }
                                            finally {
                                                return null;
                                            }
                                        });
                                        dest.add(obj);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                }
                                final Object finalObj = obj;
                                pushPopPropFields((IModelProperty) currentProp, () -> {
                                    try {
                                        Ognl.getValue(collectionNode, contextFinal, finalObj);
                                    } catch (OgnlException e) {
                                        log.error("", e);
                                    }
                                    finally {
                                        return null;
                                    }
                                });
                            }
                        } else if (propertyDescriptor.getAnnotation(ManyToMany.class) != null) {
                            dest.clear();
                            for (MapNode collectionNode : node.getChildren().values()) {
                                dest.add(entityManager.getReference(genericClazz, collectionNode.getMapping(IBaseModel.ID_FIELD)));
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
                                final Object finalObj = obj;
                                pushPopPropFields((IModelProperty) currentProp, () -> {
                                    try {
                                        Ognl.getValue(collectionNode, contextFinal, finalObj);
                                    } catch (OgnlException e) {
                                        log.error("", e);
                                    }
                                    finally {
                                        return null;
                                    }
                                });

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

                Object id = getId(node);
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
                    Object id = getId(node);
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
                    final Object finalObj = propertyObject;
                    pushPopPropFields((IModelProperty) currentProp, () -> {
                        try {
                            Ognl.getValue(node, contextFinal, finalObj);
                        } catch (OgnlException e) {
                            log.error("", e);
                        }
                        finally {
                            return null;
                        }
                    });
                }
            } else {
                OgnlContext contextFinal = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                contextFinal.extend();
                contextFinal.setObjectConstructor(this);
                final Object finalObj = propertyObject;
                pushPopPropFields((IModelProperty) currentProp, () -> {
                    try {
                        Ognl.getValue(node, contextFinal, finalObj);
                    } catch (OgnlException e) {
                        log.error("", e);
                    }
                    finally {
                        return null;
                    }
                });
            }
            return propertyObject;
        }

        return pushPopPropFields(currentProp, ()->super.processObjectForGet(context, root, propertyDescriptor, propertyObject, node));
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
        if (model instanceof IBaseModel) {
            IBaseModel baseModel = (IBaseModel) model;
            return baseModel.getId() != null && Objects.equals(getId(node), baseModel.getId());
        }
        return false;
    }

    private ID getId(MapNode node) {
        if (node != null) {
            MapNode idNode = node.getMapping(IBaseModel.ID_FIELD);
            if (idNode != null) {
                return (ID) OgnlOps.convertValue(idNode.getValue(), idClass);
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
            return pushPopPropFields(prop instanceof IModelProperty?
                    ((IModelProperty)prop).getFields(): Collections.singleton(prop), r);
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
}