#!/usr/bin/env node

import { spawn } from 'node:child_process';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';

const rootEnv = {
  ...process.env,
  SMOKE_AUTH: process.env.SMOKE_AUTH ?? '1',
};

const checks = [
  {
    name: 'API critical flow (auth + buyer/supplier)',
    command: 'node',
    args: ['./scripts/smoke-flow.mjs'],
    env: rootEnv,
  },
  {
    name: 'UI route smoke',
    command: 'node',
    args: ['./scripts/smoke-ui-routes.mjs'],
    env: rootEnv,
  },
  {
    name: 'UI response detail query hydration smoke',
    command: 'node',
    args: ['./scripts/smoke-response-detail-query.mjs'],
    env: rootEnv,
  },
  {
    name: 'Session invalidation guardrail smoke',
    command: 'node',
    args: ['./scripts/smoke-session-invalid.mjs'],
    env: rootEnv,
  },
];

function runCheck(check) {
  return new Promise((resolve, reject) => {
    const startedAt = Date.now();
    console.log(`\n▶ ${check.name}`);

    const child = spawn(check.command, check.args, {
      env: check.env,
      stdio: 'inherit',
      shell: false,
    });

    child.on('error', (error) => {
      reject(new Error(`${check.name} falhou ao iniciar: ${error.message}`));
    });

    child.on('close', (code) => {
      const durationMs = Date.now() - startedAt;
      if (code !== 0) {
        reject(new Error(`${check.name} falhou com código ${code} em ${durationMs}ms`));
        return;
      }

      console.log(`✅ ${check.name} concluído em ${durationMs}ms`);
      resolve({ name: check.name, durationMs });
    });
  });
}

async function maybeWriteReport(report) {
  const reportPathInput = process.env.SMOKE_MVP_REPORT_PATH;
  if (!reportPathInput) {
    return;
  }

  const reportPath = resolve(reportPathInput);
  await mkdir(dirname(reportPath), { recursive: true });
  await writeFile(reportPath, `${JSON.stringify(report, null, 2)}\n`, 'utf8');
  console.log(`🧾 Relatório MVP salvo em: ${reportPath}`);
}

async function run() {
  const suiteStartedAt = Date.now();
  const summary = [];

  for (const check of checks) {
    const result = await runCheck(check);
    summary.push(result);
  }

  const totalDurationMs = Date.now() - suiteStartedAt;
  const report = {
    generatedAt: new Date().toISOString(),
    totalDurationMs,
    steps: summary.map((item, index) => ({
      step: index + 1,
      check: item.name,
      durationMs: item.durationMs,
    })),
  };

  console.log('\n📦 Smoke MVP consolidado');
  console.table(report.steps);
  console.log(`Tempo total: ${totalDurationMs}ms`);

  await maybeWriteReport(report);
}

run().catch((error) => {
  console.error(`\n❌ Smoke MVP falhou: ${error.message}`);
  process.exitCode = 1;
});
