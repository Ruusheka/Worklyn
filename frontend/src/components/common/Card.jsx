import React from 'react';

export function Card({
  children,
  title,
  subtitle,
  action,
  className = '',
  bodyClassName = '',
  headerClassName = '',
  ...props
}) {
  return (
    <div
      className={`bg-[#FBF5EC] border border-[#EAD8C3] rounded-xl shadow-soft ${className}`}
      {...props}
    >
      {(title || subtitle || action) && (
        <div className={`px-5 py-4 border-b border-[#EAD8C3] flex items-center justify-between ${headerClassName}`}>
          <div>
            {title && <h3 className="text-base font-semibold text-[#342522]">{title}</h3>}
            {subtitle && <p className="text-xs text-[#89766C] mt-0.5">{subtitle}</p>}
          </div>
          {action && <div className="shrink-0">{action}</div>}
        </div>
      )}
      <div className={`p-5 ${bodyClassName}`}>{children}</div>
    </div>
  );
}

export default Card;
