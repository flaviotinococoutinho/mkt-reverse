import { describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { PostAcceptanceSummary } from './PostAcceptanceSummary';

describe('PostAcceptanceSummary', () => {
  it('renderiza CTAs e checklist base após aceite', () => {
    const html = renderToStaticMarkup(
      <PostAcceptanceSummary
        acceptedResponse={{ id: '1234567890', offerCents: 150000 }}
        eventStatus="IN_PROGRESS"
        onGoToDashboard={vi.fn()}
        onCreateRequest={vi.fn()}
      />
    );

    expect(html).toContain('Proposta vencedora definida');
    expect(html).toContain('Ir para Dashboard');
    expect(html).toContain('Publicar nova solicitação');
    expect(html).toContain('Confirmar canal de contato com o vendedor');
    expect(html).toContain('Alinhar prazo de entrega e marcos principais.');
    expect(html).not.toContain('Status da solicitação:');
  });

  it('inclui mensagem de encerramento e item extra quando status está fechado', () => {
    const html = renderToStaticMarkup(
      <PostAcceptanceSummary
        acceptedResponse={{ id: '1234567890', offerCents: 150000 }}
        eventStatus="AWARDED"
        onGoToDashboard={vi.fn()}
        onCreateRequest={vi.fn()}
      />
    );

    expect(html).toContain('Status da solicitação: AWARDED. Esta negociação foi encerrada.');
    expect(html).toContain('Solicitação encerrada: iniciar nova solicitação se ainda houver demanda.');
  });
});
