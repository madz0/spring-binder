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
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
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

    protected Object getServletData(HttpServletRequest request) throws IOException {
        if (request.getParameterMap().size() > 0) {
            return request.getParameterMap();
        }
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

    protected void addMultiParFiles(ServletRequest request, List values) {
        while (request instanceof HttpServletRequestWrapper) {
            request = ((HttpServletRequestWrapper) request).getRequest();
        }
        if (request instanceof MultipartHttpServletRequest) {
            Map<String, MultipartFile> multipartFileMap = ((MultipartHttpServletRequest) request).getFileMap();
            if (multipartFileMap != null) {
                multipartFileMap.entrySet().forEach(e -> values.add(e));
            }
        }
    }

    public <T> EntityGraph<T> createEntityGraph(EntityManager em, Class<T> clazz, String... relations) {
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

    public BindingResult validateEmptyRequest(MethodParameter parameter, Object value, WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {
        String parameterName = parameter.getParameterName();
        WebDataBinder binder = binderFactory.createBinder(webRequest, value, parameterName);
        validateIfApplicable(binder, parameter);
        return binder.getBindingResult();
    }
}
