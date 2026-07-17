--liquibase formatted sql

--changeset rangan10:003-seed-aml-questions
-- =====================================================================
-- AML Module Schema - 003
-- Seed the global question bank with EDD/KYC questions that capture the
-- declarative information behind each AML rule's red flag. Every
-- question_code is prefixed with the rule(s) it supports for traceability.
-- All questions are GLOBAL (tenant_id NULL) so every tenant can use them.
-- =====================================================================

-- ==================== AML003: address mismatch / income vs sum insured & premium ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML003_SOURCE_OF_FUNDS', 'What is the primary source of funds used to pay the premium for this policy?', 'SINGLE_CHOICE', 'GLOBAL'),
('AML003_SUM_INSURED_REASON', 'Please explain why the sum insured and/or premium for this policy is significantly higher than your declared annual income.', 'TEXT', 'GLOBAL'),
('AML003_ADDRESS_MISMATCH_REASON', 'Your current residential address differs from your permanent address. Please provide the reason.', 'TEXT', 'GLOBAL');

INSERT INTO aml_question_option (question_id, option_code, option_label, display_order)
SELECT question_id, v.option_code, v.option_label, v.display_order
FROM aml_question,
     (VALUES ('SALARY', 'Salary', 1),
             ('BUSINESS_INCOME', 'Business Income', 2),
             ('SAVINGS', 'Savings', 3),
             ('INHERITANCE', 'Inheritance', 4),
             ('SALE_OF_ASSET', 'Sale of Asset', 5),
             ('LOAN', 'Loan', 6),
             ('GIFT', 'Gift', 7),
             ('OTHER', 'Other', 8)
     ) AS v(option_code, option_label, display_order)
WHERE question_code = 'AML003_SOURCE_OF_FUNDS';

-- ==================== AML004: cash/DD payment thresholds ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML004_PAYMENT_MODE', 'What is your preferred mode of premium payment?', 'SINGLE_CHOICE', 'GLOBAL'),
('AML004_CASH_DD_REASON', 'If paying by cash or demand draft, please explain the reason for not using a bank transfer, cheque, or online payment.', 'TEXT', 'GLOBAL');

INSERT INTO aml_question_option (question_id, option_code, option_label, display_order)
SELECT question_id, v.option_code, v.option_label, v.display_order
FROM aml_question,
     (VALUES ('CASH', 'Cash', 1),
             ('DD', 'Demand Draft', 2),
             ('CHEQUE', 'Cheque', 3),
             ('NEFT_RTGS', 'NEFT / RTGS', 4),
             ('UPI', 'UPI', 5),
             ('CARD', 'Card', 6),
             ('ONLINE_BANKING', 'Online Banking', 7)
     ) AS v(option_code, option_label, display_order)
WHERE question_code = 'AML004_PAYMENT_MODE';

-- ==================== AML005: repeated free-look cancellations ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML005_FREELOOK_HISTORY', 'Have you cancelled any life/health insurance policy within the free-look period in the last 12 months?', 'BOOLEAN', 'GLOBAL'),
('AML005_FREELOOK_REASON', 'If yes, please state the reason for the free-look cancellation(s).', 'TEXT', 'GLOBAL');

-- ==================== AML006 / AML009: high premium with refund, overpayment refunds ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML006_REFUND_HISTORY', 'Have you requested a premium refund or overpayment refund on any policy in the last 12 months?', 'BOOLEAN', 'GLOBAL'),
('AML006_REFUND_DETAILS', 'If yes, please provide details of the refund request (policy number, amount, and reason).', 'TEXT', 'GLOBAL');

-- ==================== AML007: claim on high sum insured / high premium policy ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML007_CLAIM_HISTORY', 'Have you filed any insurance claim in the last 12 months?', 'BOOLEAN', 'GLOBAL'),
('AML007_CLAIM_DETAILS', 'If yes, please provide the claim details (policy number, amount, and reason for the claim).', 'TEXT', 'GLOBAL');

-- ==================== AML008: repeated address changes ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML008_ADDRESS_CHANGE_REASON', 'Have you changed your registered address more than once in the last year? If yes, please state the reason.', 'TEXT', 'GLOBAL');

-- ==================== AML010: duplicate identity attributes ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML010_DUPLICATE_IDENTITY', 'Do you, or any immediate family member, hold another insurance policy or customer record under a different name, mobile number, or email address?', 'BOOLEAN', 'GLOBAL');

-- ==================== AML011: adverse media ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML011_ADVERSE_MEDIA_DECLARATION', 'Are you, or any of your immediate family members, currently subject to any regulatory investigation, litigation, or adverse media coverage?', 'BOOLEAN', 'GLOBAL');

-- ==================== AML015 / AML016: first-year claim on non-hypothecated asset ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML015_ASSET_HYPOTHECATION', 'Is the insured asset under this policy mortgaged or hypothecated to a bank or financial institution?', 'BOOLEAN', 'GLOBAL');

-- ==================== AML017: missing documents, nominee/assignee churn ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML017_DOCUMENTS_COMPLETE', 'Have all KYC documents (identity proof, address proof, income proof) been submitted?', 'BOOLEAN', 'GLOBAL'),
('AML017_NOMINEE_CHANGE_REASON', 'Have you changed the nominee on this policy more than twice? If yes, please explain the reason.', 'TEXT', 'GLOBAL'),
('AML017_ASSIGNEE_CHANGE_REASON', 'Have you changed the policy assignee more than twice? If yes, please explain the reason.', 'TEXT', 'GLOBAL');

-- ==================== AML018: premium paid by third party ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML018_PREMIUM_PAYER', 'Who is making the premium payment for this policy?', 'SINGLE_CHOICE', 'GLOBAL'),
('AML018_THIRD_PARTY_PAYER_REASON', 'If the premium payer is not the proposer or the insured, please explain the relationship and the reason for the third-party payment.', 'TEXT', 'GLOBAL');

INSERT INTO aml_question_option (question_id, option_code, option_label, display_order)
SELECT question_id, v.option_code, v.option_label, v.display_order
FROM aml_question,
     (VALUES ('SELF', 'Self', 1),
             ('SPOUSE', 'Spouse', 2),
             ('PARENT', 'Parent', 3),
             ('CHILD', 'Child', 4),
             ('EMPLOYER', 'Employer', 5),
             ('FRIEND', 'Friend', 6),
             ('OTHER', 'Other', 7)
     ) AS v(option_code, option_label, display_order)
WHERE question_code = 'AML018_PREMIUM_PAYER';

-- ==================== AML019: MCA screening match ====================

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML019_MCA_DECLARATION', 'Are you, or any business entity you are associated with as a director, promoter, or partner, currently listed under any Ministry of Corporate Affairs (MCA) regulatory action, disqualification, or default list?', 'BOOLEAN', 'GLOBAL');

-- ==================== Risk categorization inputs (PEP / NRI / purpose) ====================
-- Not tied to a single rule code; these drive RiskCategorizationService and
-- several rules above (e.g. address/PEP declarations feed manual review).

INSERT INTO aml_question (question_code, question_text, question_type, question_scope) VALUES
('AML_PEP_SELF_DECLARATION', 'Are you a Politically Exposed Person (PEP)?', 'BOOLEAN', 'GLOBAL'),
('AML_PEP_FAMILY_DECLARATION', 'Is any of your immediate family members a Politically Exposed Person (PEP)?', 'BOOLEAN', 'GLOBAL'),
('AML_NRI_DECLARATION', 'Are you a Non-Resident Indian (NRI)?', 'BOOLEAN', 'GLOBAL'),
('AML_POLICY_PURPOSE', 'What is the purpose of purchasing this insurance policy?', 'SINGLE_CHOICE', 'GLOBAL');

INSERT INTO aml_question_option (question_id, option_code, option_label, display_order)
SELECT question_id, v.option_code, v.option_label, v.display_order
FROM aml_question,
     (VALUES ('PROTECTION', 'Protection', 1),
             ('SAVINGS_INVESTMENT', 'Savings / Investment', 2),
             ('TAX_PLANNING', 'Tax Planning', 3),
             ('RETIREMENT_PLANNING', 'Retirement Planning', 4),
             ('CHILD_EDUCATION', 'Child Education / Future Planning', 5),
             ('OTHER', 'Other', 6)
     ) AS v(option_code, option_label, display_order)
WHERE question_code = 'AML_POLICY_PURPOSE';
