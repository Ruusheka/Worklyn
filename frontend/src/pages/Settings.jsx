import React, { useState } from 'react';
import { User, Mail, Globe, Clock, Monitor, Sun, Moon, Type, Save, Shield, Bell } from 'lucide-react';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Input from '../components/common/Input.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function Settings() {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [activeSection, setActiveSection] = useState('profile');
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || 'Sarah',
    lastName: user?.lastName || 'Jenkins',
    email: user?.email || 'hr.admin@worklyn.io',
    phone: '+1 (555) 234-5678',
    designation: user?.designation || 'VP of People Operations'
  });
  const [isSaving, setIsSaving] = useState(false);

  const [preferences, setPreferences] = useState({
    language: 'en-US',
    timezone: 'America/New_York',
    dateFormat: 'MM/DD/YYYY',
    theme: 'light',
    fontSize: 'medium',
    compactMode: false,
    emailNotifications: true,
    leaveAlerts: true,
    payrollAlerts: true,
    rewindAlerts: true
  });

  const sections = [
    { id: 'profile', label: 'Profile Settings', icon: User },
    { id: 'localization', label: 'Language & Timezone', icon: Globe },
    { id: 'appearance', label: 'Appearance', icon: Monitor },
    { id: 'notifications', label: 'Notification Preferences', icon: Bell },
    { id: 'security', label: 'Security & Sessions', icon: Shield },
  ];

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    // Simulate save
    await new Promise(r => setTimeout(r, 800));
    setIsSaving(false);
    addToast('Profile settings saved successfully!', 'success');
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Account Settings</h1>
        <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
          Manage your account preferences, localization, and notification controls
        </p>
      </div>

      <div className="flex flex-col lg:flex-row gap-6">
        {/* Settings Navigation */}
        <div className="w-full lg:w-56 shrink-0">
          <Card bodyClassName="p-2">
            <nav className="space-y-0.5">
              {sections.map(s => {
                const Icon = s.icon;
                return (
                  <button
                    key={s.id}
                    onClick={() => setActiveSection(s.id)}
                    className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                      activeSection === s.id
                        ? 'bg-[#541923] text-white'
                        : 'text-[#89766C] hover:bg-[#F3E6D6] hover:text-[#342522]'
                    }`}
                  >
                    <Icon className="w-4 h-4 shrink-0" />
                    {s.label}
                  </button>
                );
              })}
            </nav>
          </Card>
        </div>

        {/* Settings Content */}
        <div className="flex-1 space-y-6">

          {/* Profile Settings */}
          {activeSection === 'profile' && (
            <Card title="Profile Information" subtitle="Update your personal and contact details">
              <form onSubmit={handleSaveProfile} className="space-y-4">
                <div className="flex items-center gap-4 pb-4 border-b border-[#EAD8C3]">
                  <img
                    src={user?.avatar || 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'}
                    alt="Profile"
                    className="w-16 h-16 rounded-2xl object-cover border-2 border-[#EAD8C3]"
                  />
                  <div>
                    <p className="text-sm font-bold text-[#342522]">{profileForm.firstName} {profileForm.lastName}</p>
                    <p className="text-xs text-[#89766C]">{profileForm.designation}</p>
                    <button
                      type="button"
                      className="mt-1 text-xs font-semibold text-[#541923] hover:underline"
                      onClick={() => addToast('Avatar upload requires backend integration', 'info')}
                    >
                      Change Photo
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="First Name"
                    value={profileForm.firstName}
                    onChange={e => setProfileForm({...profileForm, firstName: e.target.value})}
                  />
                  <Input
                    label="Last Name"
                    value={profileForm.lastName}
                    onChange={e => setProfileForm({...profileForm, lastName: e.target.value})}
                  />
                </div>
                <Input
                  label="Email Address"
                  type="email"
                  value={profileForm.email}
                  onChange={e => setProfileForm({...profileForm, email: e.target.value})}
                />
                <Input
                  label="Phone Number"
                  value={profileForm.phone}
                  onChange={e => setProfileForm({...profileForm, phone: e.target.value})}
                />
                <div className="flex justify-end pt-2">
                  <Button type="submit" variant="primary" icon={Save} isLoading={isSaving}>
                    Save Profile
                  </Button>
                </div>
              </form>
            </Card>
          )}

          {/* Localization */}
          {activeSection === 'localization' && (
            <Card title="Language & Regional Settings">
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">Interface Language</label>
                  <select
                    value={preferences.language}
                    onChange={e => setPreferences({...preferences, language: e.target.value})}
                    className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none"
                  >
                    <option value="en-US">English (United States)</option>
                    <option value="en-GB">English (United Kingdom)</option>
                    <option value="es">Spanish</option>
                    <option value="fr">French</option>
                    <option value="de">German</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">Time Zone</label>
                  <select
                    value={preferences.timezone}
                    onChange={e => setPreferences({...preferences, timezone: e.target.value})}
                    className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none"
                  >
                    <option value="America/New_York">Eastern Time (UTC-5)</option>
                    <option value="America/Chicago">Central Time (UTC-6)</option>
                    <option value="America/Los_Angeles">Pacific Time (UTC-8)</option>
                    <option value="Europe/London">London (UTC+0)</option>
                    <option value="Asia/Kolkata">India Standard Time (UTC+5:30)</option>
                    <option value="Asia/Singapore">Singapore (UTC+8)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">Date Format</label>
                  <select
                    value={preferences.dateFormat}
                    onChange={e => setPreferences({...preferences, dateFormat: e.target.value})}
                    className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none"
                  >
                    <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                    <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                    <option value="YYYY-MM-DD">YYYY-MM-DD (ISO 8601)</option>
                  </select>
                </div>
                <div className="flex justify-end">
                  <Button variant="primary" icon={Save} onClick={() => addToast('Localization preferences saved!', 'success')}>
                    Save Preferences
                  </Button>
                </div>
              </div>
            </Card>
          )}

          {/* Appearance */}
          {activeSection === 'appearance' && (
            <Card title="Appearance & Display">
              <div className="space-y-5">
                <div>
                  <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-3">Color Theme</label>
                  <div className="grid grid-cols-3 gap-3">
                    {[
                      { id: 'light', label: 'Light', desc: 'Warm beige & wine (default)', icon: Sun },
                      { id: 'system', label: 'System', desc: 'Follow OS preference', icon: Monitor },
                      { id: 'dark', label: 'Dark', desc: 'Dark mode (coming soon)', icon: Moon, disabled: true }
                    ].map(t => {
                      const Icon = t.icon;
                      return (
                        <button
                          key={t.id}
                          onClick={() => !t.disabled && setPreferences({...preferences, theme: t.id})}
                          disabled={t.disabled}
                          className={`p-3 rounded-xl border-2 text-left transition-all ${
                            preferences.theme === t.id && !t.disabled
                              ? 'border-[#541923] bg-[#F4EBE6]'
                              : t.disabled
                              ? 'border-[#EAD8C3] bg-[#F3E6D6]/40 opacity-50 cursor-not-allowed'
                              : 'border-[#EAD8C3] bg-white hover:border-[#702C37]'
                          }`}
                        >
                          <Icon className="w-5 h-5 text-[#541923] mb-2" />
                          <p className="text-xs font-bold text-[#342522]">{t.label}</p>
                          <p className="text-[10px] text-[#89766C] mt-0.5">{t.desc}</p>
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-3">Font Size</label>
                  <div className="flex items-center gap-3">
                    {['small', 'medium', 'large'].map(size => (
                      <button
                        key={size}
                        onClick={() => setPreferences({...preferences, fontSize: size})}
                        className={`flex-1 py-2 rounded-xl text-xs font-semibold border-2 transition-all capitalize ${
                          preferences.fontSize === size
                            ? 'border-[#541923] bg-[#F4EBE6] text-[#541923]'
                            : 'border-[#EAD8C3] bg-white text-[#89766C] hover:border-[#702C37]'
                        }`}
                      >
                        {size}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-between p-3.5 bg-white rounded-xl border border-[#EAD8C3]">
                  <div>
                    <p className="text-xs font-bold text-[#342522]">Compact Dashboard Layout</p>
                    <p className="text-[11px] text-[#89766C] mt-0.5">Reduce spacing and show more data per screen</p>
                  </div>
                  <button
                    onClick={() => setPreferences({...preferences, compactMode: !preferences.compactMode})}
                    className={`w-11 h-6 rounded-full transition-colors ${preferences.compactMode ? 'bg-[#541923]' : 'bg-[#EAD8C3]'}`}
                  >
                    <div className={`w-4 h-4 bg-white rounded-full shadow transition-transform mx-1 ${preferences.compactMode ? 'translate-x-5' : 'translate-x-0'}`} />
                  </button>
                </div>

                <div className="flex justify-end">
                  <Button variant="primary" icon={Save} onClick={() => addToast('Appearance settings saved!', 'success')}>
                    Apply Changes
                  </Button>
                </div>
              </div>
            </Card>
          )}

          {/* Notifications Prefs */}
          {activeSection === 'notifications' && (
            <Card title="Notification Preferences" subtitle="Control which alerts are delivered to your inbox">
              <div className="space-y-3">
                {[
                  { key: 'emailNotifications', label: 'Email Notifications', desc: 'Receive summary emails for critical HR events' },
                  { key: 'leaveAlerts', label: 'Leave Approval Alerts', desc: 'Notify when leave requests are approved or rejected' },
                  { key: 'payrollAlerts', label: 'Payroll Processing Alerts', desc: 'Notify when payroll batch completes or finalizes' },
                  { key: 'rewindAlerts', label: 'HR-REWIND Failure Alerts', desc: 'Alert when a DAG workflow fails and needs recovery' }
                ].map(pref => (
                  <div key={pref.key} className="flex items-center justify-between p-3.5 bg-white rounded-xl border border-[#EAD8C3]">
                    <div>
                      <p className="text-xs font-bold text-[#342522]">{pref.label}</p>
                      <p className="text-[11px] text-[#89766C] mt-0.5">{pref.desc}</p>
                    </div>
                    <button
                      onClick={() => setPreferences({...preferences, [pref.key]: !preferences[pref.key]})}
                      className={`w-11 h-6 rounded-full transition-colors shrink-0 ${preferences[pref.key] ? 'bg-[#541923]' : 'bg-[#EAD8C3]'}`}
                    >
                      <div className={`w-4 h-4 bg-white rounded-full shadow transition-transform mx-1 ${preferences[pref.key] ? 'translate-x-5' : 'translate-x-0'}`} />
                    </button>
                  </div>
                ))}
                <div className="flex justify-end pt-2">
                  <Button variant="primary" icon={Save} onClick={() => addToast('Notification preferences saved!', 'success')}>
                    Save Settings
                  </Button>
                </div>
              </div>
            </Card>
          )}

          {/* Security */}
          {activeSection === 'security' && (
            <Card title="Security & Session Management">
              <div className="space-y-4">
                <div className="p-4 bg-[#EBF4EF] rounded-xl border border-[#387A58]/30 text-xs text-[#387A58]">
                  <p className="font-bold mb-1">Session Status: Active & Secure</p>
                  <p>JWT token valid for 24 hours · HMAC-SHA256 signed · Stateless authentication</p>
                </div>

                <div className="space-y-3">
                  <button
                    onClick={() => addToast('Password change requires re-authentication via backend endpoint', 'info')}
                    className="w-full flex items-center justify-between p-3.5 bg-white rounded-xl border border-[#EAD8C3] hover:bg-[#F3E6D6]/40 transition-colors text-left"
                  >
                    <div>
                      <p className="text-xs font-bold text-[#342522]">Change Password</p>
                      <p className="text-[11px] text-[#89766C]">Update your account password securely</p>
                    </div>
                    <Shield className="w-4 h-4 text-[#541923]" />
                  </button>

                  <button
                    onClick={() => addToast('Two-factor authentication setup pending backend integration', 'info')}
                    className="w-full flex items-center justify-between p-3.5 bg-white rounded-xl border border-[#EAD8C3] hover:bg-[#F3E6D6]/40 transition-colors text-left"
                  >
                    <div>
                      <p className="text-xs font-bold text-[#342522]">Two-Factor Authentication</p>
                      <p className="text-[11px] text-[#89766C]">Enable TOTP-based 2FA for enhanced account protection</p>
                    </div>
                    <span className="text-[10px] bg-[#FDF5E8] text-[#D69A43] font-bold px-2 py-0.5 rounded border border-[#D69A43]/30">
                      PENDING
                    </span>
                  </button>
                </div>
              </div>
            </Card>
          )}

        </div>
      </div>
    </div>
  );
}

export default Settings;
