package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.EmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a email constraint for a string field.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface Email {
	String message() default EmailValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
