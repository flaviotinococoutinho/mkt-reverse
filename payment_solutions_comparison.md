# Comparativo: Stripe Connect vs. Adyen for Platforms
## Análise para Marketplace "Compre-Pra-Mim" no Brasil

Este documento compara as duas principais soluções de pagamento para marketplaces operando no Brasil, focando em custos, complexidade de integração e conformidade regulatória.

---

## 1. Stripe Connect

### 1.1. Estrutura de Taxas (Brasil)

**Taxas de Processamento:**
- **Taxa Principal:** 3.99% + R$0.50 por transação
- **Onboarding/Verificação:** R$7 por usuário ativo
- **Payouts:** R$0.67 + 0.25% por transferência
- **Taxa Mensal (modelo "You Handle Pricing"):** R$6 por conta ativa por mês
- **Impostos:** Todas as taxas estão sujeitas a impostos indiretos brasileiros

**Modelos de Monetização:**
1. **"Stripe Handles Pricing":** Sem taxa para a plataforma, Stripe gerencia toda a precificação
2. **"You Handle Pricing":** Plataforma define suas próprias taxas e comissões

### 1.2. Conformidade e Regulamentação (2025)

**Requisitos Atualizados para 2025:**
- **Verificação CPF:** Obrigatória para representantes, diretores, proprietários e executivos
- **Verificação de Endereço:** Documentação comprobatória do endereço da empresa
- **Status CNPJ:** Verificação do status de registro junto ao CNPJ
- **Conformidade Banco Central:** Alinhamento com regulamentações do BCB

**Recursos de Compliance Inclusos:**
- Conformidade PCI automática
- Verificação de identidade e KYC
- Verificação de sanções
- Tokenização de dados de cartão
- Licenças globais do Stripe

### 1.3. Integração e Experiência do Desenvolvedor

**Pontos Fortes:**
- Documentação extensa e bem estruturada
- SDKs robustos para múltiplas linguagens
- Webhooks confiáveis para eventos em tempo real
- Dashboard intuitivo para gestão
- Suporte a split payments nativo

**Complexidade:**
- **Baixa a Média:** Integração relativamente simples para casos de uso padrão
- **Customização:** Flexível para necessidades específicas de marketplace

---

## 2. Adyen for Platforms

### 2.1. Estrutura de Taxas (Brasil)

**Taxas de Processamento:**
- **Taxa Base:** €0.11 + percentual variável por método de pagamento
- **Sem Taxas Fixas:** Não há taxas mensais, de setup, integração ou encerramento
- **Fatura Mínima:** Varia por região e volume (aproximadamente €150/mês)
- **Métodos Locais:** Suporte nativo a Boleto, PIX, Elo

**Estrutura Transparente:**
- Taxa fixa baixa + percentual específico por método
- Negociação comercial para volumes maiores
- Sem surpresas em taxas ocultas

### 2.2. Conformidade e Regulamentação

**Recursos de Compliance:**
- Verificações KYC e AML integradas
- Verificação de listas MATCH
- Conformidade PCI
- Conformidade PSD2
- Certificação regulatória global desde o primeiro dia

**Suporte Local Brasil:**
- Integração com Banco Bradesco e Banco do Brasil
- Suporte a Visa Electron
- Processamento em Reais brasileiros
- Conhecimento local das preferências de pagamento

### 2.3. Integração e Experiência do Desenvolvedor

**Pontos Fortes:**
- Plataforma unificada para expansão global
- API robusta e bem documentada
- Suporte a 150+ moedas
- Ferramentas avançadas de prevenção à fraude
- Onboarding simplificado para usuários globais

**Complexidade:**
- **Média:** Requer mais configuração inicial, mas oferece maior flexibilidade
- **Escalabilidade:** Excelente para operações globais desde o início

---

## 3. Análise Comparativa

### 3.1. Custos (Cenário Hipotético: R$100.000 GMV/mês)

**Stripe Connect:**
- Processamento: R$100.000 × 3.99% = R$3.990
- Taxa fixa: R$100.000 ÷ R$100 (ticket médio) × R$0.50 = R$500
- Payouts: 1000 transações × R$0.67 = R$670
- **Total Mensal:** ~R$5.160 + impostos

**Adyen for Platforms:**
- Processamento: €0.11 × 1000 + percentual variável (~3.5%) = ~R$4.000
- Fatura mínima: €150 = ~R$900
- **Total Mensal:** ~R$4.000-4.500 (dependendo do mix de métodos)

### 3.2. Adequação ao "Compre-Pra-Mim"

**Stripe Connect - Vantagens:**
- ✅ Integração mais rápida para MVP
- ✅ Documentação superior
- ✅ Ecossistema maduro no Brasil
- ✅ Suporte robusto a webhooks para eventos de marketplace
- ✅ Modelo "You Handle Pricing" ideal para comissões customizadas

**Stripe Connect - Desvantagens:**
- ❌ Custos potencialmente mais altos em escala
- ❌ Menos flexibilidade para métodos de pagamento locais específicos

**Adyen for Platforms - Vantagens:**
- ✅ Custos mais competitivos em escala
- ✅ Suporte nativo superior a métodos brasileiros (PIX, Boleto)
- ✅ Melhor para expansão internacional futura
- ✅ Ferramentas de fraude mais avançadas
- ✅ Sem taxas mensais fixas

**Adyen for Platforms - Desvantagens:**
- ❌ Curva de aprendizado mais íngreme
- ❌ Integração inicial mais complexa
- ❌ Menos recursos de comunidade/tutoriais

---

## 4. Recomendação Estratégica

### 4.1. Para MVP (Primeiros 6 meses)
**Recomendação: Stripe Connect**

**Justificativa:**
- Velocidade de implementação crítica para validação
- Documentação e suporte superiores para equipe pequena
- Menor risco técnico
- Funcionalidades de marketplace prontas

### 4.2. Para Escala (Após validação)
**Recomendação: Avaliar migração para Adyen**

**Justificativa:**
- Economia significativa em custos com volume
- Melhor suporte a métodos de pagamento locais
- Preparação para expansão LATAM
- Ferramentas de fraude mais sofisticadas

### 4.3. Estratégia Híbrida (Avançada)
**Possibilidade:** Implementar ambos

- Stripe para onboarding rápido de vendedores pequenos
- Adyen para vendedores enterprise com volumes maiores
- Requer arquitetura mais complexa, mas maximiza benefícios

---

## 5. Próximos Passos Recomendados

1. **Contato Comercial:** Solicitar propostas comerciais detalhadas de ambos
2. **POC Técnico:** Implementar protótipo básico com Stripe Connect
3. **Análise de Compliance:** Validar requisitos específicos com advogados especializados
4. **Roadmap de Migração:** Planejar possível transição futura se necessário

**Decisão Final:** Iniciar com Stripe Connect para o MVP, mantendo Adyen como opção estratégica para o futuro.
