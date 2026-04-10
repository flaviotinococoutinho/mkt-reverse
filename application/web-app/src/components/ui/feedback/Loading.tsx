import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  text?: string;
  fullPage?: boolean;
}

const sizes = {
  sm: 'w-4 h-4',
  md: 'w-8 h-8',
  lg: 'w-12 h-12',
};

export const Loading: React.FC<LoadingProps> = ({ size = 'md', text, fullPage = false }) => {
  const content = (
    <div className="flex flex-col items-center gap-3">
      <Loader2 className={`${sizes[size]} animate-spin text-blue-600`} />
      {text && <p className="text-gray-600 text-sm">{text}</p>}
    </div>
  );

  if (fullPage) {
    return (
      <div className="min-h-[400px] flex items-center justify-center">
        {content}
      </div>
    );
  }

  return content;
};

export const LoadingOverlay: React.FC<{ show: boolean; text?: string }> = ({ show, text }) => {
  if (!show) return null;

  return (
    <div className="absolute inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-10 rounded-lg">
      <Loading text={text} />
    </div>
  );
};

interface SkeletonProps {
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ className = '' }) => (
  <div className={`animate-pulse bg-gray-200 rounded ${className}`} />
);

export const CardSkeleton: React.FC = () => (
  <div className="bg-white rounded-lg shadow p-4 space-y-3">
    <Skeleton className="h-4 w-3/4" />
    <Skeleton className="h-3 w-1/2" />
    <div className="flex gap-2">
      <Skeleton className="h-6 w-16 rounded-full" />
      <Skeleton className="h-6 w-20 rounded-full" />
    </div>
  </div>
);

export const ListSkeleton: React.FC<{ count?: number }> = ({ count = 3 }) => (
  <div className="space-y-3">
    {Array.from({ length: count }).map((_, i) => (
      <CardSkeleton key={i} />
    ))}
  </div>
);