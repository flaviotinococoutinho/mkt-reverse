#!/usr/bin/env node

/**
 * Smoke test do fluxo crítico do Marketplace Reverso (API):
 * 1) Buyer cria sourcing event
 * 2) Supplier descobre oportunidade
 * 3) Supplier envia proposta
 * 4) Buyer aceita proposta
 * 5) Valida status final de evento e proposta
 *
 * Uso:
 *   API_BASE_URL=http://localhost:8081/api/v1 node scripts/smoke-flow.mjs
 */

const apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:8081/api/v1';
const includeAttributes = process.env.SMOKE_INCLUDE_ATTRIBUTES === '1';
const smokeAuth = process.env.SMOKE_AUTH === '1';
const healthUrl =
  process.env.API_HEALTH_URL ||
  apiBaseUrl.replace(/\/api\/v1\/?$/, '/actuator/health');
const startupTimeoutMs = Number(process.env.SMOKE_STARTUP_TIMEOUT_MS || 45000);
const startupPollMs = Number(process.env.SMOKE_STARTUP_POLL_MS || 1500);

function nowStamp() {
  return new Date().toISOString().replace(/[.:]/g, '-');
}

function uid(prefix) {
  const compactTs = Date.now().toString(36);
  const random = Math.random().toString(36).slice(2, 10);
  const raw = `${prefix}-${compactTs}-${random}`;
  return raw.slice(0, 36);
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

async function waitForApiReady() {
  const deadline = Date.now() + startupTimeoutMs;

  while (Date.now() < deadline) {
    try {
      const response = await fetch(healthUrl);
      if (response.ok) {
        const payload = await response.json().catch(() => ({}));
        if (!payload.status || payload.status === 'UP') {
          return;
        }
      }
    } catch {
      // API ainda subindo; tenta novamente.
    }

    await new Promise((resolve) => setTimeout(resolve, startupPollMs));
  }

  throw new Error(
    `API indisponível após ${startupTimeoutMs}ms. Verifique ${healthUrl} ou ajuste SMOKE_STARTUP_TIMEOUT_MS.`
  );
}

async function request(path, { method = 'GET', body, headers } = {}) {
  const url = `${apiBaseUrl}${path}`;
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(headers || {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : undefined;
  } catch {
    data = text;
  }

  if (!response.ok) {
    throw new Error(`${method} ${path} -> ${response.status} ${response.statusText}\n${typeof data === 'string' ? data : JSON.stringify(data, null, 2)}`);
  }

  return data;
}

function phoneToMvpEmail(phone) {
  const digits = String(phone || '').replace(/\D+/g, '');
  return `mvp+${digits || 'unknown'}@mvp.local`;
}

async function registerAndLogin({ role, phone, password, documentType, documentNumber }) {
  const registerPayload = {
    email: phoneToMvpEmail(phone),
    password,
    firstName: role === 'supplier' ? 'Smoke' : 'Comprador',
    lastName: role === 'supplier' ? 'Fornecedor' : 'Teste',
    displayName: role === 'supplier' ? 'Smoke Supplier' : 'Smoke Buyer',
    documentNumber,
    documentType,
    userType: role === 'supplier' ? 'SUPPLIER' : 'BUYER',
  };

  await request('/auth/register', {
    method: 'POST',
    body: registerPayload,
  });

  const loginPayload = {
    email: registerPayload.email,
    password,
  };

  const auth = await request('/auth/login', {
    method: 'POST',
    body: loginPayload,
  });

  const token = auth?.token;
  assert(token, `Auth sem token para role=${role}`);
  return token;
}

function extractHalItems(payload) {
  if (!payload || typeof payload !== 'object') return [];
  const embedded = payload._embedded;
  if (!embedded || typeof embedded !== 'object') return [];

  for (const value of Object.values(embedded)) {
    if (Array.isArray(value)) return value;
  }

  return [];
}

async function run() {
  console.log(`\n🚦 Smoke test iniciado em: ${apiBaseUrl}`);
  console.log(`⏳ Aguardando API pronta em: ${healthUrl}`);
  await waitForApiReady();
  console.log('   ✅ API pronta');

  const tenantId = 'tenant-default';
  const buyerOrganizationId = uid('buyer-org');
  const supplierId = uid('supplier');
  const password = 'SmokeMvp#123';

  let buyerToken = null;
  let supplierToken = null;

  if (smokeAuth) {
    console.log('0) Registrando e autenticando buyer/supplier...');
    const buyerPhone = `+55 11 9${Math.floor(Math.random() * 90000000 + 10000000)}`;
    const supplierPhone = `+55 21 9${Math.floor(Math.random() * 90000000 + 10000000)}`;

    buyerToken = await registerAndLogin({
      role: 'buyer',
      phone: buyerPhone,
      password,
      documentType: 'CPF',
      documentNumber: `${Math.floor(Math.random() * 90000000000 + 10000000000)}`,
    });

    supplierToken = await registerAndLogin({
      role: 'supplier',
      phone: supplierPhone,
      password,
      documentType: 'CNPJ',
      documentNumber: `${Math.floor(Math.random() * 90000000000000 + 10000000000000)}`,
    });

    console.log('   ✅ Auth buyer/supplier validado');
  }

  const createEventPayload = {
    tenantId,
    buyerOrganizationId,
    buyerContactName: 'Smoke Buyer',
    buyerContactEmail: 'smoke-buyer@example.com',
    buyerContactPhone: '+55 11 99999-0000',
    title: `Pedido smoke ${nowStamp()}`,
    description: 'Teste automatizado do fluxo crítico do MVP',
    type: 'RFQ',
    mccCategoryCode: 5533,
    productName: 'Peça X',
    productDescription: 'Compatível com Y',
    category: 'part',
    unitOfMeasure: 'UN',
    quantityRequired: 5,
    attributes: includeAttributes
      ? [
          { key: 'voltage', type: 'VOLTAGE', unit: 'V', value: 220 },
          { key: 'color', type: 'COLOR', value: 'preto' },
        ]
      : [],
    validForHours: 48,
    estimatedBudgetCents: 2500000,
  };

  console.log('1) Criando sourcing event...');
  const createdEvent = await request('/sourcing-events', {
    method: 'POST',
    body: createEventPayload,
    headers: buyerToken ? { Authorization: `Bearer ${buyerToken}` } : undefined,
  });
  const eventId = createdEvent?.id;
  assert(eventId, 'Resposta de criação sem id de evento');
  console.log(`   ✅ Evento criado: ${eventId}`);

  console.log('2) Buscando oportunidades para supplier...');
  const opportunitiesPayload = await request(
    `/opportunities?tenantId=${encodeURIComponent(tenantId)}&supplierId=${encodeURIComponent(supplierId)}&page=0&size=20`,
    {
      headers: supplierToken ? { Authorization: `Bearer ${supplierToken}` } : undefined,
    }
  );
  const opportunities = extractHalItems(opportunitiesPayload);
  const foundOpportunity = opportunities.find((item) => item.id === eventId);
  assert(foundOpportunity, `Evento ${eventId} não encontrado em /opportunities`);
  console.log('   ✅ Oportunidade encontrada');

  console.log('3) Enviando proposta do supplier...');
  const submitResponsePayload = {
    supplierId,
    offerCents: 2350000,
    leadTimeDays: 7,
    warrantyMonths: 12,
    condition: 'NEW',
    shippingMode: 'SELLER',
    message: 'Proposta enviada via smoke test automatizado',
  };

  const createdResponse = await request(`/sourcing-events/${eventId}/responses`, {
    method: 'POST',
    body: submitResponsePayload,
    headers: supplierToken ? { Authorization: `Bearer ${supplierToken}` } : undefined,
  });
  const responseId = createdResponse?.id;
  assert(responseId, 'Resposta de criação de proposta sem id');
  console.log(`   ✅ Proposta criada: ${responseId}`);

  console.log('4) Validando proposta listada no evento...');
  const responsesBeforeAccept = await request(`/sourcing-events/${eventId}/responses`, {
    headers: buyerToken ? { Authorization: `Bearer ${buyerToken}` } : undefined,
  });
  assert(Array.isArray(responsesBeforeAccept), 'Resposta de /responses não é array');
  const pending = responsesBeforeAccept.find((item) => item.id === responseId);
  assert(pending, `Proposta ${responseId} não encontrada em /responses`);
  console.log(`   ✅ Proposta visível com status inicial: ${pending.status ?? '(sem status)'}`);

  console.log('5) Aceitando proposta...');
  await request(`/sourcing-events/${eventId}/responses/${responseId}/accept`, {
    method: 'POST',
    headers: buyerToken ? { Authorization: `Bearer ${buyerToken}` } : undefined,
  });
  console.log('   ✅ Proposta aceita');

  console.log('6) Validando status final do evento...');
  const eventAfterAccept = await request(`/sourcing-events/${eventId}`, {
    headers: buyerToken ? { Authorization: `Bearer ${buyerToken}` } : undefined,
  });
  assert(eventAfterAccept?.status === 'AWARDED', `Status do evento esperado AWARDED, recebido: ${eventAfterAccept?.status}`);
  console.log('   ✅ Evento com status AWARDED');

  console.log('7) Validando status final da proposta...');
  const responsesAfterAccept = await request(`/sourcing-events/${eventId}/responses`, {
    headers: buyerToken ? { Authorization: `Bearer ${buyerToken}` } : undefined,
  });
  const accepted = responsesAfterAccept.find((item) => item.id === responseId);
  assert(accepted, `Proposta ${responseId} não encontrada após aceite`);
  assert(accepted.status === 'ACCEPTED', `Status da proposta esperado ACCEPTED, recebido: ${accepted.status}`);
  console.log('   ✅ Proposta com status ACCEPTED');

  console.log('\n🎉 Smoke test concluído com sucesso. Fluxo crítico validado.\n');
}

run().catch((error) => {
  console.error('\n❌ Falha no smoke test:', error.message);
  process.exitCode = 1;
});
