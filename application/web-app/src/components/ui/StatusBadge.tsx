import type { ComponentType } from 'react';
import { CheckCircle, Clock, FileText, Users, XCircle } from 'lucide-react';
import { cn } from '../../lib/cn';
import { getStatusLabel } from '../../lib/status';

type StatusBadgeProps = {
  status: string;
  className?: string;
  size?: 'sm' | 'md';
};

const STATUS_STYLES: Record<string, string> = {
  PUBLISHED: 'text-citrus bg-citrus/10',
  SUBMITTED: 'text-citrus bg-citrus/10',
  ACCEPTED: 'text-mint bg-mint/10',
  REJECTED: 'text-rose-300 bg-rose-300/10',
  WITHDRAWN: 'text-zinc-400 bg-zinc-400/10',
  IN_PROGRESS: 'text-mint bg-mint/10',
  AWARDED: 'text-mint bg-mint/10',
  CLOSED: 'text-zinc-500 bg-zinc-500/10',
  CANCELLED: 'text-rose-300 bg-rose-300/10',
};

const STATUS_ICONS: Record<string, ComponentType<{ className?: string }>> = {
  PUBLISHED: Clock,
  SUBMITTED: Clock,
  ACCEPTED: CheckCircle,
  REJECTED: XCircle,
  WITHDRAWN: XCircle,
  IN_PROGRESS: Users,
  AWARDED: CheckCircle,
  CLOSED: FileText,
  CANCELLED: XCircle,
};

export function StatusBadge({ status, className, size = 'sm' }: StatusBadgeProps) {
  const Icon = STATUS_ICONS[status] ?? Clock;

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-md font-medium',
        size === 'sm' ? 'px-2 py-1 text-xs' : 'px-3 py-1 text-sm',
        STATUS_STYLES[status] ?? 'text-zinc-500 bg-zinc-500/10',
        className
      )}
    >
      <Icon className={size === 'sm' ? 'h-4 w-4' : 'h-4 w-4'} />
      {getStatusLabel(status)}
    </span>
  );
}
