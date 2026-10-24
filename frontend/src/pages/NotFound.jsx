import React from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, Home } from 'lucide-react';

export function NotFound() {
  return (
    <div className="min-h-screen bg-[#F3E6D6] flex items-center justify-center p-4">
      <div className="text-center max-w-md">
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-xl bg-[#541923] flex items-center justify-center">
            <Sparkles className="w-5 h-5 text-[#C9A574]" />
          </div>
          <span className="text-xl font-bold text-[#341018]">Worklyn</span>
        </div>

        <div className="text-9xl font-extrabold text-[#541923]/20 leading-none mb-4">404</div>

        <h1 className="text-2xl font-extrabold text-[#342522] mb-3">Page Not Found</h1>
        <p className="text-sm text-[#89766C] mb-8">
          The page you're looking for doesn't exist or has been moved. Check the URL or navigate back to the dashboard.
        </p>

        <Link
          to="/dashboard"
          className="inline-flex items-center gap-2 bg-[#541923] text-white px-6 py-3 rounded-xl font-semibold text-sm hover:bg-[#42131b] transition-colors shadow-sm"
        >
          <Home className="w-4 h-4" />
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
}

export default NotFound;
