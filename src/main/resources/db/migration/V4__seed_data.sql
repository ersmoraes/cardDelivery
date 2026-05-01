-- Regiões de atendimento

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('SP', '01000000', '05999999', 'BAIXO', 3, 'EXPRESS_CARD', TRUE, TRUE);

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('SP', '06000000', '19999999', 'BAIXO', 5, 'CORREIOS', TRUE, TRUE);

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('RJ', '20000000', '28999999', 'MEDIO', 4, 'TRANSPORTADORA_NORDESTE', TRUE, TRUE);

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('MG', '30000000', '39999999', 'BAIXO', 4, 'EXPRESS_CARD', TRUE, TRUE);

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('PR', '80000000', '87999999', 'BAIXO', 4, 'CORREIOS', TRUE, TRUE);

INSERT INTO regiao_atendimento (uf, cep_inicio, cep_fim, perfil_risco, prazo_dias, transportadora, permite_entrega, ativo)
VALUES ('AM', '69000000', '69999999', 'ALTO', 0, NULL, FALSE, TRUE);

INSERT INTO agencia (codigo, nome, uf, cidade, cep, ativo) VALUES
   ('AG-0001', 'Agência Paulista',         'SP', 'São Paulo',       '01310100', TRUE),
   ('AG-0002', 'Agência Vila Madalena',    'SP', 'São Paulo',       '05433010', TRUE),
   ('AG-0010', 'Agência Campinas Centro',  'SP', 'Campinas',        '13010100', TRUE),
   ('AG-0020', 'Agência RJ Centro',        'RJ', 'Rio de Janeiro',  '20040020', TRUE),
   ('AG-0030', 'Agência BH Savassi',       'MG', 'Belo Horizonte',  '30130170', TRUE),
   ('AG-0040', 'Agência Curitiba Centro',  'PR', 'Curitiba',        '80010010', TRUE),
   ('AG-0042', 'Agência Manaus Centro',    'AM', 'Manaus',          '69900000', TRUE),
   ('AG-0107', 'Agência Manaus Shopping',  'AM', 'Manaus',          '69050010', TRUE);
