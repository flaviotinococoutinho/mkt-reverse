import { describe, expect, it } from 'vitest';
import { getRoleDashboardPath } from './onboarding';

describe('getRoleDashboardPath', () => {
  it('retorna dashboard buyer para usuário buyer', () => {
    const path = getRoleDashboardPath({
      id: '1',
      name: 'Buyer User',
      email: 'buyer@example.com',
      role: 'buyer',
      tenantId: 'tenant-a',
    });

    expect(path).toBe('/buyer/dashboard');
  });

  it('retorna dashboard supplier para usuário supplier', () => {
    const path = getRoleDashboardPath({
      id: '2',
      name: 'Supplier User',
      email: 'supplier@example.com',
      role: 'supplier',
      tenantId: 'tenant-a',
    });

    expect(path).toBe('/supplier/dashboard');
  });

  it('usa fallback buyer quando usuário é nulo', () => {
    expect(getRoleDashboardPath(null)).toBe('/buyer/dashboard');
  });
});
