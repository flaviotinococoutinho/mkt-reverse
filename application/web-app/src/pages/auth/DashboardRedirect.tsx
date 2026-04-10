import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { getRoleDashboardPath } from '../../lib/onboarding';

export default function DashboardRedirect() {
  const { user } = useAuth();
  const targetPath = getRoleDashboardPath(user);

  return <Navigate to={targetPath} replace />;
}
