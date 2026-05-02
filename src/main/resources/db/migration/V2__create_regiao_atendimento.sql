CREATE TABLE regiao_atendimento (
    id              BIGSERIAL    PRIMARY KEY,
    uf              VARCHAR(2)   NOT NULL,
    cep_inicio      VARCHAR(8)   NOT NULL,
    cep_fim         VARCHAR(8)   NOT NULL,
    perfil_risco    VARCHAR(10)  NOT NULL,
    prazo_dias      INTEGER      NOT NULL,
    transportadora  VARCHAR(50),
    permite_entrega BOOLEAN      NOT NULL DEFAULT TRUE,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_cep_range    CHECK (cep_fim >= cep_inicio),
    CONSTRAINT chk_perfil_risco CHECK (perfil_risco IN ('BAIXO', 'MEDIO', 'ALTO'))
);

CREATE INDEX idx_regiao_cep ON regiao_atendimento(cep_inicio, cep_fim);
