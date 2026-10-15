# Executar a infraestrutura local

O arquivo Compose mantido é [docker-compose.yml](../docker-compose.yml), na raiz. Execute os comandos deste guia a partir da raiz do repositório.

## Iniciar as dependências do backend

Com Docker e Compose v2 instalados:

```bash
docker compose up -d postgres-main rabbitmq redis
docker compose ps
```

O Compose inicia os serviços nomeados explicitamente, mesmo quando eles pertencem a um profile. Para ver a configuração resolvida antes de iniciar os containers, execute `docker compose --profile local config`.

| Serviço | Porta no host | Uso |
|---|---|---|
| `postgres-main` | `5432` | Banco `marketplace_main` |
| `rabbitmq` | `5672` e `15672` | Broker e console de administração |
| `redis` | `6379` | Dependência Redis |

As credenciais de laboratório estão no Compose e no [profile local da API](../application/api-gateway/src/main/resources/application-local.yml). O PostgreSQL monta `docker/postgres/init.sql` e `docker/postgres/seed.sql` na primeira inicialização do volume. Alterar esses scripts não reaplica o conteúdo a um volume já inicializado.

## Iniciar a aplicação

Siga o [procedimento do backend e do frontend](../README.md#executar-localmente). O guia executa Maven e Vite no host, com a infraestrutura em containers.

O Compose também declara `api-gateway` e `web-app`, mas a existência desses serviços não comprova que o build em container esteja validado. Para investigar essa variante, confira os Dockerfiles e as dependências dos serviços antes de executá-la.

## Consultar logs e parar os serviços

```bash
docker compose logs -f postgres-main rabbitmq redis
docker compose --profile local --profile dev --profile mvp down
```

O comando `down` mantém os volumes. Se quiser apagar os dados do laboratório, acrescente `-v` ao comando. Essa opção remove os volumes do projeto.

## Limites conhecidos

- `docker-compose.local.yml` e `docker-compose.dev.yml` não estão no repositório. Os alvos Make que os referenciam precisam ser corrigidos antes do uso.
- O Compose atual não declara Mailhog, MinIO, Kafka ou OpenSearch. As referências a esses serviços em alvos antigos do Makefile não descrevem o ambiente disponível.
- O guia não pressupõe contas de teste ou quantidades de registros no seed. Cadastre os usuários pela interface ou use o smoke autenticado descrito no README da raiz.
