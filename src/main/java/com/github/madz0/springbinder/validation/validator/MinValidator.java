package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.Min;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for <code>@Min</code> fields.
 */
public class MinValidator implements ConstraintValidator<Min, Number> {

	final static public String message = "validation.error.min";
	private long min;

	public void initialize(Min constraintAnnotation) {
		this.min = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return value.longValue() >= min;
	}

}