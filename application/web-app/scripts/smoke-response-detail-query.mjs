#!/usr/bin/env node

import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const detailPagePath = path.resolve(__dirname, '../src/pages/buyer/SourcingEventDetail.tsx');

const checks = [
  {
    name: 'rehydration from query string via parser',
    pattern: /const\s+initialPreferences\s*=\s*parseResponseDetailPreferences\(searchParams\)/,
  },
  {
    name: 'status state initialized from query string',
    pattern: /useState<ResponseStatusFilter>\(initialPreferences\.status\)/,
  },
  {
    name: 'sort mode state initialized from query string',
    pattern: /useState<ResponseSortMode>\(initialPreferences\.sortBy\)/,
  },
  {
    name: 'max offer state initialized from query string',
    pattern: /useState\(initialPreferences\.maxOfferInput\)/,
  },
  {
    name: 'favorites state initialized from query string',
    pattern: /useState<string\[\]>\(initialPreferences\.favoriteResponseIds\)/,
  },
  {
    name: 'comparison state initialized from query string',
    pattern: /useState<string\[\]>\(initialPreferences\.comparisonIds\)/,
  },
  {
    name: 'sync back to URL uses setSearchParams replace',
    pattern: /setSearchParams\(nextParams,\s*\{\s*replace:\s*true\s*\}\)/,
  },
  {
    name: 'URL sync guarded to avoid endless loop',
    pattern: /if\s*\(current\s*!==\s*next\)\s*\{[\s\S]*setSearchParams\(/,
  },
];

async function main() {
  const source = await readFile(detailPagePath, 'utf8');

  let failures = 0;

  for (const check of checks) {
    const ok = check.pattern.test(source);
    if (ok) {
      console.log(`✅ ${check.name}`);
      continue;
    }

    failures += 1;
    console.error(`❌ ${check.name}`);
  }

  if (failures > 0) {
    console.error(`\nResponse detail query smoke failed with ${failures} issue(s).`);
    process.exit(1);
  }

  console.log('\nResponse detail query smoke passed.');
}

main().catch((error) => {
  console.error('Failed to run response detail query smoke:', error);
  process.exit(1);
});

