package com.github.madz0.springbinder.binding;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.function.Function;

public class MethodUtils {
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static<T> String getMethodName(Class<T> clazz, Function<T, ?> mapFunc){
        final StringHolder sh = new StringHolder();
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        factory.setFilter(m -> !Objects.equals(m.getName(), "toString"));
        Class proxyClass = factory.createClass();
        MethodHandler mi = (self, thisMethod, proceed, args) -> {
            sh.value = thisMethod.getName();
            return null;
        };
        T target = (T)proxyClass.newInstance();
        ((Proxy)target).setHandler(mi);
        try{
            mapFunc.apply(target);
        }catch (NullPointerException e){
            if(sh.value == null){
                throw e;
            }
        }
        return sh.value;
    }

    private static class StringHolder{
        String value=null;
    }
}
