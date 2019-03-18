package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madz0.springbinder.validation.ValidationError;
import lombok.AccessLevel;
import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class RestResultFactory<T> {

    private T result;
    @JsonIgnore
    @Getter(value = AccessLevel.NONE)
    public Class<? extends BaseGroups.IGroup> group;
    private HttpStatus status;
    private List<ValidationError> errors;
    private String serviceErrorCause;
    private String errorMessage;

    static <T> RestResultFactory<T> okay(){
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.OK;
        return body;
    }

    static <T> RestResultFactory<T> okay(T result, Class<? extends BaseGroups.IGroup> group){
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.OK;
        body.result = result;
        body.group = group;
        BindUtils.initialize(result, group);
        return body;
    }

    static <T> RestResultFactory<T> error(List<ValidationError> errors){
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.NOT_ACCEPTABLE;
        body.errors = errors;
        return body;
    }

    static <T> RestResultFactory<T> serverError(String serviceErrorCause) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.INTERNAL_SERVER_ERROR;
        body.serviceErrorCause = serviceErrorCause;
        return body;
    }

    static <T> RestResultFactory<T> notFound(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.NOT_FOUND;
        body.errorMessage = errorMessage;
        return body;
    }

    static <T> RestResultFactory<T> unauthorized(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.FORBIDDEN;         //  403, user does not have access to this service
        body.errorMessage = errorMessage;
        return body;
    }

    static <T> RestResultFactory<T> unauthenticated(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.UNAUTHORIZED;      //  401, user is not logged in
        body.errorMessage = errorMessage;
        return body;
    }

    static <T> RestResultFactory<T> badRequest(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.BAD_REQUEST;
        body.errorMessage = errorMessage;
        return body;
    }

    static <T> RestResultFactory<T> status(HttpStatus status, String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = status;
        body.errorMessage = errorMessage;
        return body;
    }
}
