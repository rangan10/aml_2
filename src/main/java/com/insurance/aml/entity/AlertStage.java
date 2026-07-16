package com.insurance.aml.entity;

/**
 * Represents the current stage in the AML alert workflow:
 * System -> Operations Team -> Operations HOD -> Compliance Officer -> FIU / Close
 */
public enum AlertStage {
    SYSTEM,
    OPERATIONS_TEAM,
    OPERATIONS_HOD,
    COMPLIANCE_OFFICER,
    FIU_REPORTED,
    CLOSED
}
