package com.github.madz0.springbinder.binding.form;

import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.extended.DefaultMemberAccess;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FormObjectBindingArgumentResolver extends AbstractModelBindingArgumentResolver {
    private EntityManager em;
    private IdClassMapper idClassMapper;
    public FormObjectBindingArgumentResolver(EntityManager em, IdClassMapper idClassMapper) {
        this.em = em;
        this.idClassMapper = idClassMapper;
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
                context.setObjectConstructor(new EntityModelObjectConstructor(em, createEntityGraph(em, cls, formObject.entityGraph()), formObject.group(), idClassMapper));
                builder.insert(0, '?');
                Map<String, List<String>> params = parsQuery(builder.toString(), name, formObject.fieldsContainRootName());
                value = Ognl.getValue(params.get(name), context, cls);
                binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                validateIfApplicable(binder, parameter);
                bindingResult = binder.getBindingResult();
            }
        }

        return finalizeBinding(parameter, mavContainer, webRequest, binderFactory, name, bindingResult, value);
    }

    private Map<String, List<String>> parsQuery(String path, String expectedRoot, Boolean fieldsContainRootName)
            throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        if (path == null || !path.startsWith("?")) {
            return null;
        }
        String queryWithoutQuestionMark = path.replaceFirst("\\?", "&");
        final String[] pairs = queryWithoutQuestionMark.split("&");

        Map<String, AtomicInteger> notIndexedFixMap = new HashMap<>();

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
                if (key.contains("[]")) {
                    int genIndex = 0;
                    if (!notIndexedFixMap.containsKey(key)) {
                        notIndexedFixMap.put(key, new AtomicInteger(0));
                    } else {
                        genIndex = notIndexedFixMap.get(key).incrementAndGet();
                    }
                    key = key.replaceFirst("\\[\\]", "["+genIndex+"]");
                }

                if (expectedRoot != null && !fieldsContainRootName) {
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
