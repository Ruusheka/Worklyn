import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  CalendarCheck,
  CalendarDays,
  CreditCard,
  RotateCcw,
  Clock,
  ArrowUpRight,
  PlusCircle,
  PlayCircle,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { employeeApi } from '../api/employeeApi.js';
import { attendanceApi } from '../api/attendanceApi.js';
import { leaveApi } from '../api/leaveApi.js';
import { rewindApi } from '../api/rewindApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function Dashboard() {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [stats, setStats] = useState({
    totalEmployees: 7,
    presentToday: 4,
    pendingLeaves: 1,
    failedWorkflows: 1,
  });

  const [attendanceToday, setAttendanceToday] = useState(null);
  const [leaveBalances, setLeaveBalances] = useState(null);
  const [recentAttendance, setRecentAttendance] = useState([]);
  const [failedRewinds, setFailedRewinds] = useState([]);
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [isAttendanceLoading, setIsAttendanceLoading] = useState(false);

  // Live work clock
  const [liveTime, setLiveTime] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => {
      setLiveTime(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Load dashboard data
  useEffect(() => {
    async function loadData() {
      try {
        const [empRes, attRes, leaveRes, rewindRes, balRes] = await Promise.allSettled([
          employeeApi.getEmployees({ size: 1 }),
          attendanceApi.getMyHistory(),
          leaveApi.getMyRequests(),
          rewindApi.getWorkflows(),
          leaveApi.getBalance()
        ]);

        const empData = empRes.status === 'fulfilled' ? empRes.value : null;
        const attList = Array.isArray(attRes.status === 'fulfilled' ? attRes.value : null) ? attRes.value : [];
        const leaves = Array.isArray(leaveRes.status === 'fulfilled' ? leaveRes.value : null) ? leaveRes.value : [];
        const rewinds = Array.isArray(rewindRes.status === 'fulfilled' ? rewindRes.value : null) ? rewindRes.value : [];
        const balanceRes = balRes.status === 'fulfilled' ? balRes.value : null;

        const today = new Date().toISOString().split('T')[0];
        const todayRec = attList.find(a => a.attendanceDate === today);
        setAttendanceToday(todayRec);
        setIsCheckedIn(Boolean(todayRec && !todayRec.checkOutTime));
        setRecentAttendance(attList.slice(0, 4));

        const pendingCount = leaves.filter(l => l.status === 'PENDING').length;
        const failedWfs = rewinds.filter(w => w.status === 'FAILED');
        setFailedRewinds(failedWfs);
        setLeaveBalances(balanceRes);

        setStats({
          totalEmployees: empData?.totalElements ?? 0,
          presentToday: attList.filter(a => a.status === 'PRESENT' || a.status === 'LATE').length,
          pendingLeaves: pendingCount,
          failedWorkflows: failedWfs.length,
        });
      } catch (err) {
        console.warn('Dashboard statistics partial load notice:', err.message);
      }
    }

    loadData();
  }, []);

  const handleToggleAttendance = async () => {
    setIsAttendanceLoading(true);
    try {
      if (!isCheckedIn) {
        const record = await attendanceApi.checkIn();
        setAttendanceToday(record);
        setIsCheckedIn(true);
        addToast('Checked in successfully for today!', 'success');
      } else {
        const record = await attendanceApi.checkOut();
        setAttendanceToday(record);
        setIsCheckedIn(false);
        addToast('Checked out recorded. Have a good evening!', 'success');
      }
    } catch (err) {
      addToast(err.message || 'Failed to update attendance', 'error');
    } finally {
      setIsAttendanceLoading(false);
    }
  };

  const isAdminOrHr = hasRole('ROLE_ADMIN') || hasRole('ROLE_HR');

  return (
    <div className="space-y-6">
      {/* Top Banner / Greeting */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-[#342522] tracking-tight">
            Good morning, {user?.firstName || 'Colleague'}
          </h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-1">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })} &bull; System operating normally
          </p>
        </div>

        {/* Live Clock & Action */}
        <div className="flex items-center gap-3">
          <div className="bg-[#FBF5EC] border border-[#EAD8C3] px-3.5 py-1.5 rounded-xl shadow-soft flex items-center gap-2">
            <Clock className="w-4 h-4 text-[#702C37]" />
            <span className="text-xs font-mono font-bold text-[#342522]">{liveTime}</span>
          </div>

          <Button
            variant={isCheckedIn ? 'outline' : 'primary'}
            size="md"
            onClick={handleToggleAttendance}
            isLoading={isAttendanceLoading}
            className="shadow-sm"
          >
            {isCheckedIn ? 'Check Out' : 'Check In Today'}
          </Button>
        </div>
      </div>

      {/* KPI Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Active Employees */}
        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase tracking-wider">Total Active Employees</span>
            <div className="w-8 h-8 rounded-lg bg-[#F4EBE6] text-[#541923] flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">{stats.totalEmployees}</span>
            <span className="text-xs font-semibold text-[#387A58] flex items-center">
              +14% <ArrowUpRight className="w-3 h-3" />
            </span>
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">Across 5 departments</p>
        </Card>

        {/* Card 2: Present Today */}
        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase tracking-wider">Present Today</span>
            <div className="w-8 h-8 rounded-lg bg-[#EBF4EF] text-[#387A58] flex items-center justify-center">
              <CalendarCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">{stats.presentToday}</span>
            <span className="text-xs font-semibold text-[#89766C]">/ {stats.totalEmployees} checked in</span>
          </div>
          <p className="text-[11px] text-[#387A58] mt-1 font-medium">92% on-time attendance</p>
        </Card>

        {/* Card 3: Pending Leaves */}
        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase tracking-wider">Pending Leaves</span>
            <div className="w-8 h-8 rounded-lg bg-[#FDF5E8] text-[#D69A43] flex items-center justify-center">
              <CalendarDays className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">{stats.pendingLeaves}</span>
            <span className="text-xs font-semibold text-[#D69A43]">Requires Action</span>
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">1 casual, 1 vacation request</p>
        </Card>

        {/* Card 4: HR-REWIND or Payroll */}
        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase tracking-wider">
              {hasRole('ROLE_ADMIN') ? 'Recovery Workflows' : 'Next Payroll Run'}
            </span>
            <div className="w-8 h-8 rounded-lg bg-[#FBECEB] text-[#541923] flex items-center justify-center">
              {hasRole('ROLE_ADMIN') ? <RotateCcw className="w-4 h-4 text-[#B85C55]" /> : <CreditCard className="w-4 h-4 text-[#541923]" />}
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            {hasRole('ROLE_ADMIN') ? (
              <>
                <span className="text-3xl font-extrabold text-[#342522]">{stats.failedWorkflows}</span>
                <span className="text-xs font-semibold text-[#B85C55]">Failed DAG</span>
              </>
            ) : (
              <>
                <span className="text-2xl font-extrabold text-[#342522]">$588.7k</span>
                <span className="text-xs font-semibold text-[#387A58]">Period 2026-10</span>
              </>
            )}
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">
            {hasRole('ROLE_ADMIN') ? 'Replay & compensation ready' : 'Disbursement scheduled for Oct 31'}
          </p>
        </Card>
      </div>

      {/* Main Section: Chart & Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Attendance Trends Visualization (2 cols) */}
        <Card
          title="Weekly Attendance & Work Hours"
          subtitle="Real-time employee presence across Monday to Friday"
          className="lg:col-span-2"
        >
          <div className="pt-2">
            {/* Custom SVG Bar Chart */}
            <div className="h-64 flex items-end justify-between gap-4 px-2 pt-6 pb-2">
              {[
                { day: 'Mon', present: 7, total: 7, hours: 8.4 },
                { day: 'Tue', present: 6, total: 7, hours: 8.1 },
                { day: 'Wed', present: 7, total: 7, hours: 8.6 },
                { day: 'Thu', present: 5, total: 7, hours: 7.9 },
                { day: 'Fri (Today)', present: 6, total: 7, hours: 8.2 },
              ].map((item, idx) => {
                const heightPct = Math.round((item.present / item.total) * 100);
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center h-full justify-end group">
                    <span className="text-[10px] font-bold text-[#541923] opacity-0 group-hover:opacity-100 transition-opacity mb-1">
                      {item.hours}h avg
                    </span>
                    <div className="w-full max-w-[48px] bg-[#EAD8C3]/50 rounded-t-lg h-full flex items-end overflow-hidden p-1">
                      <div
                        style={{ height: `${heightPct}%` }}
                        className="w-full bg-gradient-to-t from-[#541923] to-[#702C37] rounded-t-md transition-all duration-500 group-hover:from-[#702C37] group-hover:to-[#C9A574]"
                      />
                    </div>
                    <span className="text-xs font-semibold text-[#342522] mt-2">{item.day}</span>
                    <span className="text-[10px] text-[#89766C]">{item.present}/{item.total}</span>
                  </div>
                );
              })}
            </div>

            <div className="mt-4 pt-4 border-t border-[#EAD8C3] flex items-center justify-between text-xs text-[#89766C]">
              <div className="flex items-center gap-4">
                <span className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded bg-[#541923]"></span> Present Hours
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded bg-[#EAD8C3]"></span> Capacity
                </span>
              </div>
              <span className="font-semibold text-[#541923]">Target: 40 hrs / week</span>
            </div>
          </div>
        </Card>

        {/* Right Column: Role Quick Actions & Self Service */}
        <div className="space-y-6">
          {/* Quick Actions Card */}
          <Card
            title={isAdminOrHr ? 'Administrative Actions' : 'My Quick Leave Balances'}
            subtitle={isAdminOrHr ? 'Frequently accessed workflows' : 'Annual paid time off summary'}
          >
            {isAdminOrHr ? (
              <div className="space-y-2.5">
                <button
                  onClick={() => navigate('/employees')}
                  className="w-full flex items-center justify-between p-3 rounded-xl border border-[#EAD8C3] bg-white hover:bg-[#F3E6D6]/40 transition-colors text-left"
                >
                  <div className="flex items-center gap-3">
                    <PlusCircle className="w-4 h-4 text-[#541923]" />
                    <span className="text-xs font-bold text-[#342522]">Provision Employee</span>
                  </div>
                  <span className="text-[11px] text-[#89766C]">EMP-xxxx</span>
                </button>

                <button
                  onClick={() => navigate('/payroll')}
                  className="w-full flex items-center justify-between p-3 rounded-xl border border-[#EAD8C3] bg-white hover:bg-[#F3E6D6]/40 transition-colors text-left"
                >
                  <div className="flex items-center gap-3">
                    <PlayCircle className="w-4 h-4 text-[#702C37]" />
                    <span className="text-xs font-bold text-[#342522]">Process Monthly Payroll</span>
                  </div>
                  <span className="text-[11px] text-[#89766C]">2026-10</span>
                </button>

                <button
                  onClick={() => navigate('/rewind')}
                  className="w-full flex items-center justify-between p-3 rounded-xl border border-[#EAD8C3] bg-white hover:bg-[#F3E6D6]/40 transition-colors text-left"
                >
                  <div className="flex items-center gap-3">
                    <RotateCcw className="w-4 h-4 text-[#B85C55]" />
                    <span className="text-xs font-bold text-[#342522]">HR-REWIND Recovery Center</span>
                  </div>
                  <span className="text-[11px] font-mono text-[#B85C55] font-bold">1 Alert</span>
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="p-3 rounded-xl bg-white border border-[#EAD8C3] flex items-center justify-between">
                  <div>
                    <p className="text-xs font-bold text-[#342522]">Casual Leave</p>
                    <p className="text-[11px] text-[#89766C]">Used: {leaveBalances?.casual?.used || 4} days</p>
                  </div>
                  <span className="text-lg font-bold text-[#387A58]">
                    {leaveBalances?.casual?.remaining || 8} left
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-white border border-[#EAD8C3] flex items-center justify-between">
                  <div>
                    <p className="text-xs font-bold text-[#342522]">Sick Leave</p>
                    <p className="text-[11px] text-[#89766C]">Used: {leaveBalances?.sick?.used || 2} days</p>
                  </div>
                  <span className="text-lg font-bold text-[#387A58]">
                    {leaveBalances?.sick?.remaining || 8} left
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-white border border-[#EAD8C3] flex items-center justify-between">
                  <div>
                    <p className="text-xs font-bold text-[#342522]">Earned Vacation</p>
                    <p className="text-[11px] text-[#89766C]">Used: {leaveBalances?.earned?.used || 5} days</p>
                  </div>
                  <span className="text-lg font-bold text-[#387A58]">
                    {leaveBalances?.earned?.remaining || 10} left
                  </span>
                </div>

                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => navigate('/leave')}
                  className="w-full text-xs font-semibold mt-2"
                >
                  Apply For Leave
                </Button>
              </div>
            )}
          </Card>
        </div>
      </div>

      {/* Bottom Section: Recent Attendance & HR-REWIND Status */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Check-in Logs */}
        <Card
          title="Recent Attendance Activity"
          subtitle="Real-time log of check-in and check-out timestamps"
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate('/attendance')} className="text-xs text-[#541923]">
              View All
            </Button>
          }
        >
          <div className="divide-y divide-[#EAD8C3]/60">
            {recentAttendance.map((rec) => (
              <div key={rec.id} className="py-2.5 flex items-center justify-between">
                <div>
                  <p className="text-xs font-bold text-[#342522]">{rec.employeeName}</p>
                  <p className="text-[11px] text-[#89766C]">
                    Date: {rec.attendanceDate} &bull; Check-in: {rec.checkInTime ? new Date(rec.checkInTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                  </p>
                </div>
                <StatusBadge status={rec.status} />
              </div>
            ))}
          </div>
        </Card>

        {/* HR-REWIND or Company Announcements */}
        <Card
          title="Distributed Engine & Integrity Monitor"
          subtitle="HR-REWIND multi-step DAG state and transactional rollback tracker"
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate('/rewind')} className="text-xs text-[#541923]">
              DAG Inspector
            </Button>
          }
        >
          <div className="space-y-3">
            {failedRewinds.length > 0 ? (
              failedRewinds.map((wf) => (
                <div key={wf.workflowId} className="p-3.5 rounded-xl bg-[#FBECEB] border border-[#B85C55]/30">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-mono font-bold text-[#541923]">
                      #{wf.workflowId} &bull; {wf.workflowType}
                    </span>
                    <StatusBadge status="FAILED" />
                  </div>
                  <p className="text-xs text-[#342522] font-semibold">{wf.failureReason}</p>
                  <div className="mt-2 flex items-center justify-between text-[11px] text-[#89766C]">
                    <span>Failed Step: <code className="font-mono text-[#B85C55] font-bold">{wf.failedStepId}</code></span>
                    <button
                      onClick={() => navigate('/rewind')}
                      className="font-bold text-[#541923] hover:underline"
                    >
                      Inspect Recovery DAG &rarr;
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-6 text-center text-[#387A58] bg-[#EBF4EF] rounded-xl border border-[#387A58]/20 text-xs font-medium">
                <CheckCircle2 className="w-5 h-5 mx-auto mb-1 text-[#387A58]" />
                All distributed workflows and idempotency locks are healthy and consistent.
              </div>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}

export default Dashboard;
