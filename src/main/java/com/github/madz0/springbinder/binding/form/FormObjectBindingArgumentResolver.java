package com.github.madz0.springbinder.binding.form;

import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.DefaultEntityManagerBeanNameProvider;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.extended.DefaultMemberAccess;
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
        WebDataBinder binder = null;
        if (mavContainer.containsAttribute(name)) {
            value = mavContainer.getModel().get(name);
        }

        if (value == null) {
            Object ret = getServletData(request);
            FormObject formObject = parameter.getParameterAnnotation(FormObject.class);
            List<Map.Entry<String, Object>> entries = null;
            if ((ret instanceof StringBuilder)) {
                StringBuilder builder = (StringBuilder) ret;
                if (builder.length() > 0) {
                    builder.insert(0, '?');
                    Map<String, List<Map.Entry<String, Object>>> params = parsQuery(builder.toString(), name, formObject.fieldsContainRootName());
                    entries = params.get(name);
                }
            } else if (ret instanceof Map) {
                Map map = (Map) ret;
                if (map.size() > 0) {
                    Map<String, List<Map.Entry<String, Object>>> params = parsQuery(map, name, formObject.fieldsContainRootName());
                    entries = params.get(name);
                }
            }

            if (entries == null) {
                entries = new ArrayList<>();
            }
            addMultiParFiles(request, entries);
            if (entries != null && entries.size() > 0) {
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
                EntityManager em = emMap.get(formObject.entityManagerBean());
                context.setObjectConstructor(new EntityModelObjectConstructor(em,
                        createEntityGraph(em, cls, formObject.entityGraph()),
                        formObject.group(), idClassMapper, formObject.bindAsDto()));
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

    private Map<String, List<Map.Entry<String, Object>>> parsQuery(String path, String expectedRoot, Boolean fieldsContainRootName)
            throws UnsupportedEncodingException {
        Map<String, List<Map.Entry<String, Object>>> params = new LinkedHashMap<>();
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

            final int idx = pair.indexOf("=");
            String key = null;
            String value = null;
            if (idx > 0 && pair.length() >= idx + 1) {
                key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                //Fix multi select issue
                boolean multiSelect = false;
                String multiSelectIdentifier = null;
                String multiSelectEqualSign = null;
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

                if (multiSelect) {
                    int genIndex = notIndexedFixMap.computeIfAbsent(key, x -> new AtomicInteger(-1)).incrementAndGet();
                    key = key.replaceFirst("\\[\\]", "[" + genIndex + "]");
                }

                if (expectedRoot != null && !fieldsContainRootName) {
                    key = expectedRoot + "." + key;
                }

                final int bracketIdx = key.indexOf("[");
                final int dotIdx = key.indexOf(".");
                int finalIdx = 0;
                String keyParam = null;
                if (value == null) {
                    value = convertNullValue();
                }

                if (dotIdx == bracketIdx) {
                    keyParam = key;
                    params.put(keyParam, new ArrayList<>());
                } else if (dotIdx != -1 && (bracketIdx == -1 || bracketIdx > dotIdx)) {
                    keyParam = key.substring(0, dotIdx);
                    finalIdx = dotIdx + 1;
                } else {
                    keyParam = key.substring(0, bracketIdx);
                    finalIdx = bracketIdx;
                }
                List<Map.Entry<String, Object>> values = params.computeIfAbsent(keyParam, k -> new ArrayList<>());
                if (multiSelectIdentifier != null) {
                    handleMultiSelect(multiSelectIdentifier, multiSelectEqualSign, value, values, key, finalIdx);
                    continue;
                }
                Map.Entry<String, Object> valueEntry = new AbstractMap.SimpleImmutableEntry<>(key.substring(finalIdx), value);
                values.add(valueEntry);
            } else {
                params.put(pair, Collections.emptyList());
            }
        }
        return params;
    }

    private Map<String, List<Map.Entry<String, Object>>> parsQuery(Map<String, String[]> servletParams, String expectedRoot, Boolean fieldsContainRootName) {
        Map<String, List<Map.Entry<String, Object>>> params = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> pair : servletParams.entrySet()) {
            String key = pair.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            boolean multiSelect = false;
            String multiSelectIdentifier = null;
            String multiSelectEqualSign = null;
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
            for (String value : pair.getValue()) {
                String finalKey = key;
                if (multiSelect) {
                    finalKey = finalKey.replaceFirst("\\[\\]", "[" + (genIndex++) + "]");
                    if (multiSelectIdentifier != null) {
                        handleMultiSelect(multiSelectIdentifier, multiSelectEqualSign, value, values, finalKey, finalIdx + 1);
                        continue;
                    }
                }
                values.add(new AbstractMap.SimpleImmutableEntry<>(finalKey.substring(finalIdx + 1), value));
            }
        }
        return params;
    }

    private String convertNullValue() {
        return "";
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
}
