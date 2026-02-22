import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';

const Login = lazy(() => import('./pages/auth/Login'));
const Register = lazy(() => import('./pages/auth/Register'));
const PhoneVerification = lazy(() => import('./pages/auth/PhoneVerification'));
const ProfileSetup = lazy(() => import('./pages/auth/ProfileSetup'));
const OnboardingTutorial = lazy(() => import('./pages/auth/OnboardingTutorial'));
const Landing = lazy(() => import('./pages/Landing'));
const BuyerDashboard = lazy(() => import('./pages/buyer/BuyerDashboard'));
const CreateRequest = lazy(() => import('./pages/buyer/CreateRequest'));
const SourcingEventDetail = lazy(() => import('./pages/buyer/SourcingEventDetail'));
const SupplierDashboard = lazy(() => import('./pages/supplier/SupplierDashboard'));
const OpportunitiesPage = lazy(() => import('./pages/supplier/OpportunitiesPage'));
const OpportunityDetail = lazy(() => import('./pages/supplier/OpportunityDetail'));
const SubmitProposal = lazy(() => import('./pages/supplier/SubmitProposal'));
const Support = lazy(() => import('./pages/Support'));

function App() {
  return (
    <AuthProvider>
      <Router>
        <Suspense
          fallback={(
            <div className="min-h-screen bg-ink text-zinc-300 flex items-center justify-center font-sans">
              Carregando tela...
            </div>
          )}
        >
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify-phone" element={<PhoneVerification />} />
            <Route path="/support" element={<Support />} />

            {/* Shared authenticated onboarding routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/onboarding/profile" element={<ProfileSetup />} />
              <Route path="/onboarding/tutorial" element={<OnboardingTutorial />} />
            </Route>

            {/* Buyer Routes */}
            <Route element={<ProtectedRoute requiredRole="buyer" />}>
              <Route path="/buyer/dashboard" element={<BuyerDashboard />} />
              <Route path="/dashboard" element={<BuyerDashboard />} />
              <Route path="/create-request" element={<CreateRequest />} />
              <Route path="/sourcing-events/:id" element={<SourcingEventDetail />} />
            </Route>

            {/* Supplier Routes */}
            <Route element={<ProtectedRoute requiredRole="supplier" />}>
              <Route path="/supplier/dashboard" element={<SupplierDashboard />} />
              <Route path="/supplier/opportunities" element={<OpportunitiesPage />} />
              <Route path="/supplier/opportunities/:id" element={<OpportunityDetail />} />
              <Route path="/supplier/submit-proposal/:id" element={<SubmitProposal />} />
            </Route>
          </Routes>
        </Suspense>
      </Router>
    </AuthProvider>
  );
}

export default App;
