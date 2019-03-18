/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package com.github.madz0.springbinder.validation;

import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A form validation error.
 */
@NoArgsConstructor
public class ValidationError {

    /**
     * The error key.
     */
    public String key;
    /**
     * The error messageToken.
     */
    public String messageToken;
    /**
     * The error arguments.
     */
    public List<String> arguments;

    /**
     * Constructs a new <code>ValidationError</code>.
     *
     * @param key the error key
     * @param messageToken the error messageToken
     * @param arguments the error messageToken arguments
     */
    public ValidationError(String key, String messageToken, List<Object> arguments) {
        this.key = key;
        if (arguments != null && arguments.size() != 0) {
            this.arguments = arguments.stream().map(Objects::toString).collect(Collectors.toList());
        }
        this.messageToken = messageToken;
    }

    /**
     * Constructs a new <code>ValidationError</code>.
     *
     * @param key the error key
     * @param messageToken the error messageToken
     * @param arguments the error messageToken arguments
     */
    public ValidationError(String key, String messageToken, Object... arguments) {
        this(key, messageToken, Arrays.asList(arguments));
    }

}