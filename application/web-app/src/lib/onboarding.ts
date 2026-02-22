import type { User } from '../services/authService';

export function resetOnboardingState() {
  localStorage.removeItem('onboarding_completed');
}

export function isOnboardingCompleted(): boolean {
  return localStorage.getItem('onboarding_completed') === 'true';
}

export function completeOnboarding() {
  localStorage.setItem('onboarding_completed', 'true');
}

export function getRoleDashboardPath(user: User): string {
  if (user.role === 'supplier') {
    return '/supplier/dashboard';
  }
  return '/buyer/dashboard';
}

export function getNextOnboardingPath(): string | null {
  if (isOnboardingCompleted()) return null;
  // Para o MVP, assumimos que se o user está logado e não completou, vai para /onboarding/profile
  return '/onboarding/profile';
}
