--liquibase formatted sql

--changeset rangan10:003-seed-aml-questions
-- =====================================================================
-- AML Module Schema - 003
-- Seed the global question bank with EDD/KYC questions that capture the
-- declarative information behind each AML rule's red flag. Every
-- question_code is prefixed with the rule(s) it supports for traceability.
-- All questions are GLOBAL (tenant_id NULL) so every tenant can use them.
-- =====================================================================
INSERT INTO aml_question (
    question_code,
    question_text,
    question_type,
    question_scope,
    question_category,
    active,
    created_at,
    updated_at,
    version
)
VALUES
('AML003_SUM_INSURED_REASON',
 'Please explain why the sum insured and/or premium for this policy is significantly higher than your declared annual income.',
 'TEXT', 'GLOBAL', 'QUOTATION', TRUE, NOW(), NOW(), 1.0),

('AML003_ADDRESS_MISMATCH_REASON',
 'Your current residential address differs from your permanent address. Please provide the reason.',
 'TEXT', 'GLOBAL', 'KYC', TRUE, NOW(), NOW(), 1.0),

('AML005_FREELOOK_HISTORY',
 'Have you cancelled any life/health insurance policy within the free-look period in the last 12 months?',
 'BOOLEAN', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML005_FREELOOK_REASON',
 'If yes, please state the reason for the free-look cancellation(s).',
 'TEXT', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML006_REFUND_HISTORY',
 'Have you requested a premium refund or overpayment refund on any policy in the last 12 months?',
 'BOOLEAN', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML006_REFUND_DETAILS',
 'If yes, please provide details of the refund request (policy number, amount, and reason).',
 'TEXT', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML007_CLAIM_HISTORY',
 'Have you filed any insurance claim in the last 12 months?',
 'BOOLEAN', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML007_CLAIM_DETAILS',
 'If yes, please provide the claim details (policy number, amount, and reason for the claim).',
 'TEXT', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML008_ADDRESS_CHANGE_REASON',
 'Have you changed your registered address more than once in the last year? If yes, please state the reason.',
 'TEXT', 'GLOBAL', 'KYC', TRUE, NOW(), NOW(), 1.0),

('AML010_DUPLICATE_IDENTITY',
 'Do you, or any immediate family member, hold another insurance policy or customer record under a different name, mobile number, or email address?',
 'BOOLEAN', 'GLOBAL', 'KYC', TRUE, NOW(), NOW(), 1.0),

('AML011_ADVERSE_MEDIA_DECLARATION',
 'Are you, or any of your immediate family members, currently subject to any regulatory investigation, litigation, or adverse media coverage?',
 'BOOLEAN', 'GLOBAL', 'KYC', TRUE, NOW(), NOW(), 1.0),

('AML015_ASSET_HYPOTHECATION',
 'Is the insured asset under this policy mortgaged or hypothecated to a bank or financial institution?',
 'BOOLEAN', 'GLOBAL', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('AML019_MCA_DECLARATION',
 'Are you, or any business entity you are associated with as a director, promoter, or partner, currently listed under any Ministry of Corporate Affairs (MCA) regulatory action, disqualification, or default list?',
 'BOOLEAN', 'GLOBAL', 'KYC', TRUE, NOW(), NOW(), 1.0),

('AML_POLICY_PURPOSE',
 'What is the purpose of purchasing this insurance policy?',
 'SINGLE_CHOICE', 'GLOBAL', 'QUOTATION', TRUE, NOW(), NOW(), 1.0),

('AML003_SOURCE_OF_FUNDS',
 'What is the primary source of funds used to pay the premium for this policy?',
 'SINGLE_CHOICE', 'GLOBAL', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('AML004_PAYMENT_MODE',
 'What is your preferred mode of premium payment?',
 'SINGLE_CHOICE', 'GLOBAL', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('AML004_CASH_DD_REASON',
 'If paying by cash or demand draft, please explain the reason for not using a bank transfer, cheque, or online payment.',
 'TEXT', 'GLOBAL', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('AML017_DOCUMENTS_COMPLETE',
 'Have all KYC documents (identity proof, address proof, income proof) been submitted?',
 'BOOLEAN', 'GLOBAL', 'DOCUMENT', TRUE, NOW(), NOW(), 1.0),

('AML017_NOMINEE_CHANGE_REASON',
 'Have you changed the nominee on this policy more than twice? If yes, please explain the reason.',
 'TEXT', 'GLOBAL', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('AML017_ASSIGNEE_CHANGE_REASON',
 'Have you changed the policy assignee more than twice? If yes, please explain the reason.',
 'TEXT', 'GLOBAL', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('HYPOTHECATION_STATUS',
 'What is the hypothecation status of the insured asset?',
 'SINGLE_CHOICE', 'TENANT_SPECIFIC', 'POLICY', TRUE, NOW(), NOW(), 1.0),

('SUM_INSURED',
 'What is the sum insured for this policy?',
 'NUMBER', 'TENANT_SPECIFIC', 'QUOTATION', TRUE, NOW(), NOW(), 1.0),

('PREMIUM_AMOUNT',
 'What is the premium amount for this policy?',
 'NUMBER', 'TENANT_SPECIFIC', 'QUOTATION', TRUE, NOW(), NOW(), 1.0),

('EMPLOYMENT_TYPE',
 'What is your employment type?',
 'SINGLE_CHOICE', 'TENANT_SPECIFIC', 'EMP', TRUE, NOW(), NOW(), 1.0),

('AML018_PREMIUM_PAYER',
 'Who is making the premium payment for this policy?',
 'SINGLE_CHOICE', 'GLOBAL', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('AML018_THIRD_PARTY_PAYER_REASON',
 'If the premium payer is not the proposer or the insured, please explain the relationship and the reason for the third-party payment.',
 'TEXT', 'GLOBAL', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('AML_PEP_SELF_DECLARATION',
 'Are you a Politically Exposed Person (PEP)?',
 'BOOLEAN', 'GLOBAL', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('AML_PEP_FAMILY_DECLARATION',
 'Is any of your immediate family members a Politically Exposed Person (PEP)?',
 'BOOLEAN', 'GLOBAL', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('AML_NRI_DECLARATION',
 'Are you a Non-Resident Indian (NRI)?',
 'BOOLEAN', 'GLOBAL', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('CKYC_KIN_NUMBER',
 'What is your CKYC KIN number?',
 'TEXT', 'TENANT_SPECIFIC', 'DOCUMENT', TRUE, NOW(), NOW(), 1.0),

('BENEFICIAL_OWNER_FLAG',
 'Is there a beneficial owner other than the insured/proposer?',
 'BOOLEAN', 'TENANT_SPECIFIC', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('NRI_FLAG',
 'Are you a Non-Resident Indian (NRI)?',
 'BOOLEAN', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('NOMINEE_RELATIONSHIP',
 'What is the nominee''s relationship to the insured?',
 'TEXT', 'TENANT_SPECIFIC', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('PAYER_NAME',
 'What is the payer''s name?',
 'TEXT', 'TENANT_SPECIFIC', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('PAN_NUMBER',
 'What is your PAN number?',
 'TEXT', 'TENANT_SPECIFIC', 'DOCUMENT', TRUE, NOW(), NOW(), 1.0),

('RELATED_TO_PEP',
 'Are you related to a Politically Exposed Person (PEP)?',
 'BOOLEAN', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('SOURCE_OF_INCOME',
 'What is your primary source of income?',
 'SINGLE_CHOICE', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('PAYMENT_MODE',
 'What is the mode of premium payment?',
 'SINGLE_CHOICE', 'TENANT_SPECIFIC', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('PREMIUM_PAYER_TYPE',
 'Who is paying the premium for this policy?',
 'SINGLE_CHOICE', 'TENANT_SPECIFIC', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('PAYER_PAN',
 'What is the payer''s PAN number?',
 'TEXT', 'TENANT_SPECIFIC', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('RELATIONSHIP_TO_INSURED',
 'What is the payer''s relationship to the insured?',
 'TEXT', 'TENANT_SPECIFIC', 'PAYMENT', TRUE, NOW(), NOW(), 1.0),

('ULTIMATE_BENEFICIAL_OWNER_NAME',
 'What is the name of the Ultimate Beneficial Owner (UBO)?',
 'TEXT', 'TENANT_SPECIFIC', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('ASSIGNEE_EXISTS',
 'Does this policy have an assignee?',
 'BOOLEAN', 'TENANT_SPECIFIC', 'NOMINEE', TRUE, NOW(), NOW(), 1.0),

('EMAIL_ADDRESS',
 'What is your email address?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('FOREIGN_COUNTRY',
 'Which foreign country is the source of funds from?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('NGO_TRUST_AFFILIATION',
 'Do you have any NGO or Trust affiliation?',
 'BOOLEAN', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('COUNTRY_OF_RESIDENCE',
 'What is your country of residence?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('CURRENT_ADDRESS',
 'What is your current address?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('MOBILE_NUMBER',
 'What is your mobile number?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('PERMANENT_ADDRESS',
 'What is your permanent address?',
 'TEXT', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0),

('ANNUAL_INCOME',
 'What is your annual income?',
 'NUMBER', 'TENANT_SPECIFIC', 'PROFILE', TRUE, NOW(), NOW(), 1.0);