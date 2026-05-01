CREATE TABLE agencia (
     id      BIGSERIAL    PRIMARY KEY,
     codigo  VARCHAR(10)  NOT NULL UNIQUE,
     nome    VARCHAR(100) NOT NULL,
     uf      CHAR(2)      NOT NULL,
     cidade  VARCHAR(100) NOT NULL,
     cep     VARCHAR(8)   NOT NULL,
     ativo   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_agencia_uf_cidade ON agencia(uf, cidade);
