import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';
import { Button } from './Button';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="min-h-[400px] flex items-center justify-center p-8">
          <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle className="w-8 h-8 text-red-600" />
            </div>
            
            <h2 className="text-xl font-semibold text-gray-900 mb-2">
              Algo deu errado
            </h2>
            
            <p className="text-gray-600 mb-6">
              Encontramos um problema inesperado. Por favor, tente novamente.
            </p>

            {this.state.error && (
              <div className="bg-gray-50 rounded-md p-3 mb-6 text-left">
                <p className="text-xs text-gray-500 font-mono break-all">
                  {this.state.error.message}
                </p>
              </div>
            )}

            <div className="flex gap-3 justify-center">
              <Button 
                onClick={this.handleReset}
                className="flex items-center gap-2"
              >
                <RefreshCw className="w-4 h-4" />
                Tentar novamente
              </Button>
              
              <Button 
                variant="secondary"
                onClick={() => window.location.href = '/'}
                className="flex items-center gap-2"
              >
                <Home className="w-4 h-4" />
                Ir para Home
              </Button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Hook for components to trigger error boundary manually
export const useErrorBoundary = () => {
  const [error, setError] = React.useState<Error | null>(null);

  const resetError = () => setError(null);

  React.useEffect(() => {
    if (error) {
      throw error;
    }
  }, [error]);

  return { error, setError, resetError };
};