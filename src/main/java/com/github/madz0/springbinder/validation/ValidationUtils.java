package com.github.madz0.springbinder.validation;

import com.github.madz0.springbinder.model.IValidate;
import com.github.madz0.springbinder.validation.constraint.Unique;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.*;

public class ValidationUtils {
    private ValidationUtils() {
    }

    private static final Set<String> internalAnnotationAttributes = new HashSet<>(3);
    private static final Set<String> uniqueInternalAnnotationAttributes;

    static {
        internalAnnotationAttributes.add("message");
        internalAnnotationAttributes.add("groups");
        internalAnnotationAttributes.add("payload");
        uniqueInternalAnnotationAttributes = new HashSet<>(internalAnnotationAttributes);
        uniqueInternalAnnotationAttributes.add("conditionFields");
        uniqueInternalAnnotationAttributes.add("repositoryClass");
    }

    public static List<ValidationError> validate(Object object) {
        return validate(object, null, null);
    }

    public static List<ValidationError> validate(Object object, Class<?> group) {
        return validate(object,null, group );
    }

    @SuppressWarnings("unchecked")
    public static List<ValidationError> validate(Object object, String parentFieldName, Class<?> group){
        SpringValidatorAdapter validator = new SpringValidatorAdapter(Validation.validator);
        Set<ConstraintViolation<Object>> validationErrors;
        if(group == null){
            validationErrors = validator.validate(object);
        }else{
            validationErrors = validator.validate(object, group);
        }
        List<ValidationError> errors = convertValidationErrors(validationErrors, parentFieldName);

        if(object instanceof IValidate) {
            List<ValidationError> globalErrors = ((IValidate) object).validate(group);
            errors.addAll(globalErrors);
        }

        return errors;
    }

    public static List<ValidationError> convertValidationErrors(Set<ConstraintViolation<Object>> validationErrors, String parentFieldName) {
        List<ValidationError> errors = new ArrayList<>();
        if (validationErrors == null || validationErrors.isEmpty()) {
            return errors;
        }
        if(StringUtils.isEmpty(parentFieldName)){
            parentFieldName="";
        }else{
            parentFieldName=parentFieldName+".";
        }
        for (ConstraintViolation<Object> violation : validationErrors) {
            String field = parentFieldName+violation.getPropertyPath().toString();
            if(Unique.class.equals(violation.getConstraintDescriptor().getAnnotation().annotationType())){
                field = ((Unique)violation.getConstraintDescriptor().getAnnotation()).field();
            }
            Object[] errorArgs = getArgumentsForConstraint(field, violation.getConstraintDescriptor());
            String defaultMessage = violation.getMessage();

            ValidationError validationError = new ValidationError(field, defaultMessage,
                    convertErrorArguments(errorArgs));
            errors.add(validationError);
        }
        return errors;
    }

    private static List<Object> convertErrorArguments(Object[] arguments) {
        List<Object> converted = new ArrayList<>(arguments.length);
        for (Object arg : arguments) {
            if (!(arg instanceof DefaultMessageSourceResolvable)) {
                converted.add(arg);
            }
        }
        return Collections.unmodifiableList(converted);
    }

    private static Object[] getArgumentsForConstraint(String field, ConstraintDescriptor<?> descriptor) {
        List<Object> arguments = new LinkedList<>();
        String[] codes = new String[]{Errors.NESTED_PATH_SEPARATOR + field, field};
        arguments.add(new DefaultMessageSourceResolvable(codes, field));
        // Using a TreeMap for alphabetical ordering of attribute names
        Map<String, Object> attributesToExpose = new TreeMap<>();
        for (Map.Entry<String, Object> entry : descriptor.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            Object attributeValue = entry.getValue();
            Set<String> internalAttributies = descriptor.getAnnotation() instanceof Unique ?
                    uniqueInternalAnnotationAttributes :
                    internalAnnotationAttributes;
            if (!internalAttributies.contains(attributeName)) {
                attributesToExpose.put(attributeName, attributeValue);
            }
        }
        arguments.addAll(attributesToExpose.values());
        return arguments.toArray(new Object[arguments.size()]);
    }
}
