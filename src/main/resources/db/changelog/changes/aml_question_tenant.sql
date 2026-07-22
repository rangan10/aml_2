CREATE TABLE aml_question_tenant (

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    question_id BIGINT NOT NULL,

    tenant_id BIGINT NOT NULL,

    version_no INTEGER NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    effective_from TIMESTAMP,

    effective_to TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_aqt_question
        FOREIGN KEY (question_id)
        REFERENCES aml_question(question_id),

    CONSTRAINT fk_aqt_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(tenant_id),

    CONSTRAINT uk_aqt_tenant_question_version
        UNIQUE (tenant_id, question_id, version_no)
);

CREATE INDEX idx_aqt_tenant
    ON aml_question_tenant (tenant_id);

CREATE INDEX idx_aqt_question
    ON aml_question_tenant (question_id);

CREATE INDEX idx_aqt_tenant_question_version
    ON aml_question_tenant (
        tenant_id,
        question_id,
        version_no DESC
    );

CREATE INDEX idx_aqt_active
    ON aml_question_tenant (active);


INSERT INTO aml_question_tenant (
    question_id,
    tenant_id,
    version_no,
    active,
    created_at,
    updated_at
)
SELECT
    q.question_id,
    t.tenant_id,
    1,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM aml_question q
CROSS JOIN tenant t
WHERE t.tenant_code = 'KSHEMA'
  AND NOT EXISTS (
        SELECT 1
        FROM aml_question_tenant aqt
        WHERE aqt.question_id = q.question_id
          AND aqt.tenant_id = t.tenant_id
          AND aqt.version_no = 1
  );


CREATE TABLE aml_question_response (
    question_response_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_qr_user
        FOREIGN KEY (user_id)
        REFERENCES customer(customer_id),

    CONSTRAINT fk_qr_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(tenant_id),

    CONSTRAINT fk_qr_question
        FOREIGN KEY (question_id)
        REFERENCES aml_question(question_id)
);

CREATE INDEX idx_qr_user ON aml_question_response(user_id);
CREATE INDEX idx_qr_tenant ON aml_question_response(tenant_id);
CREATE INDEX idx_qr_question ON aml_question_response(question_id);
CREATE INDEX idx_qr_user_question ON aml_question_response(user_id, question_id);
CREATE INDEX idx_qr_tenant_user ON aml_question_response(tenant_id, user_id);


CREATE TABLE aml_question_option_response (
    option_response_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    response_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,

    CONSTRAINT fk_qor_response
        FOREIGN KEY (response_id)
        REFERENCES aml_question_response(question_response_id),

    CONSTRAINT fk_qor_option
        FOREIGN KEY (option_id)
        REFERENCES aml_question_option(option_id),

    CONSTRAINT uk_response_option
        UNIQUE (response_id, option_id)
);

CREATE INDEX idx_qor_response
    ON aml_question_option_response(response_id);

CREATE INDEX idx_qor_option
    ON aml_question_option_response(option_id);