package com.github.madz0.springbinder.binding.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.rest.annotation.RestObject;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

public class RestObjectHandlerMethodArgument extends AbstractModelBindingArgumentResolver {

    ObjectMapper mapper;
    EntityManager entityManager;

    public RestObjectHandlerMethodArgument(ObjectMapper mapper, EntityManager entityManager) {
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RestObject.class) != null;
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

        if(value == null && builder.length() > 0) {
            RestObject formObject = parameter.getParameterAnnotation(RestObject.class);
            try {
                BindUtils.group.set(formObject.group());
                value = mapper.readValue(builder.toString(), parameter.getParameterType());
                binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                validateIfApplicable(binder, parameter);
                bindingResult = binder.getBindingResult();
            }
            finally {
                BindUtils.group.remove();
            }
        }

        return finalizeBinding(parameter, mavContainer, webRequest, binderFactory, name, bindingResult, value);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return false;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

    }
}
