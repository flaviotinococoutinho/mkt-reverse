import type { User } from '../services/authService';

export function resetOnboardingState() {
  localStorage.removeItem('onboarding_completed');
  localStorage.removeItem('onboarding_phone_verified');
  localStorage.removeItem('onboarding_profile_done');
  localStorage.removeItem('onboarding_tutorial_done');
}

export function isOnboardingCompleted(): boolean {
  return localStorage.getItem('onboarding_completed') === 'true';
}

export function completeOnboarding() {
  localStorage.setItem('onboarding_completed', 'true');
}

export function markPhoneVerified() {
  localStorage.setItem('onboarding_phone_verified', 'true');
}

export function markProfileDone() {
  localStorage.setItem('onboarding_profile_done', 'true');
}

export function markTutorialDone() {
  localStorage.setItem('onboarding_tutorial_done', 'true');
  completeOnboarding();
}

export function getRoleDashboardPath(user: User | null): string {
  if (!user) {
    return '/buyer/dashboard';
  }

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
