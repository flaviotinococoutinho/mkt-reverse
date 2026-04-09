import { useState, useCallback } from 'react';
import { UseFormReturn, SubmitHandler } from 'react-hook-form';
import { ZodError } from 'zod';
import { useToast } from '../../ui/feedback';

interface UseFormWithValidationOptions<T> {
  onSubmit: (data: T) => Promise<void>;
  showSuccessToast?: boolean;
  successMessage?: string;
}

export function useFormWithValidation<T>(
  methods: UseFormReturn<T>,
  options: UseFormWithValidationOptions<T>
) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { error: showError } = useToast();

  const handleSubmit = useCallback(
    async (data: T) => {
      setIsSubmitting(true);
      try {
        await options.onSubmit(data);
        
        if (options.showSuccessToast && options.successMessage) {
          // Success toast handled by caller if needed
        }
      } catch (err) {
        if (err instanceof ZodError) {
          // Zod validation errors are handled by react-hook-form
          return;
        }

        if (err instanceof Error) {
          showError('Erro', err.message);
        } else {
          showError('Erro', 'Ocorreu um erro inesperado');
        }
      } finally {
        setIsSubmitting(false);
      }
    },
    [methods, options, showError]
  );

  return {
    isSubmitting,
    handleSubmit: methods.handleSubmit(handleSubmit),
  };
}

// Helper to convert Zod errors to react-hook-form errors
export function mapZodErrors<T>(error: ZodError): Partial<Record<keyof T, { message: string }>> {
  const errors: Partial<Record<keyof T, { message: string }>> = {};
  
  error.errors.forEach((err) => {
    const path = err.path[0] as keyof T;
    if (path) {
      errors[path] = { message: err.message };
    }
  });
  
  return errors;
}

// Form validation helper
export function getFieldError<T>(errors: Partial<Record<keyof T, { message?: string }>>, field: keyof T): string | undefined {
  return errors[field]?.message;
}