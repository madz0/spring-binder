package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.MinLengthValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a minumum length for a string field.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinLengthValidator.class)
public @interface MinLength {
	String message() default MinLengthValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	long value();
}
