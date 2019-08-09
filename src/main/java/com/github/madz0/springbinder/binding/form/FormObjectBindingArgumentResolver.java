package com.github.madz0.springbinder.binding.form;

import com.github.madz0.ognl2.Ognl;
import com.github.madz0.ognl2.OgnlContext;
import com.github.madz0.ognl2.OgnlRuntime;
import com.github.madz0.ognl2.extended.DefaultMemberAccess;
import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormObjectBindingArgumentResolver extends AbstractModelBindingArgumentResolver {
    private static final Pattern multiPostWithIdentifierPattern = Pattern.compile("\\[\\((.*?)\\)\\((.*?)\\)\\]");
    private IdClassMapper idClassMapper;

    public FormObjectBindingArgumentResolver(Map<String, EntityManager> emMap,
                                             IdClassMapper idClassMapper) {
        super(emMap);
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
        WebDataBinder binder;
        if (mavContainer.containsAttribute(name)) {
            value = mavContainer.getModel().get(name);
        }

        if (value == null) {
            Map<String, ? extends Object[]> map = getServletDataAsMap(request);
            FormObject formObject = Objects.requireNonNull(parameter.getParameterAnnotation(FormObject.class), "Wrong parameter for FormObject");
            List<Map.Entry<String, Object>> entries = null;
            map = new LinkedHashMap<>(map);
            addMultiParFiles(request, (Map) map);
            if (map.size() > 0) {
                Map<String, List<Map.Entry<String, Object>>> params = parsQuery(map, name, formObject.fieldsContainRootName());
                entries = params.get(name);
            }

            if (entries != null && entries.size() > 0) {
                mavContainer.setBinding(parameter.getParameterName(), true);
                Type type = parameter.getParameterType();
                OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
                Class cls;
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    cls = (Class) pType.getRawType();
                    context.extend(pType);
                } else {
                    cls = (Class) type;
                    context.extend();
                }
                EntityManager em = emMap.get(formObject.entityManagerBean());
                context.setObjectConstructor(new EntityModelObjectConstructor(em,
                        createEntityGraph(em, cls, formObject.entityGraph()),
                        formObject.group(), idClassMapper, formObject.dtoBinding()));
                value = Ognl.getValue(entries, context, cls);
                binder = binderFactory.createBinder(webRequest, value, parameter.getParameterName());
                validateIfApplicable(binder, parameter);
                bindingResult = binder.getBindingResult();
            } else {
                Class pClass = parameter.getParameterType();
                value = OgnlRuntime.createProperObject(pClass, pClass.getComponentType());
                bindingResult = validateEmptyRequest(parameter, value, binderFactory, webRequest);
            }
        }
        return finalizeBinding(parameter, mavContainer, webRequest, binderFactory, name, bindingResult, value);
    }

    private Map<String, List<Map.Entry<String, Object>>> parsQuery(Map<String, ? extends Object[]> servletParams, String expectedRoot, Boolean fieldsContainRootName) {
        Map<String, List<Map.Entry<String, Object>>> params = new LinkedHashMap<>();
        for (Map.Entry<String, ? extends Object[]> pair : servletParams.entrySet()) {
            String key = pair.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            boolean multiSelect = false;
            String multiSelectIdentifier = null;
            String multiSelectEqualSign = null;
            key = convertDashToUnderscore(key);
            if (key.contains("[]")) {
                multiSelect = true;
            }

            if (!multiSelect) {
                Matcher matcher = multiPostWithIdentifierPattern.matcher(key);
                if (matcher.find()) {
                    multiSelect = true;
                    multiSelectIdentifier = matcher.group(1);
                    multiSelectEqualSign = matcher.group(2);
                    key = matcher.replaceFirst("[]");
                }
            }
            if (expectedRoot != null && !fieldsContainRootName) {
                key = expectedRoot + "." + key;
            }
            final int bracketIdx = key.indexOf("[");
            final int dotIdx = key.indexOf(".");
            final int finalIdx = dotIdx == bracketIdx ? -1 :
                    dotIdx != -1 && (bracketIdx == -1 || bracketIdx > dotIdx) ? dotIdx : bracketIdx;
            int genIndex = 0;
            final List<Map.Entry<String, Object>> values = params.computeIfAbsent(finalIdx == -1 ? key : key.substring(0, finalIdx), x -> new ArrayList<>());
            for (Object value : pair.getValue()) {
                String finalKey = key;
                if (multiSelect) {
                    finalKey = finalKey.replaceFirst("\\[\\]", "[" + (genIndex++) + "]");
                    if (multiSelectIdentifier != null && value instanceof String) {
                        handleMultiSelect(multiSelectIdentifier, multiSelectEqualSign, (String) value, values, finalKey, finalIdx + 1);
                        continue;
                    }
                }
                values.add(new AbstractMap.SimpleImmutableEntry<>(finalKey.substring(finalIdx + 1), value));
            }
        }
        return params;
    }

    private void handleMultiSelect(String multiSelectIdentifier, String multiSelectEqualSign, String value, final List<Map.Entry<String, Object>> values, String finalKey, int finalIdx) {
        for (String eachPart : value.split(multiSelectIdentifier)) {
            if (eachPart != null && !eachPart.equals("")) {
                String[] partArr = eachPart.split(multiSelectEqualSign);
                String partKey = partArr[0];
                String partValue = partArr.length > 1 ? partArr[1] : convertNullValue();
                values.add(new AbstractMap.SimpleImmutableEntry<>(finalKey.substring(finalIdx) + partKey, partValue));
            }
        }
    }

    private String convertDashToUnderscore(String param) {
        return param.replaceAll("-", "_");
    }
}
