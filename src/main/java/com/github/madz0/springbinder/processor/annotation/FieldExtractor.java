package com.github.madz0.springbinder.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FieldExtractor {
    /**
     * The name of the implementation that will be generated. It should be a
     * valid/unique java qualifier name
     * @return
     */
    String as();

    /**
     * If true a corresponding builder (based on builder design pattern)
     * is also generated.
     * @return
     */
    boolean builder() default false;
}