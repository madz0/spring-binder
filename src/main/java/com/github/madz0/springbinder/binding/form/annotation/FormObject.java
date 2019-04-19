package com.github.madz0.springbinder.binding.form.annotation;

import com.github.madz0.springbinder.binding.DefaultEntityManagerBeanNameProvider;
import com.github.madz0.springbinder.model.BaseGroups;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FormObject {
    String entityManagerBean() default DefaultEntityManagerBeanNameProvider.DEFAULT_NAME;
    String[] entityGraph() default {};
    Class<? extends BaseGroups.IGroup> group() default BaseGroups.IGroup.class;
    boolean fieldsContainRootName() default false;
    boolean bindAsDto() default false;
}
