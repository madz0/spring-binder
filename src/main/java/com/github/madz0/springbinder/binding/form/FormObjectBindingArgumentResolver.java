package com.github.madz0.springbinder.binding.form;

import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import com.github.madz0.springbinder.model.IBaseModel;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.extended.DefaultMemberAccess;
import ognl.extended.OgnlPropertyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Subgraph;
import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FormObjectBindingArgumentResolver extends AbstractModelBindingArgumentResolver {
    private EntityManager em;

    public FormObjectBindingArgumentResolver(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(FormObject.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String name = parameter.getParameterName();
        BindingResult bindingResult = null;
        Object value = null;
        WebDataBinder binder = null;
        if (mavContainer.containsAttribute(name)) {
            value = mavContainer.getModel().get(name);
        }

        if (value == null) {
            StringBuilder builder = getServeltData(request);
            if (builder.length() > 0) {
                mavContainer.setBinding(parameter.getParameterName(), true);
                Type type = parameter.getParameterType();
                OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                Class cls = null;
                if (type instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType) type;
                    cls = (Class) ptype.getRawType();
                    context.extend(ptype);
                } else {
                    cls = (Class) type;
                    context.extend();
                }
                FormObject formObject = parameter.getParameterAnnotation(FormObject.class);
                context.setObjectConstructor(new EntityModelObjectConstructor(em, formObject.idClass(),
                        createEntityGraph(cls, formObject.entityGraph()), formObject.group()));
                builder.insert(0, '?');
                Map<String, List<String>> params = parsQuery(builder.toString(), name);
                value = Ognl.getValue(params.get(name), context, cls);
                binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                validateIfApplicable(binder, parameter);
                bindingResult = binder.getBindingResult();
            }
        }

        return finalizeBinding(parameter, mavContainer, webRequest, binderFactory, name, bindingResult, value);
    }

    private Map<String, List<String>> parsQuery(String path, String expectedRoot)
            throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        if (path == null || !path.startsWith("?")) {
            return null;
        }
        String queryWithoutQuestionMark = path.replaceFirst("\\?", "&");
        final String[] pairs = queryWithoutQuestionMark.split("&");

        Map<String, AtomicInteger> notIndexFixMap = new HashMap<>();

        for (String pair : pairs) {
            if (pair == null || pair.equals("")) {
                continue;
            }

            if (pair.startsWith("_")) {
                continue;
            }

            final int idx = pair.lastIndexOf("=");
            String key = null;
            String value = null;
            if (idx > 0 && pair.length() >= idx + 1) {

                key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");

                //Fix multi select issue
                if (key.endsWith("[%d]")) {
                    int genIndex = 0;
                    if (!notIndexFixMap.containsKey(key)) {
                        notIndexFixMap.put(key, new AtomicInteger(0));
                    } else {
                        genIndex = notIndexFixMap.get(key).incrementAndGet();
                    }
                    key = String.format(key, genIndex);
                }

                if (expectedRoot != null && !key.startsWith(expectedRoot)) {
                    key = expectedRoot + "." + key;
                }

                final int bracketIdx = key.indexOf("[");
                final int dotIdx = key.indexOf(".");
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
                if (values == null) {
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

    private <T> EntityGraph<T> createEntityGraph(Class<T> clazz, String... relations) {
        if (relations == null || relations.length == 0) {
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

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        if (returnValue != null) {
            String name = ModelFactory.getNameForReturnValue(returnValue, returnType);
            mavContainer.addAttribute(name, returnValue);
        }
    }
}
