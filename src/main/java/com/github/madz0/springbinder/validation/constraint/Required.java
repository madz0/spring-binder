package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.RequiredValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a field as required.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredValidator.class)
public @interface Required {
	String message() default RequiredValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
