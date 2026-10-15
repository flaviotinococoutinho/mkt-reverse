# Frontend do QueroJá

Interface de comprador e vendedor em React, TypeScript e Vite. A aplicação consome a API do gateway em `/api/v1`.

## Executar em desenvolvimento

Use Node.js `^20.19.0 || >=22.12.0`, conforme o requisito do Vite no `package-lock.json`. O CI usa Node 22. Instale as dependências com npm para preservar o lockfile do projeto.

Primeiro, inicie o backend conforme o [guia da raiz](../../README.md#executar-localmente). Depois, a partir da raiz:

```bash
cd application/web-app
npm ci
npm run dev
```

Abra `http://localhost:5173`. A porta é fixa; o Vite falha se ela já estiver ocupada.

## Configurar o destino da API

Dentro de `application/web-app`, crie o arquivo local de configuração:

```bash
cp .env.example .env
```

As variáveis disponíveis são:

| Variável | Padrão | Uso |
|---|---|---|
| `VITE_API_URL` | `/api/v1` | URL base usada pelo cliente no navegador |
| `VITE_API_TARGET` | `http://localhost:8081` | Destino do proxy do servidor de desenvolvimento |

O proxy de [vite.config.ts](vite.config.ts) encaminha `/api`, `/graphql` e `/actuator` ao backend. Reinicie o servidor Vite depois de alterar o ambiente.

## Gerar e verificar o build

Execute neste diretório:

```bash
npm run lint
npm test
npm run build
npm run preview
```

O `preview` serve o build local. Ele não configura um proxy de API para produção. O ambiente de publicação precisa encaminhar `/api/v1` ao gateway ou definir `VITE_API_URL` no build.

## Executar os smokes

Os scripts disponíveis estão em [package.json](package.json).

| Comando | O que verifica | Requisito |
|---|---|---|
| `npm run smoke:ui` | Declaração das rotas de comprador e vendedor | Código local |
| `npm run smoke:session` | Tratamento de sessão inválida no frontend | Código local |
| `npm run smoke:api:auth` | Cadastro, login e fluxo de solicitação e proposta | API e banco ativos |
| `npm run smoke:api:auth:invalid` | Rejeição de token inválido | API e banco ativos |
| `npm run smoke:api:report-check` | Fluxo de API e limites definidos para o relatório | API e banco ativos |
| `npm run smoke:mvp` | Smoke de API, rotas e detalhe de proposta | API e banco ativos |

O smoke de API aguarda o endpoint de saúde antes de executar o fluxo. Configure o cenário pelas variáveis abaixo:

| Variável | Padrão ou efeito |
|---|---|
| `API_BASE_URL` | `http://localhost:8081/api/v1` |
| `API_HEALTH_URL` | Derivado de `API_BASE_URL` |
| `SMOKE_STARTUP_TIMEOUT_MS` | `45000` |
| `SMOKE_STARTUP_POLL_MS` | `1500` |
| `SMOKE_INCLUDE_ATTRIBUTES=1` | Inclui atributos tipados no payload; por padrão, o smoke os omite |
| `SMOKE_REPORT_PATH` | Caminho do relatório; o script de relatório usa `./build/smoke-report.json` |
| `SMOKE_REPORT_INPUT` | Arquivo lido pelo verificador de relatório |
| `SMOKE_MAX_TOTAL_MS` | `60000` no verificador de relatório |
| `SMOKE_MAX_STEP_MS` | `25000` no verificador de relatório |

Se a API recusar atributos com `VALIDATION_ERROR`, use `SMOKE_INCLUDE_ATTRIBUTES=0 npm run smoke:api:report-check` para isolar o problema. Para testar a autorização, use o cenário de token inválido da tabela.

## Autenticação e limites do MVP

As telas de cadastro e login usam `POST /api/v1/auth/register` e `POST /api/v1/auth/login`. O contexto de autenticação guarda `token` e `user` no `localStorage`.

O MVP não envia e-mails. Quando o cadastro não informa um e-mail, o frontend deriva do telefone um identificador no domínio `mvp.local`. O [roadmap](../../docs/product/roadmap.md) descreve a evolução de identidade e recuperação de acesso.

Consulte a [arquitetura](../../ARCHITECTURE.md) para os limites do contrato, do escrow simulado e das notificações.
