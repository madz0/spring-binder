package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.MinLength;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for <code>@MinLength</code> fields.
 */
public class MinLengthValidator implements
		ConstraintValidator<MinLength, String> {
	final static public String message = "validation.error.minLength";
	private long min;

	@Override
	public void initialize(MinLength constraintAnnotation) {
		this.min = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() == 0) {
			return true;
		}
		return value.length() >= min;
	}

}
