package com.github.madz0.springbinder.binding.rest.annotation;

import com.github.madz0.springbinder.model.BaseGroups;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RestObject {
    String[] entityGraph() default {};
    Class<? extends BaseGroups.IGroup> group() default BaseGroups.IGroup.class;
    Class<? extends Serializable> idClass();
    boolean isUpdating() default false;
}
