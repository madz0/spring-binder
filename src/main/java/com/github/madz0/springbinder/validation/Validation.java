package com.github.madz0.springbinder.validation;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Validation helpers.
 */
public class Validation {
    
    /**
     * The underlying JSR-303 validator.
     */
    private final static ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
    /**
     * A JSR-303 Validator.
     */
    public static final Validator validator=factory.getValidator();

}