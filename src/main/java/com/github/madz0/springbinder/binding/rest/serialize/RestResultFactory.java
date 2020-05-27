package com.github.madz0.springbinder.binding.rest.serialize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.model.Groups;
import com.github.madz0.springbinder.validation.ValidationError;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Getter
public class RestResultFactory<T> {

    private T result;
    @JsonIgnore
    @Getter(value = AccessLevel.NONE)
    public Class<? extends Groups.IGroup> group;
    private HttpStatus status;
    private List<ValidationError> errors;
    private String serviceErrorCause;
    private String errorMessage;

    public static <T> RestResult okay() {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.OK;
        return new RestResult(body);
    }

    public static <T> RestResult created() {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.CREATED;
        return new RestResult(body);
    }

    public static <T> RestResult okay(T result, Class<? extends Groups.IGroup> group) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.OK;
        body.result = result;
        BindingUtils.initialize(result, group);
        return new RestResult(body);
    }

    public static <T> RestResult error(List<ValidationError> errors) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.NOT_ACCEPTABLE;
        body.errors = errors;
        return new RestResult(body);
    }

    public static <T> RestResult serverError(String serviceErrorCause) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.INTERNAL_SERVER_ERROR;
        body.serviceErrorCause = serviceErrorCause;
        return new RestResult(body);
    }

    public static <T> RestResult notFound(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.NOT_FOUND;
        body.errorMessage = errorMessage;
        return new RestResult(body);
    }

    public static <T> RestResult unauthorized(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.FORBIDDEN;         //  403, user does not have access to this service
        body.errorMessage = errorMessage;
        return new RestResult(body);
    }

    public static <T> RestResult unauthenticated(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.UNAUTHORIZED;      //  401, user is not logged in
        body.errorMessage = errorMessage;
        return new RestResult(body);
    }

    public static <T> RestResult badRequest(String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.BAD_REQUEST;
        body.errorMessage = errorMessage;
        return new RestResult(body);
    }

    public static <T> RestResult badRequest(List<ValidationError> errors) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.BAD_REQUEST;
        body.errors = errors;
        return new RestResult(body);
    }

    public static <T> RestResult badRequest(BindingResult bindingResult) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = HttpStatus.BAD_REQUEST;
        body.errors = getValidationErrorsOf(bindingResult);
        return new RestResult(body);
    }

    public static <T> RestResult status(HttpStatus status, String errorMessage) {
        RestResultFactory<T> body = new RestResultFactory<>();
        body.status = status;
        body.errorMessage = errorMessage;
        return new RestResult(body);
    }

    private static List<ValidationError> getValidationErrorsOf(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream().map(
            e -> new ValidationError((e instanceof FieldError) ? ((FieldError) e).getField() : e.getObjectName(),
                e.getDefaultMessage(), (e instanceof FieldError) ? ((FieldError) e).getRejectedValue() : null))
            .collect(Collectors.toList());
    }
}
