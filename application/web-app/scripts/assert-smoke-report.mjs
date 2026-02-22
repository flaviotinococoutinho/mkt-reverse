#!/usr/bin/env node

import { readFile } from 'node:fs/promises';

const reportPath = process.env.SMOKE_REPORT_INPUT || './build/smoke-report.json';
const maxTotalMs = Number(process.env.SMOKE_MAX_TOTAL_MS || 60000);
const maxStepMs = Number(process.env.SMOKE_MAX_STEP_MS || 25000);

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

async function loadReport() {
  const raw = await readFile(reportPath, 'utf8');
  return JSON.parse(raw);
}

async function main() {
  const report = await loadReport();

  assert(report && typeof report === 'object', 'Relatório inválido: objeto JSON ausente');

  const totalDurationMs = toNumber(report.totalDurationMs);
  assert(totalDurationMs !== null, 'Relatório inválido: totalDurationMs ausente ou não numérico');
  assert(
    totalDurationMs <= maxTotalMs,
    `SLA violado: totalDurationMs=${totalDurationMs}ms > limite=${maxTotalMs}ms`
  );

  const steps = Array.isArray(report.steps) ? report.steps : [];
  assert(steps.length > 0, 'Relatório inválido: steps ausente/vazio');

  for (const step of steps) {
    assert(step.status === 'OK', `Etapa falhou: ${step.name ?? 'desconhecida'} (status=${step.status})`);
    const durationMs = toNumber(step.durationMs);
    assert(durationMs !== null, `Etapa com duration inválido: ${step.name ?? 'desconhecida'}`);
    assert(
      durationMs <= maxStepMs,
      `SLA violado na etapa "${step.name ?? 'desconhecida'}": ${durationMs}ms > limite=${maxStepMs}ms`
    );
  }

  const eventStatus = report?.ids?.eventStatus;
  const responseStatus = report?.ids?.responseStatus;

  assert(
    eventStatus === 'AWARDED',
    `Status final inválido para evento: esperado AWARDED, recebido ${String(eventStatus)}`
  );
  assert(
    responseStatus === 'ACCEPTED',
    `Status final inválido para proposta: esperado ACCEPTED, recebido ${String(responseStatus)}`
  );

  console.log('✅ Smoke report validado com sucesso.');
  console.log(`   Arquivo: ${reportPath}`);
  console.log(`   totalDurationMs: ${totalDurationMs}ms (limite: ${maxTotalMs}ms)`);
  console.log(`   Etapas validadas: ${steps.length} (limite por etapa: ${maxStepMs}ms)`);
  console.log(`   Status finais: event=${eventStatus}, response=${responseStatus}`);
}

main().catch((error) => {
  console.error('❌ Falha ao validar smoke report:', error.message);
  process.exit(1);
});

