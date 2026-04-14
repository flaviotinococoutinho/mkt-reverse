import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('./api', () => ({
  default: {
    post: vi.fn(),
  },
  setTokens: vi.fn(),
  clearTokens: vi.fn(),
  setUser: vi.fn(),
  getAccessToken: vi.fn(),
}));

import api, { setTokens, clearTokens, setUser } from './api';
import { authService } from './authService';

function createStorageMock() {
  const store = new Map<string, string>();
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => {
      store.set(key, value);
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
    clear: () => {
      store.clear();
    },
  };
}

describe('authService', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', createStorageMock());
    vi.clearAllMocks();
  });

  it('converte telefone em email MVP no login e persiste sessão', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        accessToken: 'jwt-123',
        refreshToken: 'refresh-123',
        user: {
          id: 'u-1',
          name: 'Flavio',
          email: '5511999999999@queroja.mvp',
          role: 'buyer',
          tenantId: 'tenant-1',
        },
      },
    });

    const result = await authService.login({
      identifier: ' (11) 99999-9999 ',
      password: 'secret',
    });

    expect(api.post).toHaveBeenCalledWith('/auth/login', {
      email: '11999999999@queroja.mvp',
      password: 'secret',
    });
    expect(result.accessToken).toBe('jwt-123');
    expect(setTokens).toHaveBeenCalledWith('jwt-123', 'refresh-123');
  });

  it('envia payload de registro sem email explícito e com userType correto', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        accessToken: 'jwt-456',
        refreshToken: 'refresh-456',
        user: {
          id: 'u-2',
          name: 'Empresa XPTO',
          email: '5511988887777@queroja.mvp',
          role: 'supplier',
          tenantId: 'tenant-2',
        },
      },
    });

    await authService.register({
      name: 'Empresa XPTO LTDA',
      phone: '+55 (11) 98888-7777',
      password: 'secret',
      role: 'supplier',
      documentNumber: '12345678000199',
      documentType: 'CNPJ',
    });

    expect(api.post).toHaveBeenCalledWith('/auth/register', {
      email: '5511988887777@queroja.mvp',
      password: 'secret',
      firstName: 'Empresa',
      lastName: 'XPTO LTDA',
      displayName: 'Empresa XPTO LTDA',
      documentNumber: '12345678000199',
      documentType: 'CNPJ',
      userType: 'SUPPLIER',
    });
  });

  it('usa fallback de sobrenome MVP quando nome possui uma única palavra', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        accessToken: 'jwt-789',
        refreshToken: 'refresh-789',
        user: {
          id: 'u-3',
          name: 'Flavio',
          email: '5511911112222@queroja.mvp',
          role: 'buyer',
          tenantId: 'tenant-3',
        },
      },
    });

    await authService.register({
      name: 'Flavio',
      phone: '+55 (11) 91111-2222',
      password: 'secret',
      role: 'buyer',
      documentNumber: '12345678901',
      documentType: 'CPF',
    });

    expect(api.post).toHaveBeenCalledWith('/auth/register', expect.objectContaining({
      firstName: 'Flavio',
      lastName: 'MVP',
      userType: 'BUYER',
    }));
  });
});
