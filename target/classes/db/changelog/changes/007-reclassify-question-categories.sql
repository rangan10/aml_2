--liquibase formatted sql

--changeset rangan10:007-reclassify-question-categories
-- =====================================================================
-- AML Module Schema - 007
-- 006 introduced question_category but only had KYC / EMP / POLICY /
-- QUOTATION to work with, so several questions were shoehorned into the
-- closest fit. Adds four more specific categories and moves the
-- questions that fit them better:
--   PROFILE  - personal/financial background (address, income, PEP/NRI
--              declarations, foreign funds)
--   DOCUMENT - KYC document artifacts (PAN, CKYC, document completeness)
--   NOMINEE  - nominee/assignee/beneficial-owner details
--   PAYMENT  - premium payer/payment-mode details
-- =====================================================================

UPDATE aml_question q
SET question_category = v.category
FROM (VALUES
        -- PROFILE
        ('COUNTRY_OF_RESIDENCE',             'PROFILE'),
        ('NRI_FLAG',                         'PROFILE'),
        ('PEP_FLAG',                         'PROFILE'),
        ('RELATED_TO_PEP',                   'PROFILE'),
        ('NGO_TRUST_AFFILIATION',            'PROFILE'),
        ('ANNUAL_INCOME',                    'PROFILE'),
        ('SOURCE_OF_INCOME',                 'PROFILE'),
        ('CURRENT_ADDRESS',                  'PROFILE'),
        ('PERMANENT_ADDRESS',                'PROFILE'),
        ('MOBILE_NUMBER',                    'PROFILE'),
        ('EMAIL_ADDRESS',                    'PROFILE'),
        ('FOREIGN_SOURCE_OF_FUNDS',          'PROFILE'),
        ('FOREIGN_COUNTRY',                  'PROFILE'),
        ('AML003_SOURCE_OF_FUNDS',           'PROFILE'),
        ('AML_PEP_SELF_DECLARATION',         'PROFILE'),
        ('AML_PEP_FAMILY_DECLARATION',       'PROFILE'),
        ('AML_NRI_DECLARATION',              'PROFILE'),
        -- DOCUMENT
        ('PAN_NUMBER',                       'DOCUMENT'),
        ('CKYC_AVAILABLE',                   'DOCUMENT'),
        ('CKYC_KIN_NUMBER',                  'DOCUMENT'),
        ('AML017_DOCUMENTS_COMPLETE',        'DOCUMENT'),
        -- NOMINEE
        ('NOMINEE_NAME',                     'NOMINEE'),
        ('NOMINEE_RELATIONSHIP',             'NOMINEE'),
        ('ASSIGNEE_EXISTS',                  'NOMINEE'),
        ('BENEFICIAL_OWNER_FLAG',            'NOMINEE'),
        ('ULTIMATE_BENEFICIAL_OWNER_NAME',   'NOMINEE'),
        ('AML017_NOMINEE_CHANGE_REASON',     'NOMINEE'),
        ('AML017_ASSIGNEE_CHANGE_REASON',    'NOMINEE'),
        -- PAYMENT
        ('PAYMENT_MODE',                     'PAYMENT'),
        ('PREMIUM_PAYER_TYPE',               'PAYMENT'),
        ('PAYER_NAME',                       'PAYMENT'),
        ('PAYER_PAN',                        'PAYMENT'),
        ('RELATIONSHIP_TO_INSURED',          'PAYMENT'),
        ('AML004_PAYMENT_MODE',              'PAYMENT'),
        ('AML004_CASH_DD_REASON',            'PAYMENT'),
        ('AML018_PREMIUM_PAYER',             'PAYMENT'),
        ('AML018_THIRD_PARTY_PAYER_REASON',  'PAYMENT')
     ) AS v(question_code, category)
WHERE q.question_code = v.question_code;
