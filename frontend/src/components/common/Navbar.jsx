import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Bell, Menu, Shield, User, ChevronDown, Database, Cpu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';
import { systemApi } from '../../api/systemApi.js';
import { isMockMode, setMockMode } from '../../api/client.js';

export function Navbar({ onToggleSidebar }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const mockActive = isMockMode();

  const searchRef = useRef(null);
  const profileMenuRef = useRef(null);

  // Autocomplete debounce
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSuggestions([]);
      return;
    }
    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const results = await systemApi.autocompleteEmployees(searchQuery);
        setSuggestions(results);
        setShowDropdown(true);
      } catch (e) {
        console.error('Autocomplete error', e);
      } finally {
        setIsSearching(false);
      }
    }, 200);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  // Click outside listener for dropdowns
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
      if (profileMenuRef.current && !profileMenuRef.current.contains(e.target)) {
        setShowProfileMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelectEmployee = (emp) => {
    setShowDropdown(false);
    setSearchQuery('');
    navigate(`/employees/${emp.id || emp.employeeCode}`);
  };

  const toggleMockMode = () => {
    setMockMode(!mockActive);
    window.location.reload();
  };

  return (
    <header className="h-18 bg-[#FBF5EC] border-b border-[#EAD8C3] px-4 lg:px-8 flex items-center justify-between sticky top-0 z-30 shadow-soft">
      {/* Left: Mobile Toggle & Search */}
      <div className="flex items-center gap-4 flex-1 max-w-lg">
        <button
          onClick={onToggleSidebar}
          className="p-2 rounded-xl text-[#541923] hover:bg-[#F3E6D6] transition-colors lg:hidden"
          aria-label="Toggle navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Global Autocomplete Search Input */}
        <div ref={searchRef} className="relative w-full">
          <div className="relative">
            <Search className="w-4 h-4 text-[#89766C] absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search employees, code, department..."
              className="w-full bg-[#FFFFFF] border border-[#EAD8C3] rounded-xl pl-9.5 pr-4 py-2 text-xs lg:text-sm text-[#342522] placeholder-[#89766C]/70 focus:outline-none focus:border-[#702C37] focus:ring-1 focus:ring-[#702C37] transition-all"
            />
            {isSearching && (
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <svg className="animate-spin h-3.5 w-3.5 text-[#541923]" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </div>
            )}
          </div>

          {/* Autocomplete Suggestions Overlay */}
          {showDropdown && suggestions.length > 0 && (
            <div className="absolute left-0 right-0 top-full mt-2 bg-white border border-[#EAD8C3] rounded-xl shadow-lg z-50 overflow-hidden divide-y divide-[#EAD8C3]">
              <div className="px-3 py-1.5 bg-[#F8EFE4] text-[10px] uppercase font-bold text-[#89766C] tracking-wider">
                Autocomplete Results
              </div>
              {suggestions.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleSelectEmployee(item)}
                  className="px-4 py-2.5 hover:bg-[#F3E6D6]/50 cursor-pointer flex items-center justify-between transition-colors"
                >
                  <div>
                    <p className="text-sm font-semibold text-[#342522]">{item.name}</p>
                    <p className="text-xs text-[#89766C]">
                      {item.designation} &bull; {item.department}
                    </p>
                  </div>
                  <span className="text-xs font-mono font-medium text-[#541923] bg-[#F4EBE6] px-2 py-0.5 rounded">
                    {item.employeeCode}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Right Controls: Mock Indicator, Notifications & Profile */}
      <div className="flex items-center gap-3 lg:gap-4">
        {/* Mock Mode Pill */}
        {mockActive ? (
          <span
            className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-[#FDF5E8] text-[#D69A43] border border-[#D69A43]/30 hidden sm:inline-flex cursor-pointer"
            onClick={toggleMockMode}
            title="Click to toggle between Mock and Live API mode"
          >
            <span className="w-1.5 h-1.5 rounded-full bg-[#D69A43] animate-pulse"></span>
            Mock Mode (Offline)
          </span>
        ) : (
          <span
            className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-[#EBF4EF] text-[#387A58] border border-[#387A58]/30 hidden sm:inline-flex cursor-pointer"
            onClick={toggleMockMode}
            title="Click to toggle between Mock and Live API mode"
          >
            <span className="w-1.5 h-1.5 rounded-full bg-[#387A58]"></span>
            Live API Mode
          </span>
        )}

        {/* Notifications Icon */}
        <button
          onClick={() => navigate('/notifications')}
          className="relative p-2 rounded-xl text-[#342522] hover:bg-[#F3E6D6] transition-colors"
          aria-label="View notifications"
        >
          <Bell className="w-5 h-5 text-[#541923]" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#B85C55] ring-2 ring-[#FBF5EC]"></span>
        </button>

        <div className="h-6 w-px bg-[#EAD8C3] hidden sm:block"></div>

        {/* User Profile Pill & Dropdown */}
        <div ref={profileMenuRef} className="relative">
          <button
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            className="flex items-center gap-2.5 p-1 rounded-xl hover:bg-[#F3E6D6]/60 transition-colors"
          >
            <img
              src={
                user?.avatar ||
                'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'
              }
              alt={user?.firstName || 'User Avatar'}
              className="w-9 h-9 rounded-full object-cover border border-[#EAD8C3]"
            />
            <div className="text-left hidden md:block">
              <p className="text-xs font-bold text-[#342522] leading-tight">
                {user ? `${user.firstName} ${user.lastName}` : 'Guest User'}
              </p>
              <p className="text-[11px] text-[#89766C] leading-tight">
                {user?.designation || user?.roles?.[0]?.replace('ROLE_', '') || 'Employee'}
              </p>
            </div>
            <ChevronDown className="w-4 h-4 text-[#89766C] hidden md:block" />
          </button>

          {showProfileMenu && (
            <div className="absolute right-0 top-full mt-2 w-56 bg-white border border-[#EAD8C3] rounded-xl shadow-card z-50 py-1 divide-y divide-[#EAD8C3]">
              <div className="px-4 py-2.5">
                <p className="text-xs font-bold text-[#342522]">Signed in as</p>
                <p className="text-xs text-[#89766C] truncate">{user?.email}</p>
                <div className="mt-1 flex gap-1">
                  {user?.roles?.map((r) => (
                    <span key={r} className="text-[9px] bg-[#F4EBE6] text-[#541923] font-semibold px-1.5 py-0.5 rounded">
                      {r.replace('ROLE_', '')}
                    </span>
                  ))}
                </div>
              </div>
              <div className="py-1">
                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    navigate(user?.employeeId ? `/employees/${user.employeeId}` : '/settings');
                  }}
                  className="w-full text-left px-4 py-2 text-xs text-[#342522] hover:bg-[#F3E6D6]/50 flex items-center gap-2"
                >
                  <User className="w-3.5 h-3.5 text-[#541923]" />
                  <span>View My Profile</span>
                </button>
                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    navigate('/settings');
                  }}
                  className="w-full text-left px-4 py-2 text-xs text-[#342522] hover:bg-[#F3E6D6]/50 flex items-center gap-2"
                >
                  <Shield className="w-3.5 h-3.5 text-[#541923]" />
                  <span>Account Settings</span>
                </button>
                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    toggleMockMode();
                  }}
                  className="w-full text-left px-4 py-2 text-xs text-[#702C37] hover:bg-[#F3E6D6]/50 flex items-center gap-2"
                >
                  <Database className="w-3.5 h-3.5 text-[#702C37]" />
                  <span>{mockActive ? 'Switch to Live API Mode' : 'Switch to Mock Data Mode'}</span>
                </button>
              </div>
              <div className="py-1">
                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    logout();
                    navigate('/login');
                  }}
                  className="w-full text-left px-4 py-2 text-xs text-[#B85C55] font-semibold hover:bg-[#FBECEB] flex items-center gap-2"
                >
                  <span>Sign Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default Navbar;
