package com.insurance.aml.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException forField(String entityName, String field, Object value) {
        return new DuplicateResourceException(entityName + " already exists with " + field + ": " + value);
    }
}
