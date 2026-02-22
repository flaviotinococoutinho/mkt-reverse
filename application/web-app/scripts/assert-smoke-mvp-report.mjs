#!/usr/bin/env node

import { readFile } from 'node:fs/promises';

const reportPath = process.env.SMOKE_MVP_REPORT_INPUT || './build/smoke-mvp-report.json';
const maxTotalMs = Number(process.env.SMOKE_MVP_MAX_TOTAL_MS || 90000);
const maxStepMs = Number(process.env.SMOKE_MVP_MAX_STEP_MS || 45000);

const requiredChecks = [
  'API critical flow (auth + buyer/supplier)',
  'UI route smoke',
  'UI response detail query hydration smoke',
  'Session invalidation guardrail smoke',
];

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

  assert(report && typeof report === 'object', 'Relatório MVP inválido: objeto JSON ausente');

  const totalDurationMs = toNumber(report.totalDurationMs);
  assert(totalDurationMs !== null, 'Relatório MVP inválido: totalDurationMs ausente ou não numérico');
  assert(
    totalDurationMs <= maxTotalMs,
    `SLA MVP violado: totalDurationMs=${totalDurationMs}ms > limite=${maxTotalMs}ms`
  );

  const steps = Array.isArray(report.steps) ? report.steps : [];
  assert(steps.length > 0, 'Relatório MVP inválido: steps ausente/vazio');

  const stepChecks = new Set();

  for (const step of steps) {
    const durationMs = toNumber(step.durationMs);
    const checkName = typeof step.check === 'string' ? step.check : null;

    assert(checkName, `Etapa inválida: campo \"check\" ausente (step=${JSON.stringify(step)})`);
    assert(durationMs !== null, `Etapa com duration inválido: ${checkName}`);
    assert(
      durationMs <= maxStepMs,
      `SLA MVP violado na etapa \"${checkName}\": ${durationMs}ms > limite=${maxStepMs}ms`
    );

    stepChecks.add(checkName);
  }

  for (const requiredCheck of requiredChecks) {
    assert(
      stepChecks.has(requiredCheck),
      `Relatório MVP inválido: etapa obrigatória ausente (${requiredCheck})`
    );
  }

  console.log('✅ Smoke MVP report validado com sucesso.');
  console.log(`   Arquivo: ${reportPath}`);
  console.log(`   totalDurationMs: ${totalDurationMs}ms (limite: ${maxTotalMs}ms)`);
  console.log(`   Etapas validadas: ${steps.length} (limite por etapa: ${maxStepMs}ms)`);
}

main().catch((error) => {
  console.error('❌ Falha ao validar smoke MVP report:', error.message);
  process.exit(1);
});
