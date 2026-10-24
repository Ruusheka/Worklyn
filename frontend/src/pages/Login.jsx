import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Sparkles, AlertCircle, ArrowRight, ShieldCheck, UserCheck, Briefcase, Database } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { isMockMode } from '../api/client.js';
import Button from '../components/common/Button.jsx';
import Input from '../components/common/Input.jsx';

export function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();
  const mockActive = isMockMode();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Redirect if already authenticated
  React.useEffect(() => {
    if (isAuthenticated) {
      const origin = location.state?.from?.pathname || '/dashboard';
      navigate(origin, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  const validate = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = 'Email or Employee ID is required';
    } else if (!email.includes('@') && !email.startsWith('EMP-')) {
      errs.email = 'Enter a valid email address or employee code';
    }
    if (!password) {
      errs.password = 'Password is required';
    } else if (password.length < 6) {
      errs.password = 'Password must be at least 6 characters';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setIsLoading(true);
    try {
      await login(email, password);
      const origin = location.state?.from?.pathname || '/dashboard';
      navigate(origin, { replace: true });
    } catch (err) {
      setServerError(err.message || 'Authentication failed. Please verify your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickFill = (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setErrors({});
    setServerError('');
  };

  return (
    <div className="min-h-screen flex bg-[#F3E6D6]">
      {/* Left Column: Login Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between p-6 sm:p-12 lg:p-16">
        {/* Brand Header */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#702C37] to-[#541923] border border-[#C9A574]/40 flex items-center justify-center text-white shadow-sm">
            <Sparkles className="w-5 h-5 text-[#C9A574]" />
          </div>
          <div>
            <span className="text-2xl font-bold tracking-tight text-[#351018]">Worklyn</span>
            <span className="text-xs uppercase tracking-wider text-[#702C37] font-semibold block -mt-1">
              HR Management System
            </span>
          </div>
        </div>

        {/* Center Panel: Form */}
        <div className="max-w-md w-full mx-auto my-8">
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <h1 className="text-3xl font-extrabold text-[#342522] tracking-tight">Welcome back</h1>
              {mockActive && (
                <span className="text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full bg-[#FDF5E8] text-[#D69A43] border border-[#D69A43]/30 flex items-center gap-1">
                  <Database className="w-3 h-3" />
                  Mock Mode
                </span>
              )}
            </div>
            <p className="text-sm text-[#89766C] mt-2">
              Sign in to manage employees, attendance, leave, payroll, and recovery workflows.
            </p>
          </div>

          {serverError && (
            <div className="mb-6 p-4 rounded-xl bg-[#FBECEB] border border-[#B85C55]/30 flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-[#B85C55] shrink-0 mt-0.5" />
              <div className="text-xs text-[#B85C55] font-semibold leading-relaxed">
                {serverError}
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Email or Employee ID"
              type="text"
              placeholder="e.g. hr.admin@worklyn.io"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              autoComplete="username"
            />

            <Input
              label="Password"
              type="password"
              placeholder="••••••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              autoComplete="current-password"
            />

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center gap-2 cursor-pointer text-[#89766C]">
                <input
                  type="checkbox"
                  defaultChecked
                  className="rounded border-[#EAD8C3] text-[#541923] focus:ring-[#702C37]"
                />
                Remember me
              </label>
              <button
                type="button"
                onClick={() => alert('Password recovery link has been dispatched to your corporate email.')}
                className="font-semibold text-[#541923] hover:underline"
              >
                Forgot password?
              </button>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full text-base font-semibold py-3 shadow-md"
              isLoading={isLoading}
            >
              Sign In to Worklyn
              <ArrowRight className="w-4 h-4 ml-2" />
            </Button>
          </form>

          {/* Quick Demo Fill Buttons (Specified in README Section 13.3.1) */}
          <div className="mt-8 pt-6 border-t border-[#EAD8C3]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#89766C] mb-3 text-center">
              Quick-Fill Test Personas
            </p>
            <div className="grid grid-cols-3 gap-2.5">
              <button
                type="button"
                onClick={() => handleQuickFill('hr.admin@worklyn.io', 'Password@123')}
                className="p-2.5 rounded-xl border border-[#EAD8C3] bg-[#FBF5EC] hover:bg-[#EAD8C3]/50 text-left transition-colors group"
              >
                <div className="flex items-center gap-1.5 text-xs font-bold text-[#541923]">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  Admin
                </div>
                <div className="text-[10px] text-[#89766C] truncate mt-0.5">Full System</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('elena.rostova@worklyn.io', 'Password@123')}
                className="p-2.5 rounded-xl border border-[#EAD8C3] bg-[#FBF5EC] hover:bg-[#EAD8C3]/50 text-left transition-colors group"
              >
                <div className="flex items-center gap-1.5 text-xs font-bold text-[#702C37]">
                  <Briefcase className="w-3.5 h-3.5" />
                  Manager
                </div>
                <div className="text-[10px] text-[#89766C] truncate mt-0.5">Team Approver</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('david.chen@worklyn.io', 'Password@123')}
                className="p-2.5 rounded-xl border border-[#EAD8C3] bg-[#FBF5EC] hover:bg-[#EAD8C3]/50 text-left transition-colors group"
              >
                <div className="flex items-center gap-1.5 text-xs font-bold text-[#342522]">
                  <UserCheck className="w-3.5 h-3.5" />
                  Employee
                </div>
                <div className="text-[10px] text-[#89766C] truncate mt-0.5">Self-Service</div>
              </button>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center text-xs text-[#89766C]">
          Protected by Enterprise Token-Bucket Rate Limiter &bull; Worklyn v1.0
        </div>
      </div>

      {/* Right Column: Warm Visual Workplace Section */}
      <div className="hidden lg:block lg:w-1/2 relative bg-[#351018] overflow-hidden">
        <img
          src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=1600&q=80"
          alt="Workplace Collaboration"
          className="absolute inset-0 w-full h-full object-cover mix-blend-overlay opacity-60"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#351018] via-[#541923]/60 to-transparent flex flex-col justify-end p-16 text-white">
          <div className="max-w-md space-y-4">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-[#C9A574]/20 text-[#C9A574] border border-[#C9A574]/30 backdrop-blur-xs">
              <Sparkles className="w-3.5 h-3.5" />
              Unified People Platform
            </span>
            <h2 className="text-3xl font-bold leading-tight">
              Streamline attendance, leave, payroll, and fault-tolerant DAG recovery.
            </h2>
            <p className="text-sm text-[#F3E6D6]/80 leading-relaxed">
              Designed for distributed consistency with Murmur3 shard routing and transactional compensation rollbacks.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
