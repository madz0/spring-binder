package com.github.madz0.springbinder.binding.rest.annotation;

import com.github.madz0.springbinder.binding.DefaultEntityManagerBeanNameProvider;
import com.github.madz0.springbinder.model.Groups;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RestObject {
    String entityManagerBean() default DefaultEntityManagerBeanNameProvider.DEFAULT_NAME;
    String[] entityGraph() default {};
    Class<? extends Groups.IGroup> group() default Groups.IGroup.class;
    boolean isUpdating() default false;
    boolean dtoBinding() default false;
}
