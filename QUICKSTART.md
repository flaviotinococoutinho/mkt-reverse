# 🚀 Guia de Início Rápido - Marketplace Reverso C2B

Este guia te ajudará a rodar o MVP do Marketplace Reverso C2B em poucos minutos.

## 📋 Pré-requisitos

- **Docker** 20.10+ e **Docker Compose** 2.0+
- **8GB RAM** mínimo
- **20GB** espaço em disco
- Portas disponíveis: 3000, 5432, 5672, 8080, 8081, 8082, 15672

## ⚡ Início Rápido (3 Passos)

### 1️⃣ Clone o Repositório

```bash
git clone https://github.com/flaviotinococoutinho/mkt-reverse.git
cd mkt-reverse
```

### 2️⃣ Inicie os Serviços

```bash
docker-compose -f docker-compose.mvp.yml up --build
```

**Aguarde** 5-10 minutos para todos os serviços iniciarem na primeira vez.

### 3️⃣ Acesse a Aplicação

Abra seu navegador em: **http://localhost:3000**

## 🎯 O Que Você Pode Fazer

### Como **Consumidor** (Comprador)

1. **Registre-se** com role `CONSUMER`
2. **Crie uma oportunidade** de compra
   - Título: "Preciso de 100 camisetas personalizadas"
   - Descrição: Detalhes do que você precisa
   - Orçamento: R$ 5.000,00
   - Prazo: 30 dias
3. **Receba propostas** de empresas interessadas
4. **Aceite ou rejeite** propostas
5. **Receba notificações** em tempo real

### Como **Empresa** (Fornecedor)

1. **Registre-se** com role `COMPANY`
2. **Navegue pelas oportunidades** disponíveis
3. **Envie propostas** para oportunidades
   - Seu preço
   - Prazo de entrega
   - Descrição da sua oferta
4. **Acompanhe o status** das suas propostas
5. **Receba notificações** quando propostas forem aceitas/rejeitadas

## 🔗 URLs Importantes

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **Frontend** | http://localhost:3000 | Interface React |
| **BFF Gateway** | http://localhost:8080 | API Gateway + Auth |
| **Opportunity API** | http://localhost:8081 | Serviço de Oportunidades |
| **Proposal API** | http://localhost:8082 | Serviço de Propostas |
| **RabbitMQ UI** | http://localhost:15672 | Gerenciamento de Filas |
| **PostgreSQL** | localhost:5432 | Banco de Dados |

### Credenciais RabbitMQ

- **Usuário**: marketplace
- **Senha**: marketplace_pass

## 🧪 Testando a Aplicação

### 1. Criar Usuário Consumidor

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao_consumer",
    "email": "joao@example.com",
    "password": "senha123",
    "role": "CONSUMER"
  }'
```

### 2. Fazer Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao_consumer",
    "password": "senha123"
  }'
```

Copie o `accessToken` retornado.

### 3. Criar Oportunidade

```bash
curl -X POST http://localhost:8080/api/opportunities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "title": "Preciso de 100 camisetas personalizadas",
    "description": "Camisetas 100% algodão com logo da empresa",
    "category": "fashion",
    "budget": {
      "amount": 5000.00,
      "currency": "BRL"
    },
    "deadline": "2025-12-31",
    "specifications": [
      {"key": "Material", "value": "100% Algodão", "required": true},
      {"key": "Cores", "value": "Branco e Preto", "required": true}
    ]
  }'
```

## 📊 Monitoramento

### Ver Logs em Tempo Real

```bash
# Todos os serviços
docker-compose -f docker-compose.mvp.yml logs -f

# Apenas frontend
docker-compose -f docker-compose.mvp.yml logs -f frontend

# Apenas BFF Gateway
docker-compose -f docker-compose.mvp.yml logs -f bff-gateway
```

### Verificar Status dos Serviços

```bash
docker-compose -f docker-compose.mvp.yml ps
```

### Health Checks

```bash
# BFF Gateway
curl http://localhost:8080/actuator/health

# Opportunity Service
curl http://localhost:8081/actuator/health

# Proposal Service
curl http://localhost:8082/actuator/health
```

## 🛑 Parar a Aplicação

```bash
# Parar serviços
docker-compose -f docker-compose.mvp.yml down

# Parar e remover volumes (reset completo)
docker-compose -f docker-compose.mvp.yml down -v
```

## 🐛 Troubleshooting

### Porta já em uso

```bash
# Verificar portas em uso
netstat -tuln | grep -E '3000|5432|5672|8080|8081|8082'

# Matar processo na porta 3000 (exemplo)
lsof -ti:3000 | xargs kill -9
```

### Serviço não inicia

```bash
# Ver logs detalhados
docker-compose -f docker-compose.mvp.yml logs -f <nome-do-servico>

# Reconstruir serviço específico
docker-compose -f docker-compose.mvp.yml up --build --no-deps <nome-do-servico>
```

### Banco de dados não conecta

```bash
# Verificar PostgreSQL
docker-compose -f docker-compose.mvp.yml exec postgres pg_isready -U marketplace_user

# Acessar PostgreSQL
docker-compose -f docker-compose.mvp.yml exec postgres psql -U marketplace_user -d marketplace
```

### Frontend não carrega

```bash
# Limpar cache do Docker
docker system prune -a

# Reconstruir frontend
docker-compose -f docker-compose.mvp.yml up --build --no-deps frontend
```

## 📚 Documentação Adicional

- **Arquitetura**: Ver `ARCHITECTURE.md`
- **Guia de Desenvolvimento**: Ver `DEVELOPMENT_GUIDE.md`
- **Docker Detalhado**: Ver `DOCKER_README.md`
- **Roadmap**: Ver `ROADMAP.md`

## 🎓 Próximos Passos

1. ✅ Explorar a interface do usuário
2. ✅ Criar oportunidades e propostas
3. ✅ Testar notificações em tempo real
4. ✅ Explorar RabbitMQ Management UI
5. 📖 Ler a documentação completa
6. 🔧 Personalizar configurações
7. 🚀 Deploy em produção

## 💡 Dicas

- Use **RabbitMQ Management UI** para ver mensagens em tempo real
- Abra **múltiplas abas** do navegador para testar notificações WebSocket
- Crie usuários com **roles diferentes** para testar fluxos completos
- Verifique os **logs** se algo não funcionar como esperado

## 🆘 Suporte

- 📧 Email: suporte@marketplace.com
- 🐛 Issues: https://github.com/flaviotinococoutinho/mkt-reverse/issues
- 📖 Wiki: https://github.com/flaviotinococoutinho/mkt-reverse/wiki

---

**Desenvolvido com ❤️ usando Java Spring Boot, React, PostgreSQL e RabbitMQ**
