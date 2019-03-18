package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.utils.NetworkUtils;
import com.github.madz0.springbinder.validation.constraint.IpAddress;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Created by MAZMAZ on 6/20/2015.
 */
public class IpAddressValidator implements ConstraintValidator<IpAddress, String> {
    final static public String message = "validation.error.ipAddress";
    private IpAddress annotation;

    @Override
    public void initialize(IpAddress annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isBlank(value) ||
                (NetworkUtils.isValid(value) &&
                        Arrays.asList(annotation.versions()).contains(NetworkUtils.guessVersion(value)));
    }
}
