package com.github.madz0.springbinder.binding;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.Groups;
import com.github.madz0.springbinder.model.Model;
import javassist.util.proxy.ProxyFactory;
import lombok.SneakyThrows;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.persistence.EntityGraph;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.springframework.util.comparator.Comparators;

public class BindingUtils {

    private static final Map<Class<?>, BeanWrapperImpl> beanWrapperMap = new HashMap<>();
    private static final Set<IProperty> emptyCollection = Collections.emptySet();
    private static Map<Class<? extends Groups.IGroup>, Set<IProperty>> groupMap = new HashMap<>();

    public interface RunnableWithException {
        void run() throws Throwable;
    }

    public static void pushPopProperties(
        DeserializationContext context,
        Set<IProperty> properties,
        RunnableWithException runnable) throws Throwable {
        Deque<Set<IProperty>> props = getPropsDeque(context);
        props.push(properties);
        try {
            runnable.run();
        } finally {
            props.pop();
        }
    }

    public static void pushPopProperties(
        SerializerProvider provider,
        Set<IProperty> properties,
        RunnableWithException runnable) throws Throwable {
        Deque<Set<IProperty>> props = getPropsDeque(provider);
        props.push(properties);
        try {
            runnable.run();
        } finally {
            props.pop();
        }
    }

    protected static Deque<Set<IProperty>> getPropsDeque(DeserializationContext context) {
        return (Deque<Set<IProperty>>)context.getAttribute("props");
    }

    protected static Deque<Set<IProperty>> getPropsDeque(SerializerProvider provider) {
        return (Deque<Set<IProperty>>)provider.getAttribute("props");
    }

    public static Set<IProperty> peekProperties(DeserializationContext context) {
        Deque<Set<IProperty>> props = getPropsDeque(context);
        if (props.size() > 0) {
            return props.peek();
        }
        return emptyCollection;
    }

    public static Set<IProperty> peekProperties(SerializerProvider provider) {
        Deque<Set<IProperty>> props = getPropsDeque(provider);
        if (props.size() > 0) {
            return props.peek();
        }
        return emptyCollection;
    }

    private BindingUtils() {
    }

    @SneakyThrows
    public static <T> Collection<T> createCollection(Class<T> collectionType) {
        if (!collectionType.isInterface()) {
            return (Collection<T>) collectionType.getConstructor().newInstance();
        } else if (List.class.equals(collectionType)) {
            return new ArrayList<>();
        } else if (SortedSet.class.equals(collectionType)) {
            return new TreeSet<>(Comparators.comparable());
        } else {
            return new LinkedHashSet<>();
        }
    }

    @SneakyThrows
    public static Map createMap(Class collectionType) {
        if (!collectionType.isInterface()) {
            return (Map) collectionType.getConstructor().newInstance();
        } else if (SortedMap.class.equals(collectionType)) {
            return new TreeMap();
        } else {
            return new HashMap();
        }
    }

    public static Object createModel(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static synchronized Set<IProperty> getPropertiesFromGroup(Class<? extends Groups.IGroup> group) {
        if (!groupMap.containsKey(group)) {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setInterfaces(new Class[]{group});
            Groups.IGroup target = (Groups.IGroup) proxyFactory.create(null, null);
            groupMap.put(group, target.getProperties());
        }
        return groupMap.get(group);
    }

    public static Set<IProperty> getPropertiesOfCurrentGroup(DeserializationContext context) {
        return getPropertiesFromGroup(getGroup(context));
    }

    public static Set<IProperty> getPropertiesOfCurrentGroup(SerializerProvider provider) {
        return getPropertiesFromGroup(getGroup(provider));
    }

    public static BeanWrapperImpl getBeanWrapper(Class<?> clazz) {
        if (!beanWrapperMap.containsKey(clazz)) {
            synchronized (beanWrapperMap) {
                if (!beanWrapperMap.containsKey(clazz)) {
                    beanWrapperMap.put(clazz, new BeanWrapperImpl(clazz));
                }
            }
        }
        return beanWrapperMap.get(clazz);
    }

    public static void initialize(Object model, Class<? extends Groups.IGroup> group) {
        if (group != null) {
            Set<IProperty> properties = getPropertiesFromGroup(group);
            initialize(model, properties);
        }
    }

    @SneakyThrows
    private static void initialize(Object model, Set<IProperty> properties) {
        if (model instanceof Model) {
            BeanWrapper beanWrapper = getBeanWrapper(model.getClass());
            for (IProperty property : properties) {
                Object nested = beanWrapper.getPropertyDescriptor(property.getName()).getReadMethod().invoke(model);
                Hibernate.initialize(nested);
                if (property instanceof IModelProperty) {
                    initialize(nested, ((IModelProperty) property).getFields());
                }
            }
        }
    }

    public static <Y> Path<Y> findPath(Path<Y> path, String field, Boolean isFetch) {
        String[] split = field.split("\\.");
        Path<Y> result = path;
        int i = 0;
        for (; i < split.length - 1; i++) {
            if (!isFetch) {
                result = (result instanceof Join ? (Join) result : (Root) result).join(split[i]);
            } else {
                result = (Path<Y>) (result instanceof Fetch ? (Fetch) result : (Root) result).fetch(split[i]);
            }
        }
        return result.get(split[i]);
    }

    public static <T, Y> Path<Y> findPath(Root<T> root, String field) {
        return findPath((Path<Y>) root, field, false);
    }

    public static <T, Y> Path<Y> findPathFetch(Root<T> root, String field) {
        return findPath((Path<Y>) root, field, true);
    }

    public static <Y> Path<Y> findPathFetch(Path<Y> path, String field) {
        return findPath(path, field, true);
    }

    public static void setGroup(DeserializationContext context, Class<? extends Groups.IGroup> group) {
        context.setAttribute("group", group);
    }

    public static Class<? extends Groups.IGroup> getGroup(DeserializationContext context) {
        return (Class<? extends Groups.IGroup>) context.getAttribute("group");
    }

    public static void setModifying(DeserializationContext context, boolean isModifying) {
        context.setAttribute("modifying", isModifying);
    }

    public static Boolean isModifying(DeserializationContext context) {
        return (Boolean) context.getAttribute("modifying");
    }

    public static void setEntityGraph(DeserializationContext context, EntityGraph<?> entityGraph) {
        context.setAttribute("entityGraph", entityGraph);
    }

    public static EntityGraph<?> getEntityGraph(DeserializationContext context) {
        return (EntityGraph<?>) context.getAttribute("entityGraph");
    }

    public static void setDtoBinding(DeserializationContext context, boolean isModifying) {
        context.setAttribute("dtoBinding", isModifying);
    }

    public static Boolean getDtoBinding(DeserializationContext context) {
        return (Boolean) context.getAttribute("dtoBinding");
    }

    public static void setGroup(SerializerProvider provider, Class<? extends Groups.IGroup> group) {
        provider.setAttribute("group", group);
    }

    public static Class<? extends Groups.IGroup> getGroup(SerializerProvider provider) {
        return (Class<? extends Groups.IGroup>) provider.getAttribute("group");
    }
}
