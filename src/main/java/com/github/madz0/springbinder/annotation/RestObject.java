package com.github.madz0.springbinder.annotation;

import ir.iiscenter.springform.model.BaseGroups;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RestObject {
    String[] entityGraph() default {};
    Class<BaseGroups.IGroup> group();
}