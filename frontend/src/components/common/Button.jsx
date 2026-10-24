import React from 'react';

/**
 * Reusable Button Component matching HRM-SAGE aesthetic
 * Variants: 'primary' (wine), 'secondary' (cream/beige), 'outline', 'danger', 'ghost'
 */
export function Button({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  disabled = false,
  isLoading = false,
  onClick,
  className = '',
  icon: Icon,
  ...props
}) {
  const baseStyles = 'inline-flex items-center justify-center font-medium transition-all duration-150 rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed';

  const sizeStyles = {
    sm: 'text-xs px-3 py-1.5 gap-1.5',
    md: 'text-sm px-4 py-2.5 gap-2',
    lg: 'text-base px-5 py-3 gap-2.5',
  };

  const variantStyles = {
    primary: 'bg-[#541923] text-white hover:bg-[#42131b] active:bg-[#351018] focus:ring-[#702C37] shadow-sm',
    secondary: 'bg-[#FBF5EC] text-[#342522] border border-[#EAD8C3] hover:bg-[#F3E6D6] focus:ring-[#702C37]',
    outline: 'bg-transparent text-[#541923] border border-[#541923] hover:bg-[#541923]/10 focus:ring-[#541923]',
    danger: 'bg-[#B85C55] text-white hover:bg-[#a04e47] focus:ring-[#B85C55]',
    ghost: 'bg-transparent text-[#342522] hover:bg-[#EAD8C3]/40 focus:ring-[#702C37]',
    gold: 'bg-[#C9A574] text-[#342522] font-semibold hover:bg-[#b88e55] focus:ring-[#C9A574]',
  };

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      onClick={onClick}
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {isLoading ? (
        <>
          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-current" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>Processing...</span>
        </>
      ) : (
        <>
          {Icon && <Icon className="w-4 h-4 shrink-0" />}
          {children}
        </>
      )}
    </button>
  );
}

export default Button;
