import { describe, expect, it, vi } from 'vitest';
import { acceptResponseAndRefresh } from './acceptResponseFlow';

describe('acceptResponseAndRefresh', () => {
  it('aceita proposta e refaz refresh de evento + respostas em modo silencioso', async () => {
    const acceptResponse = vi.fn().mockResolvedValue(undefined);
    const loadResponses = vi.fn().mockResolvedValue(undefined);
    const loadEvent = vi.fn().mockResolvedValue(undefined);
    const onSuccess = vi.fn();
    const onError = vi.fn();

    await acceptResponseAndRefresh(
      { acceptResponse, loadResponses, loadEvent, onSuccess, onError },
      { eventId: 'evt-1', responseId: 'resp-1' }
    );

    expect(acceptResponse).toHaveBeenCalledWith('evt-1', 'resp-1');
    expect(loadResponses).toHaveBeenCalledWith(true);
    expect(loadEvent).toHaveBeenCalledOnce();
    expect(onSuccess).toHaveBeenCalledOnce();
    expect(onError).not.toHaveBeenCalled();
  });

  it('dispara callback de erro e propaga falha quando aceite falha', async () => {
    const acceptResponse = vi.fn().mockRejectedValue(new Error('boom'));
    const loadResponses = vi.fn().mockResolvedValue(undefined);
    const loadEvent = vi.fn().mockResolvedValue(undefined);
    const onSuccess = vi.fn();
    const onError = vi.fn();

    await expect(
      acceptResponseAndRefresh(
        { acceptResponse, loadResponses, loadEvent, onSuccess, onError },
        { eventId: 'evt-1', responseId: 'resp-1' }
      )
    ).rejects.toThrow('ACCEPT_RESPONSE_FAILED');

    expect(loadResponses).not.toHaveBeenCalled();
    expect(loadEvent).not.toHaveBeenCalled();
    expect(onSuccess).not.toHaveBeenCalled();
    expect(onError).toHaveBeenCalledOnce();
  });
});
