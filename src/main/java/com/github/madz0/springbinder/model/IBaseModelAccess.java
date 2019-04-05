package com.github.madz0.springbinder.model;

public interface IBaseModelAccess {
    String ACCESS_FIELD = "access";

    default String[] getAccess() {
        return null;
    }
}
