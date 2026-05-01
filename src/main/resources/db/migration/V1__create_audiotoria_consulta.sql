CREATE TABLE auditoria_consulta (
    id                  BIGSERIAL PRIMARY KEY,
    event_id            VARCHAR(36)  NOT NULL UNIQUE,
    cep_consultado      VARCHAR(9)   NOT NULL,
    customer_id         VARCHAR(20)  NOT NULL,
    card_type           VARCHAR(20)  NOT NULL,
    data_hora           TIMESTAMP    NOT NULL,
    resposta_api        JSONB,
    decisao             VARCHAR(30)  NOT NULL,
    modalidade          VARCHAR(30),
    prazo_dias          INTEGER,
    risco_regiao        VARCHAR(10),
    status              VARCHAR(20)  NOT NULL,
    tempo_resposta_ms   BIGINT
);

CREATE INDEX idx_aud_cep      ON auditoria_consulta(cep_consultado);
CREATE INDEX idx_aud_customer ON auditoria_consulta(customer_id);
CREATE INDEX idx_aud_data     ON auditoria_consulta(data_hora DESC);
CREATE UNIQUE INDEX idx_aud_event_id ON auditoria_consulta(event_id);
