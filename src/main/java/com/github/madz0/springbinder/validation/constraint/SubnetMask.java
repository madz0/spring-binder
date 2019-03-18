package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.SubnetMaskValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * used only for IPv4
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SubnetMaskValidator.class)
public @interface SubnetMask {

    String message() default SubnetMaskValidator.message;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
