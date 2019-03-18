package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.Required;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

/**
 * Validator for <code>@Required</code> fields.
 */
public class RequiredValidator implements ConstraintValidator<Required, Object> {
	final static public String message = "validation.error.required";

	@Override
	public void initialize(Required constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return !((String) value).isEmpty();
		}
		if (value instanceof Collection) {
			return !((Collection<?>) value).isEmpty();
		}
		return true;
	}

}
