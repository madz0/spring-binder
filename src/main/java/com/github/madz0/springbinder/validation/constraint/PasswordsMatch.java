package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.validation.validator.PasswordsMatchValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {
    String message();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
