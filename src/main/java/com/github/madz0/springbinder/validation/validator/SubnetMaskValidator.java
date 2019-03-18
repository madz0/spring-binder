package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.utils.NetworkUtils;
import com.github.madz0.springbinder.validation.constraint.SubnetMask;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SubnetMaskValidator implements ConstraintValidator<SubnetMask, String> {
    final static public String message = "validation.error.subnetMask";

    @Override
    public void initialize(SubnetMask annotation){
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isBlank(value) || NetworkUtils.isValidMask(value);
    }
}
