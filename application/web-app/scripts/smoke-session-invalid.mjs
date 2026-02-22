#!/usr/bin/env node

import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const apiFilePath = path.resolve(__dirname, '../src/services/api.ts');
const authContextPath = path.resolve(__dirname, '../src/context/AuthContext.tsx');
const loginPagePath = path.resolve(__dirname, '../src/pages/auth/Login.tsx');

const checks = [
  {
    name: 'API interceptor limpa token/user em 401/403',
    file: 'api',
    pattern: /if \(status === 401 \|\| status === 403\)[\s\S]*localStorage\.removeItem\('token'\)[\s\S]*localStorage\.removeItem\('user'\)/,
  },
  {
    name: 'API interceptor dispara evento auth:session-invalid',
    file: 'api',
    pattern: /window\.dispatchEvent\([\s\S]*'auth:session-invalid'/,
  },
  {
    name: 'AuthProvider escuta evento auth:session-invalid e limpa user',
    file: 'authContext',
    pattern: /window\.addEventListener\('auth:session-invalid'[\s\S]*setUser\(null\)/,
  },
  {
    name: 'Login consome aviso auth.notice após sessão inválida',
    file: 'login',
    pattern: /sessionStorage\.getItem\('auth\.notice'\)|sessionStorage\.getItem\(AUTH_NOTICE_KEY\)/,
  },
  {
    name: 'Login remove auth.notice após leitura',
    file: 'login',
    pattern: /sessionStorage\.removeItem\('auth\.notice'\)|sessionStorage\.removeItem\(AUTH_NOTICE_KEY\)/,
  },
];

async function main() {
  const [apiSource, authContextSource, loginSource] = await Promise.all([
    readFile(apiFilePath, 'utf8'),
    readFile(authContextPath, 'utf8'),
    readFile(loginPagePath, 'utf8'),
  ]);

  const sources = {
    api: apiSource,
    authContext: authContextSource,
    login: loginSource,
  };

  let failures = 0;

  for (const check of checks) {
    const ok = check.pattern.test(sources[check.file]);
    if (ok) {
      console.log(`✅ ${check.name}`);
      continue;
    }

    failures += 1;
    console.error(`❌ ${check.name}`);
  }

  if (failures > 0) {
    console.error(`\nSession-invalid smoke failed with ${failures} issue(s).`);
    process.exit(1);
  }

  console.log('\nSession-invalid smoke passed.');
}

main().catch((error) => {
  console.error('Failed to run session-invalid smoke:', error);
  process.exit(1);
});
