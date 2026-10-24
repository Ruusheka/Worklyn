import React, { useState } from 'react';
import { TrendingUp, Target, MessageSquare, CheckCircle2, AlertTriangle, Clock, ChevronRight } from 'lucide-react';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function Performance() {
  const { user, hasRole } = useAuth();
  const { addToast } = useToast();
  const [activeTab, setActiveTab] = useState('goals');
  const goals = [];
  const reviews = [];

  const avgProgress = goals.length > 0 ? Math.round(goals.reduce((sum, g) => sum + g.progress, 0) / goals.length) : 0;

  const getProgressColor = (pct) => {
    if (pct >= 80) return 'bg-[#387A58]';
    if (pct >= 50) return 'bg-[#D69A43]';
    return 'bg-[#B85C55]';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Performance Management</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            OKR tracking, peer feedback, and quarterly review cycles
          </p>
        </div>
        <Button
          variant="primary"
          icon={Target}
          onClick={() => addToast('Goal creation requires backend Performance API (planned for future release)', 'warning')}
        >
          Set New Goal
        </Button>
      </div>

      {/* Summary Banner */}
      <Card bodyClassName="p-6 bg-gradient-to-r from-[#FBF5EC] to-[#F3E6D6]">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-2xl bg-[#541923] flex items-center justify-center shadow-sm">
              <span className="text-2xl font-extrabold text-white">{avgProgress}%</span>
            </div>
            <div>
              <p className="text-xs font-bold text-[#89766C] uppercase tracking-wider">Overall Goal Progress</p>
              <p className="text-xl font-extrabold text-[#342522]">Q4 2026 Performance Cycle</p>
              <div className="mt-1.5 w-48 bg-[#EAD8C3] rounded-full h-2 overflow-hidden">
                <div
                  className="bg-[#541923] h-full rounded-full transition-all duration-500"
                  style={{ width: `${avgProgress}%` }}
                />
              </div>
            </div>
          </div>
          <div className="flex gap-6 text-center">
            <div>
              <p className="text-2xl font-extrabold text-[#387A58]">{goals.filter(g => g.status === 'ON_TRACK').length}</p>
              <p className="text-[11px] text-[#89766C] uppercase font-bold">On Track</p>
            </div>
            <div>
              <p className="text-2xl font-extrabold text-[#D69A43]">{goals.filter(g => g.status === 'ATTENTION_NEEDED').length}</p>
              <p className="text-[11px] text-[#89766C] uppercase font-bold">At Risk</p>
            </div>
            <div>
              <p className="text-2xl font-extrabold text-[#342522]">{goals.length}</p>
              <p className="text-[11px] text-[#89766C] uppercase font-bold">Total Goals</p>
            </div>
          </div>
        </div>
      </Card>

      {/* Backend Pending Banner */}
      <div className="p-4 bg-[#FDF5E8] border border-[#D69A43]/30 rounded-xl flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-[#D69A43] shrink-0 mt-0.5" />
        <div className="text-xs">
          <p className="font-bold text-[#342522] mb-1">Performance Management — Backend Pending</p>
          <p className="text-[#89766C]">
            The Performance Management API (OKR tracking, peer reviews) is planned for a future release. No live data is available yet.
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#EAD8C3] space-x-6">
        <button
          onClick={() => setActiveTab('goals')}
          className={`pb-3 text-sm font-semibold border-b-2 transition-all ${activeTab === 'goals' ? 'border-[#541923] text-[#541923]' : 'border-transparent text-[#89766C] hover:text-[#342522]'}`}
        >
          My Goals ({goals.length})
        </button>
        <button
          onClick={() => setActiveTab('reviews')}
          className={`pb-3 text-sm font-semibold border-b-2 transition-all ${activeTab === 'reviews' ? 'border-[#541923] text-[#541923]' : 'border-transparent text-[#89766C] hover:text-[#342522]'}`}
        >
          Feedback & Reviews
        </button>
      </div>

      {/* Goals Tab */}
      {activeTab === 'goals' && (
        <div className="space-y-4">
          {goals.map((goal) => (
            <Card key={goal.id} className="hover:shadow-card transition-shadow">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1.5">
                    <span className="text-[10px] font-bold uppercase text-[#702C37] tracking-wider bg-[#F4EBE6] px-2 py-0.5 rounded">
                      {goal.category}
                    </span>
                    <StatusBadge status={goal.status} />
                  </div>
                  <h3 className="text-sm font-bold text-[#342522]">{goal.title}</h3>
                  <p className="text-xs text-[#89766C] mt-1 flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    Due: {goal.targetDate}
                  </p>
                </div>

                <div className="sm:w-48 flex-shrink-0">
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-xs text-[#89766C]">Progress</span>
                    <span className={`text-sm font-extrabold ${goal.progress >= 80 ? 'text-[#387A58]' : goal.progress >= 50 ? 'text-[#D69A43]' : 'text-[#B85C55]'}`}>
                      {goal.progress}%
                    </span>
                  </div>
                  <div className="w-full bg-[#EAD8C3] rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`${getProgressColor(goal.progress)} h-full rounded-full transition-all duration-700`}
                      style={{ width: `${goal.progress}%` }}
                    />
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Reviews Tab */}
      {activeTab === 'reviews' && (
        <div className="space-y-4">
          <div className="p-4 bg-[#FDF5E8] border border-[#D69A43]/30 rounded-xl text-xs text-[#D69A43] font-semibold flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            Performance review data is fetched from the backend Performance Management module (future feature). Displaying available records.
          </div>

          {reviews.map((rev) => (
            <Card key={rev.id}>
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-[#F4EBE6] flex items-center justify-center font-bold text-[#541923]">
                    {rev.reviewer.split(' ').map(n => n[0]).join('')}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-[#342522]">{rev.reviewer}</p>
                    <p className="text-[11px] text-[#89766C]">{rev.reviewerRole}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs font-bold text-[#387A58] bg-[#EBF4EF] px-2 py-0.5 rounded">{rev.rating}</p>
                  <p className="text-[11px] text-[#89766C] mt-1">{rev.date}</p>
                </div>
              </div>

              <div className="mt-3 pt-3 border-t border-[#EAD8C3]">
                <p className="text-[10px] font-bold text-[#89766C] uppercase tracking-wider mb-1.5">{rev.cycle}</p>
                <p className="text-xs text-[#342522] leading-relaxed">&ldquo;{rev.feedback}&rdquo;</p>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

export default Performance;
