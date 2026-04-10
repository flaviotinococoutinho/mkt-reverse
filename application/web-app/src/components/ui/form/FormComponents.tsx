import React from 'react';
import { UseFormRegister, FieldErrors, FieldName } from 'react-hook-form';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const FormInput = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, className = '', id, ...props }, ref) => {
    const inputId = id || props.name;

    return (
      <div className="space-y-1">
        {label && (
          <label htmlFor={inputId} className="block text-sm font-medium text-zinc-300">
            {label}
            {props.required && <span className="text-red-400 ml-1">*</span>}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={`
            w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
            focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
            transition-colors
            ${error 
              ? 'border-red-500 focus:ring-red-500' 
              : 'border-stroke hover:border-zinc-600'
            }
            ${className}
          `}
          aria-invalid={!!error}
          aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
          {...props}
        />
        {error && (
          <p id={`${inputId}-error`} className="text-sm text-red-400" role="alert">
            {error}
          </p>
        )}
        {helperText && !error && (
          <p id={`${inputId}-helper`} className="text-sm text-zinc-500">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

FormInput.displayName = 'FormInput';

// Select component
interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  options: { value: string; label: string }[];
  placeholder?: string;
}

export const FormSelect = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, options, placeholder, className = '', id, ...props }, ref) => {
    const selectId = id || props.name;

    return (
      <div className="space-y-1">
        {label && (
          <label htmlFor={selectId} className="block text-sm font-medium text-zinc-300">
            {label}
            {props.required && <span className="text-red-400 ml-1">*</span>}
          </label>
        )}
        <select
          ref={ref}
          id={selectId}
          className={`
            w-full px-3 py-2 bg-ink border rounded-md text-zinc-200
            focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
            transition-colors
            ${error 
              ? 'border-red-500 focus:ring-red-500' 
              : 'border-stroke hover:border-zinc-600'
            }
            ${className}
          `}
          aria-invalid={!!error}
          {...props}
        >
          {placeholder && (
            <option value="" className="text-zinc-500">
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        {error && (
          <p id={`${selectId}-error`} className="text-sm text-red-400" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

FormSelect.displayName = 'FormSelect';

// Textarea component
interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

export const FormTextarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, className = '', id, ...props }, ref) => {
    const textareaId = id || props.name;

    return (
      <div className="space-y-1">
        {label && (
          <label htmlFor={textareaId} className="block text-sm font-medium text-zinc-300">
            {label}
            {props.required && <span className="text-red-400 ml-1">*</span>}
          </label>
        )}
        <textarea
          ref={ref}
          id={textareaId}
          className={`
            w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
            focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
            transition-colors resize-y min-h-[100px]
            ${error 
              ? 'border-red-500 focus:ring-red-500' 
              : 'border-stroke hover:border-zinc-600'
            }
            ${className}
          `}
          aria-invalid={!!error}
          {...props}
        />
        {error && (
          <p id={`${textareaId}-error`} className="text-sm text-red-400" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

FormTextarea.displayName = 'FormTextarea';

// Checkbox component
interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string;
  error?: string;
}

export const FormCheckbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ label, error, className = '', id, ...props }, ref) => {
    const checkboxId = id || props.name;

    return (
      <div className="flex items-start gap-2">
        <input
          ref={ref}
          type="checkbox"
          id={checkboxId}
          className={`
            mt-1 w-4 h-4 rounded border-stroke bg-ink text-citrus
            focus:ring-2 focus:ring-citrus focus:ring-offset-0
            ${error ? 'border-red-500' : ''}
            ${className}
          `}
          aria-invalid={!!error}
          {...props}
        />
        <label htmlFor={checkboxId} className="text-sm text-zinc-300">
          {label}
        </label>
        {error && (
          <p className="text-sm text-red-400" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

FormCheckbox.displayName = 'FormCheckbox';

// Radio Group component
interface RadioOption {
  value: string;
  label: string;
  description?: string;
}

interface RadioGroupProps {
  name: string;
  label?: string;
  options: RadioOption[];
  error?: string;
  value?: string;
  onChange?: (value: string) => void;
}

export const FormRadioGroup: React.FC<RadioGroupProps> = ({
  name,
  label,
  options,
  error,
  value,
  onChange,
}) => {
  return (
    <div className="space-y-2">
      {label && (
        <label className="block text-sm font-medium text-zinc-300">
          {label}
        </label>
      )}
      <div className="space-y-2">
        {options.map((opt) => (
          <label
            key={opt.value}
            className={`
              flex items-start gap-3 p-3 rounded-lg border cursor-pointer
              transition-colors
              ${value === opt.value 
                ? 'border-citrus bg-citrus/10' 
                : 'border-stroke hover:border-zinc-600'
              }
            `}
          >
            <input
              type="radio"
              name={name}
              value={opt.value}
              checked={value === opt.value}
              onChange={(e) => onChange?.(e.target.value)}
              className="mt-1 w-4 h-4 text-citrus border-zinc-500 bg-ink focus:ring-citrus"
            />
            <div>
              <span className="text-sm text-zinc-200">{opt.label}</span>
              {opt.description && (
                <p className="text-xs text-zinc-500 mt-0.5">{opt.description}</p>
              )}
            </div>
          </label>
        ))}
      </div>
      {error && (
        <p className="text-sm text-red-400" role="alert">
          {error}
        </p>
      )}
    </div>
  );
};

// Form Field wrapper for consistent spacing
interface FormFieldProps {
  children: React.ReactNode;
  className?: string;
}

export const FormField: React.FC<FormFieldProps> = ({ children, className = '' }) => (
  <div className={className}>{children}</div>
);

// Form Actions (submit button area)
interface FormActionsProps {
  children: React.ReactNode;
  className?: string;
}

export const FormActions: React.FC<FormActionsProps> = ({ children, className = '' }) => (
  <div className={`flex gap-3 justify-end pt-4 ${className}`}>{children}</div>
);