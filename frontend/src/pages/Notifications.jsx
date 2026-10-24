import React, { useState } from 'react';
import { Bell, CheckCheck, Filter, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import { INITIAL_NOTIFICATIONS } from '../api/mockData.js';

export function Notifications() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState(INITIAL_NOTIFICATIONS);
  const [activeFilter, setActiveFilter] = useState('All');

  const categories = ['All', 'HR', 'Leave', 'Payroll', 'Recruitment'];

  const filtered = activeFilter === 'All'
    ? notifications
    : notifications.filter(n => n.category === activeFilter);

  const unreadCount = notifications.filter(n => !n.read).length;

  const markAllRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const markRead = (id) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const categoryColor = {
    HR: 'bg-[#F4EBE6] text-[#541923] border-[#702C37]/20',
    Leave: 'bg-[#EEF4F8] text-[#4B6B82] border-[#4B6B82]/20',
    Payroll: 'bg-[#EBF4EF] text-[#387A58] border-[#387A58]/20',
    Recruitment: 'bg-[#FDF5E8] text-[#D69A43] border-[#D69A43]/20',
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Notifications</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            {unreadCount > 0 ? `${unreadCount} unread alerts` : 'All notifications reviewed'} · System and workflow alerts
          </p>
        </div>
        {unreadCount > 0 && (
          <Button variant="secondary" icon={CheckCheck} onClick={markAllRead} size="sm">
            Mark All Read
          </Button>
        )}
      </div>

      {/* Category Filters */}
      <div className="flex flex-wrap gap-2">
        {categories.map(cat => (
          <button
            key={cat}
            onClick={() => setActiveFilter(cat)}
            className={`px-4 py-1.5 rounded-full text-xs font-semibold border transition-all ${
              activeFilter === cat
                ? 'bg-[#541923] text-white border-[#541923]'
                : 'bg-[#FBF5EC] text-[#89766C] border-[#EAD8C3] hover:bg-[#F3E6D6]'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Notification List */}
      <Card title={`${filtered.length} Notification${filtered.length !== 1 ? 's' : ''}`}>
        <div className="divide-y divide-[#EAD8C3]/60">
          {filtered.length === 0 ? (
            <div className="py-10 text-center text-[#89766C] text-sm">
              No notifications in this category.
            </div>
          ) : (
            filtered.map((notif) => (
              <div
                key={notif.id}
                className={`py-4 flex items-start gap-4 transition-colors cursor-pointer ${
                  notif.read ? '' : 'bg-[#FBF5EC]/60'
                }`}
                onClick={() => {
                  markRead(notif.id);
                  if (notif.link) navigate(notif.link);
                }}
              >
                {/* Indicator dot */}
                <div className="mt-1 shrink-0">
                  {notif.read ? (
                    <div className="w-2.5 h-2.5 rounded-full bg-[#EAD8C3]" />
                  ) : (
                    <div className="w-2.5 h-2.5 rounded-full bg-[#541923]" />
                  )}
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded border ${categoryColor[notif.category] || 'bg-[#F3E6D6] text-[#89766C] border-[#EAD8C3]'}`}>
                      {notif.category}
                    </span>
                    <span className="text-[11px] text-[#89766C]">{notif.timestamp}</span>
                    {!notif.read && (
                      <span className="text-[10px] font-bold text-[#B85C55] bg-[#FBECEB] px-1.5 py-0.5 rounded">NEW</span>
                    )}
                  </div>
                  <p className={`text-xs font-bold ${notif.read ? 'text-[#89766C]' : 'text-[#342522]'}`}>
                    {notif.title}
                  </p>
                  <p className="text-xs text-[#89766C] mt-0.5 line-clamp-2">{notif.description}</p>
                </div>

                {/* Navigate Arrow */}
                {notif.link && (
                  <div className="shrink-0 text-[#89766C] hover:text-[#541923] transition-colors mt-1">
                    <ArrowRight className="w-4 h-4" />
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
}

export default Notifications;
