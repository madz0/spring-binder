package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.processor.annotation.FieldExtractor;

@FieldExtractor(as="IdModelFields")
public interface IdModel<T> {

    T getId();
    void setId(T t);
}
