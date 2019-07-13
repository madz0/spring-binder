package com.github.madz0.springbinder.model;

public interface AccessModel {
    String ACCESS_FIELD = "access";

    default String[] getAccess() {
        return null;
    }
}
