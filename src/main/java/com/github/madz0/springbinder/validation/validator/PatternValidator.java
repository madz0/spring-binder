package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for <code>@Pattern</code> fields.
 */
public class PatternValidator implements ConstraintValidator<Pattern, String> {
	final static public String message = "validation.error.pattern";
	java.util.regex.Pattern regex = null;

	@Override
	public void initialize(Pattern constraintAnnotation) {
		regex = java.util.regex.Pattern.compile(constraintAnnotation.value());
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() == 0) {
			return true;
		}
		return regex.matcher(value).matches();
	}
}
