package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.Max;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for <code>@Max</code> fields.
 */
public class MaxValidator implements ConstraintValidator<Max, Number> {

	final static public String message = "validation.error.max";
	private long max;

	@Override
	public void initialize(Max constraintAnnotation) {
		this.max = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		return value.longValue() <= max;
	}

}
