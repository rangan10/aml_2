
INSERT INTO aml_db.aml_question_tenant (
    question_id,
    tenant_id,
    version_no,
    active,
    effective_from,
    effective_to
)
SELECT
    q.question_id,
    t.tenant_id,
    1,
    TRUE,
    NOW(),
    NOW()
FROM aml_db.aml_question q
CROSS JOIN aml_db.tenant t
WHERE q.question_scope = 'TENANT_SPECIFIC'
  AND t.tenant_code = 'KSHEMA'
  AND NOT EXISTS (
      SELECT 1
      FROM aml_db.aml_question_tenant aqt
      WHERE aqt.question_id = q.question_id
        AND aqt.tenant_id = t.tenant_id
        AND aqt.version_no = 1
  );
