package com.github.madz0.springbinder.binding;

import com.github.madz0.springbinder.binding.rest.serialize.RestResultFactory;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractModelBindingArgumentResolver implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {
    protected Map<String, EntityManager> emMap;

    protected AbstractModelBindingArgumentResolver(Map<String, EntityManager> emMap) {
        this.emMap = emMap;
        this.emMap.putIfAbsent(DefaultEntityManagerBeanNameProvider.DEFAULT_NAME, this.emMap.entrySet().iterator().next().getValue());
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
        if (binder instanceof WebRequestDataBinder) {
            ((WebRequestDataBinder) binder).bind(request);
        }
    }

    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        for (Annotation ann : parameter.getParameterAnnotations()) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                if (hints != null) {
                    Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[]{hints});
                    binder.validate(validationHints);
                } else {
                    binder.validate();
                }
                break;
            }
        }
    }

    protected Object finalizeBinding(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory, String name, BindingResult bindingResult, Object value) throws Exception {
        WebDataBinder binder;
        if (bindingResult == null) {
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

    protected Map<String, ? extends Object[]> getServletDataAsMap(HttpServletRequest request) throws IOException {
        if (request.getParameterMap().size() == 0) {
            request.getParameter("_someParam");
            if (request.getParameterMap().size() == 0) {
                StringBuilder sb = getServletDataAsStringBuilder(request);
                if(request.getQueryString() != null && request.getQueryString().length() > 0) {
                    sb.append('&').append(request.getQueryString());
                }
                return parsQuery(sb.toString());
            }
        }
        return request.getParameterMap();
    }

    protected StringBuilder getServletDataAsStringBuilder(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (request.getContentLength() > 0) {
            try (BufferedReader br = request.getReader()) {
                char[] readBytes = new char[1024];
                int readSize;
                while ((readSize = br.read(readBytes)) != -1) {
                    builder.append(new String(readBytes, 0, readSize));
                }
            }
        }
        return builder;
    }

    private Map<String, List<MultipartFile>> getMultiParFiles(ServletRequest request) {
        while (!(request instanceof MultipartHttpServletRequest) &&
                request instanceof HttpServletRequestWrapper) {
            request = ((HttpServletRequestWrapper) request).getRequest();
        }

        if (request instanceof MultipartHttpServletRequest) {
            return ((MultipartHttpServletRequest) request).getMultiFileMap();
        }
        return null;
    }

    protected void addMultiParFiles(ServletRequest request, Map<String, Object[]> values) {
        Map<String, List<MultipartFile>> multipartFileMap = getMultiParFiles(request);
        if (multipartFileMap != null) {
            values.putAll(multipartFileMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().toArray())));
        }
    }

    protected <T> EntityGraph<T> createEntityGraph(EntityManager em, Class<T> clazz, String... relations) {
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
        return false;//RestResultBody.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        if (returnValue instanceof RestResultFactory) {
            RestResultFactory restResultBody = (RestResultFactory) returnValue;
            mavContainer.setStatus(restResultBody.getStatus());
            String name = ModelFactory.getNameForReturnValue(returnValue, returnType);
            mavContainer.addAttribute(name, returnValue);
        }
    }

    protected BindingResult validateEmptyRequest(MethodParameter parameter, Object value, WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {
        String parameterName = parameter.getParameterName();
        WebDataBinder binder = binderFactory.createBinder(webRequest, value, parameterName);
        validateIfApplicable(binder, parameter);
        return binder.getBindingResult();
    }

    private Map<String, String[]> parsQuery(String path)
            throws UnsupportedEncodingException {
        Map<String, List<String>> params = new LinkedHashMap<>();
        if (path == null) {
            return null;
        }
        if (path.startsWith("?")) {
            path = path.replaceFirst("\\?", "");
        }
        final String[] pairs = path.split("&");

        for (String pair : pairs) {
            if (pair == null || pair.equals("")) {
                continue;
            }

            final int idx = pair.indexOf("=");
            String key;
            String value;
            if (idx > 0 && pair.length() >= idx + 1) {
                key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                if (value == null) {
                    value = convertNullValue();
                }
                List<String> values = params.computeIfAbsent(key, k -> new ArrayList<>());
                values.add(value);
            } else {
                params.put(pair, Collections.emptyList());
            }
        }
        return params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().toArray(new String[0])));
    }

    protected String convertNullValue() {
        return "";
    }
}
