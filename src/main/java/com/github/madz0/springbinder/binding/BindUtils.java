package com.github.madz0.springbinder.binding;

import com.github.madz0.springbinder.binding.property.IModelProperty;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.model.IBaseModel;
import javassist.util.proxy.ProxyFactory;
import lombok.SneakyThrows;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

public class BindUtils {
    private static final Map<Class<?>, BeanWrapperImpl> beanWrapperMap = new HashMap<>();
    public static final ThreadLocal<Class<? extends BaseGroups.IGroup>> group = ThreadLocal.withInitial(() -> null);
    public static final ThreadLocal<Boolean> updating = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Stack<Set<IProperty>>> currentProperties = ThreadLocal.withInitial(Stack::new);
    public static final ThreadLocal<Class<?>> idClass = ThreadLocal.withInitial(() -> Void.class);

    public interface RunnableWithException {
        void run() throws Throwable;
    }

    public static void pushPopProperties(Set<IProperty> properties, RunnableWithException runnable) throws Throwable {
        currentProperties.get().push(properties);
        try {
            runnable.run();
        } finally {
            currentProperties.get().pop();
        }

    }

    public static Set<IProperty> peekProperties() {
        if (currentProperties.get().size() > 0) {
            return currentProperties.get().peek();
        }
        return null;

    }


    private BindUtils() {
    }

    @SneakyThrows
    public static Collection createCollection(Class collectionType) {
        if (!collectionType.isInterface()) {
            return (Collection) collectionType.newInstance();
        } else if (List.class.equals(collectionType)) {
            return new ArrayList();
        } else if (SortedSet.class.equals(collectionType)) {
            return new TreeSet();
        } else {
            return new LinkedHashSet();
        }
    }

    @SneakyThrows
    public static Map createMap(Class collectionType) {
        if (!collectionType.isInterface()) {
            return (Map) collectionType.newInstance();
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

    private static Map<Class<? extends BaseGroups.IGroup>, Set<IProperty>> groupMap = new HashMap<>();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static synchronized Set<IProperty> getPropertiesFromGroup(Class<? extends BaseGroups.IGroup> group) {
        if (!groupMap.containsKey(group)) {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setInterfaces(new Class[] {group});
            BaseGroups.IGroup target = (BaseGroups.IGroup) proxyFactory.create(null, null);
            groupMap.put(group, target.getProperties());
        }
        return groupMap.get(group);
    }

    public static Set<IProperty> getPropertiesOfCurrentGroup() {
        return getPropertiesFromGroup(group.get());
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

    public static void initialize(Object model, Class<? extends BaseGroups.IGroup> group) {
        if (group != null) {
            Set<IProperty> properties = getPropertiesFromGroup(group);
            initialize(model, properties);
        }
    }

    @SneakyThrows
    private static void initialize(Object model, Set<IProperty> properties) {
        if (model instanceof IBaseModel) {
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
}