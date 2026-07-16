package com.insurance.aml.entity;

/**
 * Action taken at a given stage of the AML alert workflow.
 */
public enum AlertAction {
    FORWARD,
    ESCALATE,
    CLOSE,
    REPORT_TO_FIU,
    REQUEST_INFO
}
