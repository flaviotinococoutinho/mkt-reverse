import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { getNextOnboardingPath } from '../../lib/onboarding';

type Role = 'buyer' | 'supplier';

export function ProtectedRoute({
  requiredRole,
  redirectTo = '/login',
}: {
  requiredRole?: Role;
  redirectTo?: string;
}) {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    const safeHome = user?.role === 'supplier' ? '/supplier/dashboard' : '/dashboard';
    return <Navigate to={safeHome} replace />;
  }

  if (requiredRole) {
    const nextOnboardingPath = getNextOnboardingPath();
    if (nextOnboardingPath) {
      return <Navigate to={nextOnboardingPath} replace />;
    }
  }

  return <Outlet />;
}
