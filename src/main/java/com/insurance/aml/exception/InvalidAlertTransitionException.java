package com.insurance.aml.exception;

/**
 * Thrown when an action is attempted on an AmlAlert that is not valid
 * for its current workflow stage (e.g. trying to report to FIU from
 * the Operations Team stage).
 */
public class InvalidAlertTransitionException extends RuntimeException {

    public InvalidAlertTransitionException(String message) {
        super(message);
    }
}
