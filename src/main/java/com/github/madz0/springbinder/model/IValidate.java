package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.validation.ValidationError;

import java.util.List;

public interface IValidate {
    List<ValidationError> validate(Class<?> group);
}
