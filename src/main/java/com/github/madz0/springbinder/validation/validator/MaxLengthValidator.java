package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.MaxLength;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for <code>@MaxLength</code> fields.
 */
public class MaxLengthValidator implements
		ConstraintValidator<MaxLength, String> {
	final static public String message = "validation.error.maxLength";
	private long max;

	@Override
	public void initialize(MaxLength constraintAnnotation) {
		this.max = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() == 0) {
			return true;
		}
		return value.length() <= max;
	}

}
