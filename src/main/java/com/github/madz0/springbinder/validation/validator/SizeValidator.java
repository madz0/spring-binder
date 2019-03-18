package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.validation.constraint.Size;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

/**
 * Created by Javadi on 5/7/2016.
 */
public class SizeValidator implements ConstraintValidator<Size, Collection> {

    final static public String message = "validation.constraint.size";
    private int min;
    private int max;

    @Override
    public void initialize(Size constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Collection collection, ConstraintValidatorContext context) {
        if (collection == null || collection.size() < min || collection.size() > max) {
            return false;
        }
        return collection.size() >= min && collection.size() <= max;
    }
}
