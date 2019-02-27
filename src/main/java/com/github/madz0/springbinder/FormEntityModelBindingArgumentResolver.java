package com.github.madz0.springbinder;

import com.github.madz0.springbinder.annotation.FormObject;
import ir.iiscenter.springform.model.IBaseModel;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.extended.DefaultMemberAccess;
import ognl.extended.OgnlPropertyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Subgraph;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;

public class FormEntityModelBindingArgumentResolver implements HandlerMethodArgumentResolver {
    private Class<Serializable> idClazz;
    private EntityManager em;

    public FormEntityModelBindingArgumentResolver(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(FormObject.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        StringBuilder builder = new StringBuilder();
        Object value = null;
        try (BufferedReader br = request.getReader()) {
            char[] readBytes = new char[1024];
            int readSize;
            while ((readSize = br.read(readBytes)) != -1) {
                builder.append(new String(readBytes, 0, readSize));
            }
        }
        if (builder.length() > 0) {
            Type type = parameter.getParameterType();
            OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
            Class cls = null;
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                cls = (Class) ptype.getRawType();
                context.extend(ptype);
                if (idClazz == null && Collection.class.isAssignableFrom(cls)) {
                    Class genericType = (Class) ptype.getActualTypeArguments()[0];
                    checkIdClazz(genericType);
                }
            } else {
                cls = (Class) type;
                checkIdClazz(cls);
                context.extend();
            }
            context.addObjectConstructor(new EntityModelObjectConstructor<>(em, idClazz,
                    createEntityGraph(cls, parameter.getParameterAnnotation(FormObject.class).entityGraph())));
            builder.insert(0, '?');
            Map<String, List<String>> params = parsQuery(builder.toString());
            value = Ognl.getValue(params.get(parameter.getParameterName()), context, cls);
        }

        if (parameter.hasParameterAnnotation(Valid.class)) {
            binderFactory.createBinder(webRequest, value, parameter.getParameterName()).validate();
        }
        return value;
    }

    private Map<String, List<String>> parsQuery(String path)
            throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        if (path == null || !path.startsWith("?")) {
            return null;
        }
        String queryWithoutQuestionMark = path.replaceFirst("\\?", "&");
        final String[] pairs = queryWithoutQuestionMark.split("&");

        for (String pair : pairs) {
            if (pair == null || pair.equals("")) {
                continue;
            }

            final int idx = pair.indexOf("=");
            String key = null;
            String value = null;
            if (idx > 0 && pair.length() > idx + 1) {
                key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                final int bracketIdx = key.indexOf("[");
                final int dotIdx = key.indexOf(".");
                int finalIdx = 0;
                String keyParam = null;
                if (value == null) {
                    value = "";
                }

                if (dotIdx == bracketIdx) {
                    keyParam = key;
                    params.put(keyParam, new ArrayList<>());
                } else if (dotIdx != -1 && (bracketIdx == -1 || bracketIdx > dotIdx)) {
                    keyParam = key.substring(0, dotIdx);
                    value = key.substring(dotIdx + 1) + "=" + value;
                } else {
                    keyParam = key.substring(0, bracketIdx);
                    value = key.substring(bracketIdx) + "=" + value;
                }

                List<String> values = params.get(keyParam);
                if(values == null) {
                    values = new ArrayList<>();
                    params.put(keyParam, values);
                }
                values.add(value);
            } else {
                params.put(pair, Arrays.asList(value));
            }
        }
        return params;
    }

    private void checkIdClazz(Class clazz) throws IntrospectionException, OgnlException {
        if (IBaseModel.class.isAssignableFrom(clazz)) {
            OgnlPropertyDescriptor idProperty = OgnlRuntime.getPropertyDescriptor(clazz, IBaseModel.ID_FIELD);
            if (idProperty != null && idProperty.isPropertyDescriptor() && idProperty.getAnnotation(Id.class) != null) {
                idClazz = (Class<Serializable>) idProperty.getReadMethod().getReturnType();
            }
        }
    }

    private <T> EntityGraph<T> createEntityGraph(Class<T> clazz, String ... relations) {
        if(relations == null || relations.length == 0) {
            return null;
        }
        EntityGraph<T> graph = em.createEntityGraph(clazz);
        Stream.of(relations).forEach(path -> {
            String[] splitted = path.split("\\.");
            Subgraph<T> root = graph.addSubgraph(splitted[0]);
            for (int i = 1; i < splitted.length; i++)
                root = root.addSubgraph(splitted[i]);
        });
        return graph;
    }
}
