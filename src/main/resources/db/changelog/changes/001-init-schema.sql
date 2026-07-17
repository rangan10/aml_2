--liquibase formatted sql

--changeset rangan10:001-init-schema
-- =====================================================================
-- AML Module Schema - 001
-- =====================================================================

CREATE TABLE customer (
    customer_id         BIGSERIAL PRIMARY KEY,
    customer_code       VARCHAR(30)  NOT NULL UNIQUE,
    full_name           VARCHAR(200) NOT NULL,
    pan_number          VARCHAR(10)  NOT NULL,
    mobile_number       VARCHAR(15)  NOT NULL,
    email               VARCHAR(150) NOT NULL,
    address_current     VARCHAR(500) NOT NULL,
    address_permanent   VARCHAR(500) NOT NULL,
    annual_income       NUMERIC(18,2) NOT NULL,
    occupation_type     VARCHAR(30)  NOT NULL,
    is_pep              BOOLEAN NOT NULL DEFAULT FALSE,
    is_nri              BOOLEAN NOT NULL DEFAULT FALSE,
    risk_category       VARCHAR(10)  NOT NULL DEFAULT 'LOW',
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_customer_pan ON customer(pan_number);
CREATE INDEX idx_customer_mobile ON customer(mobile_number);
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_address_current ON customer(address_current);

CREATE TABLE policy (
    policy_id            BIGSERIAL PRIMARY KEY,
    policy_number        VARCHAR(30) NOT NULL UNIQUE,
    customer_id          BIGINT NOT NULL REFERENCES customer(customer_id),
    product_type         VARCHAR(50) NOT NULL,
    proposer_name        VARCHAR(200) NOT NULL,
    sum_insured          NUMERIC(18,2) NOT NULL,
    annual_premium       NUMERIC(18,2) NOT NULL,
    asset_hypothecated   BOOLEAN NOT NULL DEFAULT FALSE,
    policy_start_date    DATE NOT NULL,
    policy_status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_policy_customer ON policy(customer_id);

CREATE TABLE payment_transaction (
    payment_id      BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    payment_mode    VARCHAR(20) NOT NULL,
    amount          NUMERIC(18,2) NOT NULL,
    payment_date    TIMESTAMP NOT NULL,
    payer_name      VARCHAR(200) NOT NULL,
    payer_pan       VARCHAR(10),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_policy ON payment_transaction(policy_id);
CREATE INDEX idx_payment_date ON payment_transaction(payment_date);

CREATE TABLE claim_event (
    claim_id        BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    claim_amount    NUMERIC(18,2) NOT NULL,
    claim_date      DATE NOT NULL,
    claim_status    VARCHAR(20) NOT NULL DEFAULT 'LOGGED',
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_claim_policy ON claim_event(policy_id);

CREATE TABLE free_look_cancellation (
    cancellation_id     BIGSERIAL PRIMARY KEY,
    policy_id           BIGINT NOT NULL REFERENCES policy(policy_id),
    customer_id         BIGINT NOT NULL REFERENCES customer(customer_id),
    cancellation_date   DATE NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_flc_customer ON free_look_cancellation(customer_id);

CREATE TABLE refund_transaction (
    refund_id       BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    refund_amount   NUMERIC(18,2) NOT NULL,
    refund_date     DATE NOT NULL,
    refund_reason   VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_refund_policy ON refund_transaction(policy_id);

CREATE TABLE address_change_log (
    change_id       BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customer(customer_id),
    old_address     VARCHAR(500),
    new_address     VARCHAR(500) NOT NULL,
    change_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_addr_change_customer ON address_change_log(customer_id);

CREATE TABLE nominee_change_log (
    change_id       BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    change_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_nominee_change_policy ON nominee_change_log(policy_id);

CREATE TABLE assignee_change_log (
    change_id       BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    change_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_assignee_change_policy ON assignee_change_log(policy_id);

CREATE TABLE document_status (
    doc_status_id   BIGSERIAL PRIMARY KEY,
    policy_id       BIGINT NOT NULL REFERENCES policy(policy_id),
    missing_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    remarks         VARCHAR(500),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_doc_status_policy ON document_status(policy_id);

CREATE TABLE adverse_media_screening (
    screening_id    BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customer(customer_id),
    match_found     BOOLEAN NOT NULL DEFAULT FALSE,
    source          VARCHAR(100),
    remarks         VARCHAR(500),
    screening_date  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_adverse_media_customer ON adverse_media_screening(customer_id);

CREATE TABLE mca_screening_result (
    screening_id    BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customer(customer_id),
    match_found     BOOLEAN NOT NULL DEFAULT FALSE,
    remarks         VARCHAR(500),
    screening_date  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_mca_screening_customer ON mca_screening_result(customer_id);

-- ==================== AML Alert & Workflow ====================

CREATE TABLE aml_alert (
    alert_id        BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customer(customer_id),
    policy_id       BIGINT REFERENCES policy(policy_id),
    rule_code       VARCHAR(10) NOT NULL,
    description     VARCHAR(1000) NOT NULL,
    severity        VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    current_stage   VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_alert_customer ON aml_alert(customer_id);
CREATE INDEX idx_alert_rule_code ON aml_alert(rule_code);
CREATE INDEX idx_alert_status ON aml_alert(status);

CREATE TABLE aml_alert_action (
    action_id       BIGSERIAL PRIMARY KEY,
    alert_id        BIGINT NOT NULL REFERENCES aml_alert(alert_id),
    stage           VARCHAR(30) NOT NULL,
    action_by       VARCHAR(100) NOT NULL,
    action          VARCHAR(30) NOT NULL,
    remarks         VARCHAR(1000),
    action_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_alert_action_alert ON aml_alert_action(alert_id);
