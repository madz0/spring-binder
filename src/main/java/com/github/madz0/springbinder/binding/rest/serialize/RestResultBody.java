package com.github.madz0.springbinder.binding.rest.serialize;

import org.springframework.http.ResponseEntity;

public class RestResultBody extends ResponseEntity<RestResultFactory> {
    public RestResultBody(RestResultFactory body) {
        super(body, body.getStatus());
    }
}
