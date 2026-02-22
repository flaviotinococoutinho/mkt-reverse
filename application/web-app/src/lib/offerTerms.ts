export const CONDITION_LABELS = {
  NEW: 'Novo',
  USED: 'Usado',
  REFURBISHED: 'Recondicionado',
  MADE_TO_ORDER: 'Sob Encomenda',
} as const;

export const SHIPPING_MODE_LABELS = {
  SELLER: 'Vendedor (Frete Grátis)',
  BUYER: 'Comprador (Frete por conta)',
  THIRD_PARTY: 'Terceiros (Frete a combinar)',
} as const;

export type ConditionCode = keyof typeof CONDITION_LABELS;
export type ShippingModeCode = keyof typeof SHIPPING_MODE_LABELS;

export function getConditionLabel(condition?: string): string {
  if (!condition) return 'Não informado';
  return CONDITION_LABELS[condition as ConditionCode] ?? condition;
}

export function getShippingModeLabel(mode?: string): string {
  if (!mode) return 'Não informado';
  return SHIPPING_MODE_LABELS[mode as ShippingModeCode] ?? mode;
}

export const CONDITION_OPTIONS = Object.entries(CONDITION_LABELS).map(([value, label]) => ({
  value,
  label,
}));

export const SHIPPING_MODE_OPTIONS = Object.entries(SHIPPING_MODE_LABELS).map(([value, label]) => ({
  value,
  label,
}));
