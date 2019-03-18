package com.github.madz0.springbinder.validation.constraint;

import com.github.madz0.springbinder.utils.NetworkUtils;
import com.github.madz0.springbinder.validation.validator.IpAddressValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IpAddressValidator.class)
public @interface IpAddress {

    NetworkUtils.IpVersion[] versions() default {NetworkUtils.IpVersion.IPv4, NetworkUtils.IpVersion.IPv6};

    String message() default IpAddressValidator.message;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
