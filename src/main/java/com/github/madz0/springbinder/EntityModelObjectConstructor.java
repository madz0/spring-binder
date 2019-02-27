package com.github.madz0.springbinder;

import ir.iiscenter.springform.model.IBaseModel;
import lombok.extern.slf4j.Slf4j;
import ognl.*;
import ognl.extended.DefaultMemberAccess;
import ognl.extended.DefaultObjectConstructor;
import ognl.extended.MapNode;
import ognl.extended.OgnlPropertyDescriptor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class EntityModelObjectConstructor<ID> extends DefaultObjectConstructor {

    EntityManager entityManager;
    Class<ID> idClass;
    EntityGraph<?> graph;

    public EntityModelObjectConstructor(EntityManager entityManager, Class<ID> idClass, EntityGraph<?> graph) {
        this.entityManager = entityManager;
        this.idClass = idClass;
        this.graph = graph;
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
    public Object processObject(OgnlContext context, Object root, OgnlPropertyDescriptor propertyDescriptor,
                                Object propertyObject, MapNode node) {
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
                                context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                                context.extend();
                                context.addObjectConstructor(this);
                                Object obj = null;
                                if (model.isPresent()) {
                                    obj = model;
                                } else {
                                    try {
                                        obj = OgnlRuntime.createProperObject(context, genericClazz, genericClazz.getComponentType(), node);
                                        dest.add(obj);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                }
                                try {
                                    Ognl.getValue(collectionNode, context, obj);
                                } catch (OgnlException e) {
                                    log.error("", e);
                                }
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
                                context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                                context.extend();
                                context.addObjectConstructor(this);
                                if (model == null) {
                                    try {
                                        obj = OgnlRuntime.createProperObject(context, genericClazz, genericClazz.getComponentType(), node);
                                        dest.put(key, obj);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                }
                                try {
                                    Ognl.getValue(collectionNode, context, obj);
                                } catch (OgnlException e) {
                                    log.error("", e);
                                }
                            }
                        } else {
                            throw new IllegalStateException("Map field must be OneToMany");
                        }
                    }
                }
            } else if (propertyDescriptor.getAnnotation(ManyToOne.class) != null) {
                Object id = getId(node);
                Object fieldValue;
                if (id == null) {
                    fieldValue = null;
                } else {
                    fieldValue = entityManager.getReference(propertyObject.getClass(), id);
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
                    context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                    context.extend();
                    context.addObjectConstructor(this);
                    try {
                        Ognl.getValue(node, context, propertyObject);
                    } catch (OgnlException e) {
                        log.error("", e);
                    }
                }
            } else {
                context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                context.extend();
                context.addObjectConstructor(this);
                try {
                    Ognl.getValue(node, context, propertyObject);
                } catch (OgnlException e) {
                    log.error("", e);
                }
            }
            return propertyObject;
        }

        return super.processObject(context, root, propertyDescriptor, propertyObject, node);
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
}
