import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  CalendarCheck,
  CalendarDays,
  CreditCard,
  Briefcase,
  TrendingUp,
  RotateCcw,
  Bell,
  Settings,
  LogOut,
  Sparkles,
  X
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';

export function Sidebar({ isOpen, onClose }) {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    {
      to: '/dashboard',
      label: 'Dashboard',
      icon: LayoutDashboard,
      visible: true,
    },
    {
      to: '/employees',
      label: 'Employees',
      icon: Users,
      visible: hasRole('ROLE_ADMIN') || hasRole('ROLE_HR') || hasRole('ROLE_MANAGER'),
    },
    {
      to: '/attendance',
      label: 'Attendance',
      icon: CalendarCheck,
      visible: true,
    },
    {
      to: '/leave',
      label: 'Leave Management',
      icon: CalendarDays,
      visible: true,
    },
    {
      to: '/payroll',
      label: 'Payroll',
      icon: CreditCard,
      visible: true,
    },
    {
      to: '/recruitment',
      label: 'Recruitment',
      icon: Briefcase,
      visible: hasRole('ROLE_ADMIN') || hasRole('ROLE_HR'),
    },
    {
      to: '/performance',
      label: 'Performance',
      icon: TrendingUp,
      visible: true,
    },
    {
      to: '/rewind',
      label: 'HR-REWIND Engine',
      icon: RotateCcw,
      visible: hasRole('ROLE_ADMIN'),
      badge: 'Core',
    },
    {
      to: '/notifications',
      label: 'Notifications',
      icon: Bell,
      visible: true,
    },
    {
      to: '/settings',
      label: 'Settings',
      icon: Settings,
      visible: true,
    },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-[#351018]/60 backdrop-blur-xs lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 w-64 bg-[#351018] text-[#F3E6D6] flex flex-col transition-transform duration-200 ease-in-out lg:static lg:translate-x-0 ${
          isOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-18 flex items-center justify-between px-6 border-b border-[#541923]">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#702C37] to-[#541923] border border-[#C9A574]/40 flex items-center justify-center text-white shadow-inner">
              <Sparkles className="w-5 h-5 text-[#C9A574]" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-white block">Worklyn</span>
              <span className="text-[10px] uppercase tracking-wider text-[#C9A574] font-semibold block -mt-0.5">
                HR Architecture
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[#EAD8C3]/70 hover:text-white hover:bg-white/10 lg:hidden"
            aria-label="Close sidebar"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[11px] font-semibold tracking-wider text-[#EAD8C3]/50 uppercase">
            Platform Navigation
          </div>
          {navItems
            .filter((item) => item.visible)
            .map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => onClose && onClose()}
                  className={({ isActive }) =>
                    `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all group ${
                      isActive
                        ? 'bg-[#F3E6D6] text-[#351018] font-bold shadow-sm'
                        : 'text-[#EAD8C3]/80 hover:bg-[#541923]/60 hover:text-white'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <div className="flex items-center gap-3">
                        <Icon
                          className={`w-4 h-4 transition-colors ${
                            isActive ? 'text-[#541923]' : 'text-[#C9A574] group-hover:text-white'
                          }`}
                        />
                        <span>{item.label}</span>
                      </div>
                      {item.badge && (
                        <span
                          className={`text-[10px] px-1.5 py-0.5 rounded font-bold uppercase tracking-wider ${
                            isActive
                              ? 'bg-[#541923] text-white'
                              : 'bg-[#702C37] text-[#C9A574]'
                          }`}
                        >
                          {item.badge}
                        </span>
                      )}
                    </>
                  )}
                </NavLink>
              );
            })}
        </div>

        {/* Bottom User Profile Section */}
        <div className="p-3 border-t border-[#541923] bg-[#2d0d14]">
          <div className="flex items-center justify-between p-2 rounded-xl bg-[#351018]/80 border border-[#541923]/60">
            <div className="flex items-center gap-2.5 min-w-0">
              <img
                src={
                  user?.avatar ||
                  'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'
                }
                alt={user?.firstName || 'User'}
                className="w-8 h-8 rounded-full object-cover border border-[#C9A574]/40 shrink-0"
              />
              <div className="min-w-0">
                <p className="text-xs font-semibold text-white truncate">
                  {user ? `${user.firstName} ${user.lastName}` : 'Guest User'}
                </p>
                <p className="text-[10px] text-[#C9A574] truncate">
                  {user?.roles?.[0]?.replace('ROLE_', '') || 'EMPLOYEE'}
                </p>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="p-1.5 rounded-lg text-[#EAD8C3]/70 hover:text-white hover:bg-[#541923] transition-colors"
              title="Sign Out"
              aria-label="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
