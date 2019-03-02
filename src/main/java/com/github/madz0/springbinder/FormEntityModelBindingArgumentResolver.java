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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;

public class FormEntityModelBindingArgumentResolver implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {
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
        try (BufferedReader br = request.getReader()) {
            char[] readBytes = new char[1024];
            int readSize;
            while ((readSize = br.read(readBytes)) != -1) {
                builder.append(new String(readBytes, 0, readSize));
            }
        }
        String name = parameter.getParameterName();
        BindingResult bindingResult = null;
        Object value = null;
        WebDataBinder binder = null;
        if (mavContainer.containsAttribute(name)) {
            value = mavContainer.getModel().get(name);
        }

        if (value == null && builder.length() > 0) {
            mavContainer.setBinding(parameter.getParameterName(), true);
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
                if(idClazz == null) {
                    checkIdClazz(cls);
                }
                context.extend();
            }
            context.addObjectConstructor(new EntityModelObjectConstructor<>(em, idClazz,
                    createEntityGraph(cls, parameter.getParameterAnnotation(FormObject.class).entityGraph())));
            builder.insert(0, '?');
            Map<String, List<String>> params = parsQuery(builder.toString());
            value = Ognl.getValue(params.get(name), context, cls);
            if (parameter.hasParameterAnnotation(Validated.class)) {
                binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                binder.validate();
                bindingResult = binder.getBindingResult();
            }
        }

        if(bindingResult == null) {
            binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
            bindRequestParameters(binder, webRequest);
            if (binder.getTarget() != null) {
                if (!mavContainer.isBindingDisabled(name)) {
                    bindRequestParameters(binder, webRequest);
                }
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                    throw new BindException(binder.getBindingResult());
                }
            }
            if (!parameter.getParameterType().isInstance(value)) {
                value = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
            }
            bindingResult = binder.getBindingResult();
        }

        Map<String, Object> bindingResultModel = bindingResult.getModel();
        mavContainer.removeAttributes(bindingResultModel);
        mavContainer.addAllAttributes(bindingResultModel);

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

    protected boolean isBindExceptionRequired(MethodParameter parameter) {
        int i = parameter.getParameterIndex();
        Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }

    protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
        return isBindExceptionRequired(parameter);
    }

    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        ((WebRequestDataBinder) binder).bind(request);
    }

    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        for (Annotation ann : parameter.getParameterAnnotations()) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                if (hints != null) {
                    Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
                    binder.validate(validationHints);
                }
                else {
                    binder.validate();
                }
                break;
            }
        }
    }
}
