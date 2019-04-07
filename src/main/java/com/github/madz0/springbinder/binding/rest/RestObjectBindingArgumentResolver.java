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

public class RestObjectBindingArgumentResolver extends AbstractModelBindingArgumentResolver {

    ObjectMapper mapper;
    EntityManager entityManager;

    public RestObjectBindingArgumentResolver(ObjectMapper mapper, EntityManager entityManager) {
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
        String name = parameter.getParameterName();
        BindingResult bindingResult = null;
        Object value = null;
        WebDataBinder binder = null;
        if (mavContainer.containsAttribute(name)) {
            value = mavContainer.getModel().get(name);
        }

        if(value == null) {
            StringBuilder builder = getServletData(request);
            if(builder.length() > 0) {
                RestObject restObject = parameter.getParameterAnnotation(RestObject.class);
                try {
                    BindUtils.group.set(restObject.group());
                    BindUtils.updating.set(restObject.isUpdating());
                    BindUtils.entityGraph.set(createEntityGraph(entityManager, parameter.getParameterType(), restObject.entityGraph()));
                    value = mapper.readValue(builder.toString(), parameter.getParameterType());
                    binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                    validateIfApplicable(binder, parameter);
                    bindingResult = binder.getBindingResult();
                }
                finally {
                    BindUtils.group.remove();
                    BindUtils.updating.remove();
                }
            }
            else {
                bindingResult = validateEmptyRequest(parameter, binderFactory, webRequest);
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
