package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.validation.ValidationError;

import java.util.List;

public interface Validate {
    List<ValidationError> validate(Class<?> group);
}
