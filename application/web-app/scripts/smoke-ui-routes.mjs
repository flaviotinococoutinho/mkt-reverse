#!/usr/bin/env node

import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const appFilePath = path.resolve(__dirname, '../src/App.tsx');

const checks = [
  {
    name: 'Public route: landing',
    pattern: /<Route\s+path="\/"\s+element={<Landing\s*\/>}\s*\/>/,
  },
  {
    name: 'Public route: login',
    pattern: /<Route\s+path="\/login"\s+element={<Login\s*\/>}\s*\/>/,
  },
  {
    name: 'Public route: register',
    pattern: /<Route\s+path="\/register"\s+element={<Register\s*\/>}\s*\/>/,
  },
  {
    name: 'Public route: verify phone',
    pattern: /<Route\s+path="\/verify-phone"\s+element={<PhoneVerification\s*\/>}\s*\/>/,
  },
  {
    name: 'Public route: support',
    pattern: /<Route\s+path="\/support"\s+element={<Support\s*\/>}\s*\/>/,
  },
  {
    name: 'Shared auth route: /dashboard redirect',
    pattern: /<Route\s+path="\/dashboard"\s+element={<DashboardRedirect\s*\/>}\s*\/>/,
  },
  {
    name: 'Shared auth route: onboarding profile',
    pattern: /<Route\s+path="\/onboarding\/profile"\s+element={<ProfileSetup\s*\/>}\s*\/>/,
  },
  {
    name: 'Shared auth route: onboarding tutorial',
    pattern: /<Route\s+path="\/onboarding\/tutorial"\s+element={<OnboardingTutorial\s*\/>}\s*\/>/,
  },
  {
    name: 'Buyer route: /buyer/dashboard',
    pattern: /<Route\s+path="\/buyer\/dashboard"\s+element={<BuyerDashboard\s*\/>}\s*\/>/,
  },
  {
    name: 'Buyer route: /create-request',
    pattern: /<Route\s+path="\/create-request"\s+element={<CreateRequest\s*\/>}\s*\/>/,
  },
  {
    name: 'Buyer route: /sourcing-events/:id',
    pattern: /<Route\s+path="\/sourcing-events\/:id"\s+element={<SourcingEventDetail\s*\/>}\s*\/>/,
  },
  {
    name: 'Supplier route: /supplier/dashboard',
    pattern: /<Route\s+path="\/supplier\/dashboard"\s+element={<SupplierDashboard\s*\/>}\s*\/>/,
  },
  {
    name: 'Supplier route: /supplier/opportunities',
    pattern: /<Route\s+path="\/supplier\/opportunities"\s+element={<OpportunitiesPage\s*\/>}\s*\/>/,
  },
  {
    name: 'Supplier route: /supplier/submit-proposal/:id',
    pattern: /<Route\s+path="\/supplier\/submit-proposal\/:id"\s+element={<SubmitProposal\s*\/>}\s*\/>/,
  },
  {
    name: 'Supplier route: /supplier/opportunities/:id',
    pattern: /<Route\s+path="\/supplier\/opportunities\/:id"\s+element={<OpportunityDetail\s*\/>}\s*\/>/,
  },
  {
    name: 'Fallback route exists',
    pattern: /<Route\s+path="\*"\s+element={<Landing\s*\/>}\s*\/>/,
  },
  {
    name: 'ProtectedRoute shared auth block exists',
    pattern: /<Route\s+element={<ProtectedRoute\s*\/>}>/,
  },
  {
    name: 'ProtectedRoute buyer block exists',
    pattern: /<Route\s+element={<ProtectedRoute\s+requiredRole="buyer"\s*\/>}>/,
  },
  {
    name: 'ProtectedRoute supplier block exists',
    pattern: /<Route\s+element={<ProtectedRoute\s+requiredRole="supplier"\s*\/>}>/,
  },
];

async function main() {
  const appSource = await readFile(appFilePath, 'utf8');

  let failures = 0;

  for (const check of checks) {
    const ok = check.pattern.test(appSource);
    if (ok) {
      console.log(`✅ ${check.name}`);
      continue;
    }

    failures += 1;
    console.error(`❌ ${check.name}`);
  }

  if (failures > 0) {
    console.error(`\nUI route smoke failed with ${failures} issue(s).`);
    process.exit(1);
  }

  console.log('\nUI route smoke passed.');
}

main().catch((error) => {
  console.error('Failed to run UI route smoke:', error);
  process.exit(1);
});
