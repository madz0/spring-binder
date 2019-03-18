package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.MinValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a minumum value for a numeric field.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinValidator.class)
public @interface Min {
	String message() default MinValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	long value();
}