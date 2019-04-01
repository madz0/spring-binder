package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.PasswordsMatch;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, PasswordsMatchValidator.PasswordBean> {
    @Override
    public boolean isValid(PasswordBean passwordBean, ConstraintValidatorContext constraintValidatorContext) {
        return passwordBean.getPassword() != null &&
                passwordBean.getRepeatedPassword() != null &&
                passwordBean.getPassword().equals(passwordBean.getRepeatedPassword());
    }

    public interface PasswordBean {
        String getPassword();

        String getRepeatedPassword();
    }
}
