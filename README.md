# QueroJá: marketplace reverso C2B

Prova de conceito de um marketplace em que o comprador publica uma intenção de compra e vendedores enviam propostas seladas. O comprador compara as propostas e aceita uma delas.

O backend é um monólito modular em Java e Spring Boot. O frontend usa React, TypeScript e Vite. O fluxo de contrato usa um simulador de escrow; a integração com um provedor de pagamento real permanece no [plano de execução](docs/product/next-fronts.md).

## Executar localmente

Execute os comandos a partir da raiz do repositório, exceto quando o bloco indicar outro diretório.

Requisitos:

- JDK compatível com Java 21 e Maven. O `pom.xml` compila para Java 21; o workflow do MVP usa JDK 25.
- Node.js compatível com o Vite registrado no lockfile: `^20.19.0 || >=22.12.0`. O CI usa Node 22.
- Docker com Compose v2 para os serviços de infraestrutura.

### Iniciar a infraestrutura

O arquivo disponível é `docker-compose.yml`, na raiz. Inicie os serviços usados pelo backend local:

```bash
docker compose up -d postgres-main rabbitmq redis
docker compose ps
```

O PostgreSQL publica a porta `5432`, o RabbitMQ publica `5672` e o Redis publica `6379`. A configuração local do banco está em [application-local.yml](application/api-gateway/src/main/resources/application-local.yml).

Os alvos `make dev-local-up` e `make dev-up` ainda referenciam arquivos Compose ausentes. Use o comando acima até esses alvos serem corrigidos.

### Iniciar o backend

Instale primeiro os módulos Maven dos quais o gateway depende:

```bash
mvn -pl application/api-gateway -am install -DskipTests
mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

O gateway atende em `http://localhost:8081`. Em outro terminal, confira a saúde da API:

```bash
curl -fsS http://localhost:8081/actuator/health
```

### Iniciar o frontend

Em outro terminal, a partir da raiz:

```bash
cd application/web-app
npm ci
npm run dev
```

Abra `http://localhost:5173`. O Vite encaminha `/api`, `/graphql` e `/actuator` ao gateway. Para alterar esse destino, consulte o [guia do frontend](application/web-app/README.md).

## Validar o fluxo

A partir da raiz, execute os testes do gateway:

```bash
mvn -pl application/api-gateway -am test
```

Com a API e a infraestrutura ativas e as dependências do frontend instaladas, execute o smoke de API:

```bash
make smoke-mvp-report-check
```

O smoke registra e autentica comprador e vendedor, cria uma solicitação, envia uma proposta e aceita a proposta. O relatório fica em `application/web-app/build/smoke-report.json`. O alvo `make verify-mvp-daily` reúne testes do backend e o smoke consolidado definido no Makefile.

Para validar o frontend sem alterar o diretório do terminal:

```bash
(cd application/web-app && npm run lint && npm test && npm run build)
```

O [workflow do MVP](.github/workflows/mvp-daily-guardrail.yml) descreve a sequência usada pelo CI. A presença desses checks não implica que tenham passado na sua máquina.

## Escopo atual

| Componente | Responsabilidade |
|---|---|
| `modules/user-management` | Identidade, credenciais e papéis de acesso |
| `modules/sourcing-management` | Intenção de compra, atributos por categoria, propostas e aceite |
| `modules/catalog-management` | Tipos de ativo e definições de atributos para evolução do catálogo |
| `modules/agreement-management` | Contrato, snapshot, prazos e operações de escrow pela porta `EscrowGateway` |
| `modules/notification-service` | Notificações persistidas e feed no aplicativo |
| `application/api-gateway` | Composição dos módulos, REST, GraphQL e segurança |
| `application/web-app` | Interface de comprador e vendedor |
| `shared/` | Tipos compartilhados, publicação de eventos e infraestrutura |

A API REST usa `/api/v1`; o endpoint GraphQL é `/graphql`. O backend publica eventos por outbox transacional em `shr_outbox_events`, com relay para RabbitMQ. A busca principal usa PostgreSQL; o adaptador OpenSearch depende de configuração opcional.

O MVP não envia e-mails nem recebe upload de imagens. O adaptador real de pagamento, a verificação de identidade e a operação de disputas constam do roadmap. Os documentos de produto registram o desenho pretendido e seus critérios de entrada; esses planos não comprovam integração financeira ou conformidade regulatória em produção.

## Diagnosticar falhas do smoke

- Se ocorrer `ECONNREFUSED`, confira os containers e a resposta de `/actuator/health` antes de repetir o smoke.
- Se a API retornar `VALIDATION_ERROR` nos atributos, execute `SMOKE_INCLUDE_ATTRIBUTES=0 make smoke-mvp-report-check` para isolar o payload. Confira depois a categoria e os atributos em `CategoryAttributeSchema`.
- Para testar a rejeição de um token inválido, execute `SMOKE_AUTH_INVALID=1 make smoke-mvp-report-check`. Esse cenário espera `401` ou `403`.

## Consultar a documentação

- [Stack](STACK.md): versões declaradas e convenções técnicas.
- [Arquitetura](ARCHITECTURE.md): módulos, contrato e eventos.
- [Ambiente Docker](docker/README.md): infraestrutura local e limites do Compose atual.
- [Identidade do produto](docs/product/identity.md) e [visão do produto](docs/product/vision.md): público e objetivos.
- [Modelo de negócio](docs/product/business-model.md): hipóteses, fases e monetização.
- [Limites de compliance](docs/compliance/guardrails.md): controles previstos por domínio.
- [Roadmap](docs/product/roadmap.md) e [sequência técnica](docs/product/next-fronts.md): trabalho planejado.
- [Auditoria de produto de 09/08/2026](docs/product/pm-review.md): achados históricos, que precisam ser confrontados com o código atual.
- [Changelog](CHANGELOG.md): histórico de alterações.
