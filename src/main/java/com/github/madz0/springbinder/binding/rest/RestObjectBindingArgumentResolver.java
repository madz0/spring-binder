package com.github.madz0.springbinder.binding.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.DefaultEntityManagerBeanNameProvider;
import com.github.madz0.springbinder.binding.rest.annotation.RestObject;
import ognl.OgnlRuntime;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class RestObjectBindingArgumentResolver extends AbstractModelBindingArgumentResolver {

    private ObjectMapper mapper;

    public RestObjectBindingArgumentResolver(Map<String, EntityManager> emMap, ObjectMapper mapper) {
        super(emMap);
        this.mapper = mapper;
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

        if (value == null) {
            StringBuilder builder = (StringBuilder) getServletData(request);
            if (builder.length() > 0) {
                RestObject restObject = parameter.getParameterAnnotation(RestObject.class);
                try {
                    EntityManager em = emMap.get(restObject.entityManagerBean());
                    BindUtils.group.set(restObject.group());
                    BindUtils.updating.set(restObject.isUpdating());
                    BindUtils.entityGraph.set(createEntityGraph(em, parameter.getParameterType(), restObject.entityGraph()));
                    BindUtils.bindAsDto.set(restObject.bindAsDto());
                    value = mapper.readValue(builder.toString(), parameter.getParameterType());
                    binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                    validateIfApplicable(binder, parameter);
                    bindingResult = binder.getBindingResult();
                } finally {
                    BindUtils.group.remove();
                    BindUtils.updating.remove();
                    BindUtils.entityGraph.remove();
                    BindUtils.bindAsDto.remove();
                }
            } else {
                Class pClass = parameter.getParameterType();
                value = OgnlRuntime.createProperObject(pClass, pClass.getComponentType());
                bindingResult = validateEmptyRequest(parameter, value, binderFactory, webRequest);
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
