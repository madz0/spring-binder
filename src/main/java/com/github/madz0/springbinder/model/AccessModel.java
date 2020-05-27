package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.processor.annotation.FieldExtractor;

@FieldExtractor(as = "AccessModelFields")
public interface AccessModel {

    default String[] getAccess() {
        return new String[]{};
    }
}
