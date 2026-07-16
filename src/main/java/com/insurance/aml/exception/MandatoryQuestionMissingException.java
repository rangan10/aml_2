package com.insurance.aml.exception;

public class MandatoryQuestionMissingException extends RuntimeException {

    public MandatoryQuestionMissingException(String message) {
        super(message);
    }
}
