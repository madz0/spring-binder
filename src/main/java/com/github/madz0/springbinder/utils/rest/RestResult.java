package com.github.madz0.springbinder.utils.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class RestResult<T>{
    public final T result;
    public final HttpStatus httpStatus;
    public final String errorMessage;

    public boolean isSuccessful(){
        return httpStatus != null && httpStatus.is2xxSuccessful();
    }
}
