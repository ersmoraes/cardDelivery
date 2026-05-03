-- Hibernate 6 envia String como character varying; text aceita sem cast explicito
ALTER TABLE auditoria_consulta ALTER COLUMN resposta_api TYPE text;

-- Permite gravar consultas de agencias (sem customer/card context)
ALTER TABLE auditoria_consulta ALTER COLUMN customer_id DROP NOT NULL;
ALTER TABLE auditoria_consulta ALTER COLUMN card_type   DROP NOT NULL;
