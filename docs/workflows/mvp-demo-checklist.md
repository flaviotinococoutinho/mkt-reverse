# MVP Demo Checklist (QueroJá / Marketplace Reverso)

Objetivo: rodar uma demonstração curta, repetível e verificável do fluxo principal do MVP.

## Pré-condições

1. API + banco locais ativos.
2. Frontend ativo (opcional para demo visual).
3. Sem dependências externas além das já definidas no repositório.

## Setup rápido (local)

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

make dev-local-up
mvn -pl application/api-gateway -am install -DskipTests
mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Em outro terminal (opcional, demo visual):

```bash
cd /Users/flaviocoutinho/development/mkt-reverse/application/web-app
npm install
npm run dev
```

## Roteiro de demonstração (API-first)

### 1) Validar saúde da API

```bash
curl http://localhost:8081/actuator/health
```

Critério de sucesso:
- resposta `UP`.

### 2) Executar smoke do fluxo core sem auth

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
make smoke-mvp
```

Critério de sucesso:
- cria evento de sourcing,
- cria proposta de supplier,
- aceita proposta,
- status final do evento = `AWARDED`,
- status final da proposta = `ACCEPTED`.

### 3) Executar smoke do fluxo core com auth (registro/login)

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
make smoke-mvp-auth
```

Critério de sucesso:
- registra buyer e supplier,
- login dos dois perfis,
- fluxo core completo também finaliza com `AWARDED`/`ACCEPTED`.

## Roteiro de demonstração visual (frontend)

1. Acessar `http://localhost:5173`.
2. Buyer: login/cadastro e criação de solicitação.
3. Supplier: descoberta de oportunidade e envio de proposta.
4. Buyer: aceitar proposta na tela de detalhe.

Critérios visuais:
- mudança de status refletida no badge,
- proposta aceita destacada,
- feedback de sucesso/erro via toast.

## Encerramento

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
make dev-local-down
```

(Se api-gateway estiver em execução manual, encerrar com Ctrl+C no terminal correspondente.)
