# CardDelivery Hub

API REST para avaliação inteligente de entrega de cartões bancários. O sistema determina se um cartão pode ser entregue no endereço do cliente ou se deve ser retirado em agência, levando em conta o CEP, o tipo de cartão e o perfil de risco da região.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar o projeto](#como-rodar-o-projeto)
- [Serviços e portas](#serviços-e-portas)
- [Realizando requisições via Postman](#realizando-requisições-via-postman)
- [Acompanhando os logs de consulta no PgAdmin](#acompanhando-os-logs-de-consulta-no-pgadmin)
- [Endpoints da API](#endpoints-da-api)
- [Fluxo de mensageria (RabbitMQ)](#fluxo-de-mensageria-rabbitmq)
- [Validando o backup no S3 (LocalStack)](#validando-o-backup-no-s3-localstack)
- [Como rodar os testes e enviar ao SonarQube](#como-rodar-os-testes-e-enviar-ao-sonarqube)
- [Comandos úteis](#comandos-úteis)

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5 |
| Banco de dados | PostgreSQL 15 + Flyway |
| Mensageria | RabbitMQ 3.13 |
| Cache | Caffeine (in-memory) |
| Resiliência | Resilience4j — Circuit Breaker + Retry |
| Armazenamento | AWS S3 SDK v2 (LocalStack em dev) |
| CEP | ViaCEP (WireMock em dev) |
| Qualidade | SonarQube + JaCoCo (cobertura mínima 80%) |
| Containers | Docker + Docker Compose |

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e **rodando**
- Java 17+ — necessário apenas para rodar testes ou a aplicação fora do Docker
- Maven 3.9+ — ou utilize o wrapper `.\mvnw.cmd` (Windows) / `./mvnw` (Linux/macOS) já incluído no projeto

---

## Como rodar o projeto

### 1. Suba todos os containers

```bash
docker compose up -d
```

Esse comando sobe toda a stack: aplicação, banco de dados, RabbitMQ, WireMock, LocalStack, PgAdmin e SonarQube.

Na primeira execução, o Docker irá construir a imagem da aplicação automaticamente. Isso pode levar alguns minutos.

### 2. Aguarde a aplicação inicializar

Acompanhe os logs da aplicação:

```bash
docker compose logs -f app
```

Quando estiver pronta, você verá algo como `Started CarddeliveryApplication`. Você também pode verificar pelo health check:

```bash
curl http://localhost:8082/actuator/health
# Resposta esperada: {"status":"UP"}
```

### 3. Realize as requisições via Postman

Com os containers no ar, importe a collection e dispare as requisições. Veja a seção [Realizando requisições via Postman](#realizando-requisições-via-postman) para mais detalhes.

### 4. Acompanhe os logs de consulta no PgAdmin

Acesse http://localhost:5050 para visualizar os registros de auditoria gravados no banco após cada requisição. Veja a seção [Acompanhando os logs de consulta no PgAdmin](#acompanhando-os-logs-de-consulta-no-pgadmin).

---

## Serviços e portas

| Serviço | URL | Credenciais |
|---|---|---|
| **API** | http://localhost:8082 | — |
| **Health Check** | http://localhost:8082/actuator/health | — |
| **RabbitMQ Console** | http://localhost:15672 | guest / guest |
| **PgAdmin** | http://localhost:5050 | admin@carddelivery.com / admin |
| **WireMock (CEP mock)** | http://localhost:8081/__admin/ | — |
| **LocalStack (S3 mock)** | http://localhost:4566 | — |
| **SonarQube** | http://localhost:9000 | admin / admin |
| **PostgreSQL** | localhost:5432 | usuário: app / senha: app / banco: carddelivery |

---

## Realizando requisições via Postman

### Importando a collection

1. Abra o Postman e clique em **Import**
2. Selecione o arquivo `CardDelivery_postman_collection.json` na raiz do projeto
3. A base URL já está configurada como `http://localhost:8082`

### Grupos de requisições disponíveis

| Grupo | Qtd | O que cobre |
|---|---|---|
| Avaliação de Entrega | 13 | Todos os tipos de cartão, perfis de risco, CEPs inválidos, falhas externas |
| Agências Próximas | 6 | Busca por CEP, formatação, resultado vazio |
| Regiões de Atendimento | 8 | Paginação, cadastro, validações |
| Auditoria de Consultas | 7 | Listagem, filtros por cliente e data, busca por CEP |
| Health & Actuator | 3 | Health, info, metrics |
| WireMock Stubs | 6 | Verificação de mocks do CEP |

### Exemplo principal — Avaliação de entrega

```
POST http://localhost:8082/api/cards/delivery/evaluate
```

```json
// Request Body
{
  "cep": "01310100",
  "cardType": "CREDIT",
  "customerId": "C123456"
}
```

```json
// Response 200
{
  "decisao": "APROVADO",
  "modalidade": "ENTREGA_DOMICILIAR",
  "prazoDiasUteis": 3,
  "endereco": {
    "logradouro": "Avenida Paulista",
    "bairro": "Bela Vista",
    "cidade": "São Paulo",
    "uf": "SP"
  }
}
```

Tipos de cartão aceitos: `CREDIT` `DEBIT` `MULTIPLE` `BLACK`

---

## Acompanhando os logs de consulta no PgAdmin

Após disparar requisições pelo Postman, cada operação é registrada automaticamente na tabela `auditoria_consulta`.

**Passo a passo:**

1. Acesse http://localhost:5050
2. Faça login com `admin@carddelivery.com` / `admin`
3. O servidor **CardDelivery PostgreSQL** já aparece na sidebar (pré-configurado via `servers.json`)
4. Clique no servidor → informe a senha `app` quando solicitado
5. Navegue em: `Databases → carddelivery → Schemas → public → Tables`
6. Clique com botão direito em `auditoria_consulta` → **View/Edit Data → All Rows**

Cada linha registra:

| Campo | Descrição |
|---|---|
| `cep_consultado` | CEP da requisição |
| `customer_id` | Identificador do cliente (quando aplicável) |
| `card_type` | Tipo de cartão (quando aplicável) |
| `decisao` | Resultado da avaliação |
| `status` | `SUCESSO` ou `ERRO` |
| `tempo_resposta_ms` | Tempo de resposta em milissegundos |
| `data_hora` | Data e hora da consulta |
| `resposta_api` | JSON completo da resposta gerada |

---

## Endpoints da API

### Avaliação de Entrega

**`POST /api/cards/delivery/evaluate`** — Avalia se o cartão pode ser entregue no endereço ou deve ser retirado em agência.

### Agências Próximas

**`GET /api/cards/delivery/branches/nearby?cep={cep}`** — Retorna agências próximas ao CEP. Aceita CEP com ou sem hífen.

### Regiões de Atendimento

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/regions?page=0&size=10&sort=uf,asc` | Lista regiões ativas (paginado) |
| `POST` | `/api/regions` | Cadastra nova região |

```json
// POST /api/regions — Request Body
{
  "uf": "SP",
  "cepInicio": "01000000",
  "cepFim": "19999999",
  "perfilRisco": "BAIXO",
  "prazoDias": 3,
  "transportadora": "CORREIOS",
  "permiteEntrega": true,
  "ativo": true
}
```

Perfis de risco aceitos: `BAIXO` `MEDIO` `ALTO`

### Auditoria de Consultas

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/audit/queries` | Lista auditorias com filtros (paginado) |
| `GET` | `/api/audit/queries/{cep}` | Consultas por CEP específico |

Filtros para `GET /api/audit/queries`: `customerId`, `dataInicio`, `dataFim`, `page`, `size`

### Health & Monitoramento

| Endpoint | Descrição |
|---|---|
| `GET /actuator/health` | Status de saúde |
| `GET /actuator/info` | Informações da aplicação |
| `GET /actuator/metrics` | Métricas detalhadas |

---

## Fluxo de mensageria (RabbitMQ)

Toda avaliação de entrega publica automaticamente um evento na fila:

```
POST /api/cards/delivery/evaluate
          ↓
  Avaliação concluída
          ↓
   cards.exchange  (routing key: delivery.evaluated)
          ↓
  ┌───────────────────────┬──────────────────────┐
  ↓                                              ↓
delivery.evaluated.persist          delivery.evaluated.s3
  ↓                                              ↓
Persiste no banco (PostgreSQL)      Backup em JSON no S3
(tabela auditoria_consulta)         (bucket carddelivery-audit)
```

Se um listener falhar ao processar, a mensagem vai para a **Dead Letter Queue** (`delivery.evaluated.dlq`).

> A falha ao publicar **não bloqueia** a resposta da API — é tratada como erro não-crítico.

Para acompanhar o fluxo em tempo real, acesse http://localhost:15672 e observe a aba **Queues** enquanto dispara requisições pelo Postman.

---

## Validando o backup no S3 (LocalStack)

Após cada avaliação, um arquivo JSON é gravado no bucket `carddelivery-audit` com o caminho `ano/mês/dia/{eventId}.json`.

### Passo 1 — Listar os arquivos gravados

```bash
docker exec carddelivery-localstack awslocal s3 ls s3://carddelivery-audit --recursive
```

A saída será algo como:

```
2026-05-03 21:30:00      1234   2026/05/03/550e8400-e29b-41d4-a716-446655440000.json
```

### Passo 2 — Ler o conteúdo do arquivo

Copie o caminho exibido no passo anterior e substitua abaixo:

```bash
docker exec -it carddelivery-localstack \
  env AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 \
  s3 cp s3://carddelivery-audit/2026/05/03/550e8400-e29b-41d4-a716-446655440000.json -
```

O conteúdo do arquivo será exibido diretamente no terminal — é o JSON completo do evento `DeliveryEvaluatedEvent` com todos os dados da avaliação.

---

## Como rodar os testes e enviar ao SonarQube

> No Windows, sempre use o **PowerShell**. Rodar via bash (Git Bash) pode causar erros de parsing nos argumentos.

### Rodando os testes

```powershell
# Rodar todos os testes
.\mvnw.cmd test

# Rodar testes + gerar relatório de cobertura JaCoCo
.\mvnw.cmd verify
```

O relatório de cobertura é gerado em `target/site/jacoco/index.html`.  
**Cobertura mínima exigida:** 80% de linhas.

### Enviando ao SonarQube

**1. Gere o token de acesso:**
1. Acesse http://localhost:9000 com `admin / admin`
2. Vá em **My Account → Security → Generate Token**
3. Copie o token gerado

**2. Execute o comando:**

```powershell
.\mvnw.cmd verify sonar:sonar "-Dsonar.login=SEU_TOKEN"
```

Esse comando roda os testes, gera o relatório de cobertura e envia tudo ao SonarQube em sequência.

O resultado fica disponível em: http://localhost:9000/dashboard?id=card-delivery-hub

---

## Comandos úteis

```bash
# Acompanhar logs da aplicação
docker compose logs -f app

# Rebuild apenas da API após mudança de código
docker compose up -d --build app

# Subir só a infra (sem a API)
docker compose up -d postgres pgadmin rabbitmq wiremock localstack

# Parar tudo (preserva volumes e dados)
docker compose down

# Parar tudo e apagar dados (CUIDADO — banco zerado)
docker compose down -v

# Acessar shell do container da API
docker exec -it carddelivery-app sh

# Verificar filas do RabbitMQ via CLI
docker exec carddelivery-rabbitmq rabbitmqctl list_queues name messages
```
