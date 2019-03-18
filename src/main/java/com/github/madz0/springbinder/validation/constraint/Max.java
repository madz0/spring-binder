package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.MaxValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a maximum value for a numeric field.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxValidator.class)
public @interface Max {
	String message() default MaxValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	long value();
}
