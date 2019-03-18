package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.PatternValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a pattern constraint for a string field.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PatternValidator.class)
public @interface Pattern {
	String message() default PatternValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String value();
}
