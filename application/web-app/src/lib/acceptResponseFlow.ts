export interface AcceptResponseFlowDeps {
  acceptResponse: (eventId: string, responseId: string) => Promise<void>;
  loadResponses: (silent?: boolean) => Promise<void>;
  loadEvent: () => Promise<void>;
  onSuccess: () => void;
  onError: () => void;
}

export async function acceptResponseAndRefresh(
  deps: AcceptResponseFlowDeps,
  params: { eventId: string; responseId: string }
): Promise<void> {
  try {
    await deps.acceptResponse(params.eventId, params.responseId);
    await Promise.all([deps.loadResponses(true), deps.loadEvent()]);
    deps.onSuccess();
  } catch {
    deps.onError();
    throw new Error('ACCEPT_RESPONSE_FAILED');
  }
}
