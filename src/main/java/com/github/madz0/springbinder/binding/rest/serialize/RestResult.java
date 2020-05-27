package com.github.madz0.springbinder.binding.rest.serialize;

import org.springframework.http.ResponseEntity;

public class RestResult<T> extends ResponseEntity<RestResultFactory<T>> {

    public RestResult(RestResultFactory<T> body) {
        super(body, body.getStatus());
    }
}
