import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

export function Input({
  label,
  type = 'text',
  error,
  helperText,
  icon: Icon,
  className = '',
  containerClassName = '',
  id,
  ...props
}) {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';
  const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className={`w-full ${containerClassName}`}>
      {label && (
        <label htmlFor={inputId} className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
          {label}
        </label>
      )}
      <div className="relative rounded-lg shadow-sm">
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#89766C]">
            <Icon className="h-4 w-4" />
          </div>
        )}
        <input
          id={inputId}
          type={inputType}
          className={`w-full rounded-lg bg-white border ${
            error ? 'border-[#B85C55] focus:ring-[#B85C55]' : 'border-[#EAD8C3] focus:border-[#702C37] focus:ring-[#702C37]'
          } px-3.5 py-2.5 text-sm text-[#342522] placeholder-[#89766C]/60 focus:outline-none focus:ring-1 transition-all duration-150 ${
            Icon ? 'pl-9' : ''
          } ${isPassword ? 'pr-10' : ''} ${className}`}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            tabIndex={-1}
            onClick={() => setShowPassword(!showPassword)}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-[#89766C] hover:text-[#541923] focus:outline-none"
            aria-label={showPassword ? "Hide password" : "Show password"}
          >
            {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        )}
      </div>
      {error && <p className="mt-1 text-xs text-[#B85C55] font-medium">{error}</p>}
      {!error && helperText && <p className="mt-1 text-xs text-[#89766C]">{helperText}</p>}
    </div>
  );
}

export default Input;
