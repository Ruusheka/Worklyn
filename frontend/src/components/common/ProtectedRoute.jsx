import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';

export function ProtectedRoute({ children, allowedRoles }) {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F3E6D6]">
        <div className="flex flex-col items-center space-y-3">
          <svg className="animate-spin h-8 w-8 text-[#541923]" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span className="text-sm font-semibold text-[#541923]">Loading Worklyn session...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const hasPermission = allowedRoles.some((role) => user?.roles?.includes(role));
    if (!hasPermission) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-[#F3E6D6] p-4">
          <div className="max-w-md w-full bg-[#FBF5EC] border border-[#EAD8C3] rounded-2xl p-8 text-center shadow-soft">
            <div className="w-12 h-12 rounded-full bg-[#FBECEB] text-[#B85C55] mx-auto flex items-center justify-center font-bold text-xl mb-4">
              !
            </div>
            <h2 className="text-xl font-bold text-[#342522] mb-2">Access Restricted</h2>
            <p className="text-sm text-[#89766C] mb-6">
              Your account ({user?.roles?.join(', ') || 'No roles'}) does not have permission to access this module.
            </p>
            <Navigate to="/dashboard" replace />
          </div>
        </div>
      );
    }
  }

  return children;
}

export default ProtectedRoute;
