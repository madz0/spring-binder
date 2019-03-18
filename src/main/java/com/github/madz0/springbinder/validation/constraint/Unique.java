package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.repository.BaseRepository;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
//@Constraint(validatedBy = UniqueValidator.class)
@Repeatable(value = Uniques.class)
public @interface Unique {

    String field();

    String[] conditionFields() default {};

    Class<? extends BaseRepository> repositoryClass();

    String message() default "validation.error.unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
