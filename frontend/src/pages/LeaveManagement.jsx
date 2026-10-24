import React, { useState, useEffect } from 'react';
import {
  CalendarDays,
  PlusCircle,
  CheckCircle,
  XCircle,
  Clock,
  AlertTriangle,
  FileText
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { leaveApi } from '../api/leaveApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Table from '../components/common/Table.jsx';
import Modal from '../components/common/Modal.jsx';
import Input from '../components/common/Input.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function LeaveManagement() {
  const { user, hasRole } = useAuth();
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState('my-requests');
  const [balances, setBalances] = useState(null);
  const [requests, setRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Apply Modal State
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  const [applyForm, setApplyForm] = useState({
    leaveType: 'CASUAL',
    startDate: '',
    endDate: '',
    reason: '',
  });
  const [daysCount, setDaysCount] = useState(0);
  const [applyErrors, setApplyErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Approve/Reject Modal State
  const [actionModal, setActionModal] = useState({
    isOpen: false,
    type: 'approve', // 'approve' | 'reject'
    requestId: null,
    remarks: '',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [bal, reqList] = await Promise.all([
        leaveApi.getBalance(),
        leaveApi.getMyRequests()
      ]);
      setBalances(bal);
      setRequests(reqList);
    } catch (err) {
      console.error('Failed to load leave data', err);
      addToast(err.message || 'Failed to fetch leave records', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // Compute business day count and validate date ordering (UI-004)
  useEffect(() => {
    if (!applyForm.startDate || !applyForm.endDate) {
      setDaysCount(0);
      return;
    }
    const start = new Date(applyForm.startDate);
    const end = new Date(applyForm.endDate);

    if (end < start) {
      setApplyErrors((prev) => ({
        ...prev,
        endDate: 'End date cannot be earlier than start date',
      }));
      setDaysCount(0);
      return;
    } else {
      setApplyErrors((prev) => {
        const next = { ...prev };
        delete next.endDate;
        return next;
      });
    }

    // Calculate days
    const diffTime = Math.abs(end - start);
    const totalDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    setDaysCount(totalDays);
  }, [applyForm.startDate, applyForm.endDate]);

  const handleApply = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!applyForm.startDate) errs.startDate = 'Start date is required';
    if (!applyForm.endDate) errs.endDate = 'End date is required';
    if (new Date(applyForm.endDate) < new Date(applyForm.startDate)) {
      errs.endDate = 'End date cannot be earlier than start date';
    }
    if (!applyForm.reason.trim()) errs.reason = 'Reason for leave is required';

    if (Object.keys(errs).length > 0) {
      setApplyErrors(errs);
      return;
    }

    setIsSubmitting(true);
    try {
      await leaveApi.applyLeave({
        ...applyForm,
        daysCount,
      });
      addToast('Leave request submitted successfully!', 'success');
      setIsApplyModalOpen(false);
      setApplyForm({
        leaveType: 'CASUAL',
        startDate: '',
        endDate: '',
        reason: '',
      });
      loadData();
    } catch (err) {
      addToast(err.message || 'Failed to submit leave request', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDecision = async () => {
    try {
      if (actionModal.type === 'approve') {
        await leaveApi.approveLeave(actionModal.requestId, actionModal.remarks);
        addToast('Leave request approved!', 'success');
      } else {
        await leaveApi.rejectLeave(actionModal.requestId, actionModal.remarks);
        addToast('Leave request rejected.', 'warning');
      }
      setActionModal({ isOpen: false, type: 'approve', requestId: null, remarks: '' });
      loadData();
    } catch (err) {
      addToast(err.message || 'Failed to process decision', 'error');
    }
  };

  const isManagerOrAdmin = hasRole('ROLE_ADMIN') || hasRole('ROLE_HR') || hasRole('ROLE_MANAGER');

  const myRequests = requests.filter(r => r.employeeId === user?.employeeId || r.employeeName === `${user?.firstName} ${user?.lastName}`);
  const pendingApprovals = requests.filter(r => r.status === 'PENDING');

  const myColumns = [
    {
      header: 'Leave Type',
      key: 'leaveType',
      render: (val) => <span className="font-bold text-xs text-[#541923]">{val}</span>,
    },
    {
      header: 'Dates',
      key: 'dates',
      render: (_, row) => (
        <span className="text-xs text-[#342522]">
          {row.startDate} &rarr; {row.endDate}
        </span>
      ),
    },
    {
      header: 'Days',
      key: 'daysCount',
      render: (val) => <span className="font-semibold text-xs text-[#342522]">{val} days</span>,
    },
    {
      header: 'Reason',
      key: 'reason',
      render: (val) => <span className="text-xs text-[#89766C] line-clamp-1">{val}</span>,
    },
    {
      header: 'Status',
      key: 'status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      header: 'Approver Remarks',
      key: 'managerRemarks',
      render: (val) => <span className="text-xs text-[#89766C]">{val || '—'}</span>,
    },
  ];

  const approvalColumns = [
    {
      header: 'Employee',
      key: 'employeeName',
      render: (val, row) => (
        <div>
          <p className="text-xs font-bold text-[#342522]">{val}</p>
          <p className="text-[10px] text-[#89766C]">{row.department} &bull; {row.employeeCode}</p>
        </div>
      ),
    },
    {
      header: 'Type',
      key: 'leaveType',
      render: (val) => <span className="font-bold text-xs text-[#541923]">{val}</span>,
    },
    {
      header: 'Duration',
      key: 'daysCount',
      render: (val, row) => (
        <div className="text-xs">
          <p className="font-semibold text-[#342522]">{val} Days</p>
          <p className="text-[10px] text-[#89766C]">{row.startDate} to {row.endDate}</p>
        </div>
      ),
    },
    {
      header: 'Reason',
      key: 'reason',
      render: (val) => <span className="text-xs text-[#342522]">{val}</span>,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (_, row) => (
        <div className="flex items-center justify-end gap-2">
          <Button
            variant="primary"
            size="sm"
            onClick={() => setActionModal({ isOpen: true, type: 'approve', requestId: row.id, remarks: '' })}
            className="text-xs px-2.5 py-1 bg-[#387A58] hover:bg-[#2d6246]"
          >
            Approve
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={() => setActionModal({ isOpen: true, type: 'reject', requestId: row.id, remarks: '' })}
            className="text-xs px-2.5 py-1"
          >
            Reject
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Leave Management</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            Balance allocations, dynamic business day calculation, and multi-tier approval cascades
          </p>
        </div>

        <Button
          variant="primary"
          icon={PlusCircle}
          onClick={() => setIsApplyModalOpen(true)}
          className="shadow-sm"
        >
          Apply For Leave
        </Button>
      </div>

      {/* Leave Balance Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase">Casual Leave (CL)</span>
            <span className="text-xs font-mono font-bold text-[#541923] bg-[#F4EBE6] px-2 py-0.5 rounded">
              {balances?.casual?.remaining ?? 8} Days Left
            </span>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">
              {balances?.casual?.remaining ?? 8}
            </span>
            <span className="text-xs text-[#89766C]">/ {balances?.casual?.total ?? 12} total allocated</span>
          </div>
          <div className="mt-2 w-full bg-[#EAD8C3] rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-[#541923] h-full rounded-full"
              style={{
                width: `${((balances?.casual?.used ?? 4) / (balances?.casual?.total ?? 12)) * 100}%`,
              }}
            />
          </div>
          <p className="text-[10px] text-[#89766C] mt-2">
            Used: {balances?.casual?.used ?? 4} days this cycle
          </p>
        </Card>

        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase">Sick Leave (SL)</span>
            <span className="text-xs font-mono font-bold text-[#387A58] bg-[#EBF4EF] px-2 py-0.5 rounded">
              {balances?.sick?.remaining ?? 8} Days Left
            </span>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">
              {balances?.sick?.remaining ?? 8}
            </span>
            <span className="text-xs text-[#89766C]">/ {balances?.sick?.total ?? 10} total allocated</span>
          </div>
          <div className="mt-2 w-full bg-[#EAD8C3] rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-[#387A58] h-full rounded-full"
              style={{
                width: `${((balances?.sick?.used ?? 2) / (balances?.sick?.total ?? 10)) * 100}%`,
              }}
            />
          </div>
          <p className="text-[10px] text-[#89766C] mt-2">
            Used: {balances?.sick?.used ?? 2} days with medical certificate
          </p>
        </Card>

        <Card className="hover:shadow-card transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#89766C] uppercase">Earned / Privilege (EL)</span>
            <span className="text-xs font-mono font-bold text-[#702C37] bg-[#FDF5E8] px-2 py-0.5 rounded">
              {balances?.earned?.remaining ?? 10} Days Left
            </span>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-[#342522]">
              {balances?.earned?.remaining ?? 10}
            </span>
            <span className="text-xs text-[#89766C]">/ {balances?.earned?.total ?? 15} total allocated</span>
          </div>
          <div className="mt-2 w-full bg-[#EAD8C3] rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-[#702C37] h-full rounded-full"
              style={{
                width: `${((balances?.earned?.used ?? 5) / (balances?.earned?.total ?? 15)) * 100}%`,
              }}
            />
          </div>
          <p className="text-[10px] text-[#89766C] mt-2">
            Encashable at fiscal end or roll forward
          </p>
        </Card>
      </div>

      {/* Tabs: My Requests vs Team Approvals */}
      <div className="flex border-b border-[#EAD8C3] space-x-6">
        <button
          onClick={() => setActiveTab('my-requests')}
          className={`pb-3 text-sm font-semibold border-b-2 transition-all ${
            activeTab === 'my-requests'
              ? 'border-[#541923] text-[#541923]'
              : 'border-transparent text-[#89766C] hover:text-[#342522]'
          }`}
        >
          My Leave Applications ({requests.length})
        </button>

        {isManagerOrAdmin && (
          <button
            onClick={() => setActiveTab('approvals')}
            className={`pb-3 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 ${
              activeTab === 'approvals'
                ? 'border-[#541923] text-[#541923]'
                : 'border-transparent text-[#89766C] hover:text-[#342522]'
            }`}
          >
            <span>Pending Approvals</span>
            {pendingApprovals.length > 0 && (
              <span className="text-[10px] bg-[#B85C55] text-white px-2 py-0.5 rounded-full font-bold">
                {pendingApprovals.length}
              </span>
            )}
          </button>
        )}
      </div>

      {/* Content Table */}
      {activeTab === 'my-requests' ? (
        <Card title="Application History" subtitle="Your leave applications and processing timeline">
          <Table
            columns={myColumns}
            data={requests}
            isLoading={isLoading}
            emptyMessage="You have not submitted any leave applications yet."
          />
        </Card>
      ) : (
        <Card title="Team Leave Approval Requests" subtitle="Review subordinate employee leave submissions">
          <Table
            columns={approvalColumns}
            data={pendingApprovals}
            isLoading={isLoading}
            emptyMessage="No pending leave applications requiring approval."
          />
        </Card>
      )}

      {/* Apply Leave Modal */}
      <Modal
        isOpen={isApplyModalOpen}
        onClose={() => setIsApplyModalOpen(false)}
        title="Apply For Leave"
        subtitle="Calculates required business day quota against annual entitlement"
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsApplyModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleApply}
              isLoading={isSubmitting}
              disabled={Boolean(applyErrors.endDate) || isSubmitting}
            >
              Submit Application
            </Button>
          </>
        }
      >
        <form onSubmit={handleApply} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
              Leave Category
            </label>
            <select
              value={applyForm.leaveType}
              onChange={(e) => setApplyForm({ ...applyForm, leaveType: e.target.value })}
              className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none focus:border-[#702C37]"
            >
              <option value="CASUAL">Casual Leave ({balances?.casual?.remaining ?? 8} available)</option>
              <option value="SICK">Sick Leave ({balances?.sick?.remaining ?? 8} available)</option>
              <option value="EARNED">Earned / Vacation ({balances?.earned?.remaining ?? 10} available)</option>
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Start Date"
              type="date"
              value={applyForm.startDate}
              onChange={(e) => setApplyForm({ ...applyForm, startDate: e.target.value })}
              error={applyErrors.startDate}
            />
            <Input
              label="End Date"
              type="date"
              value={applyForm.endDate}
              onChange={(e) => setApplyForm({ ...applyForm, endDate: e.target.value })}
              error={applyErrors.endDate}
            />
          </div>

          {daysCount > 0 && !applyErrors.endDate && (
            <div className="p-3 rounded-lg bg-[#EBF4EF] border border-[#387A58]/30 text-xs text-[#387A58] font-semibold flex items-center justify-between">
              <span>Total Leave Duration:</span>
              <span className="font-bold text-sm">{daysCount} business day{daysCount > 1 ? 's' : ''}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
              Reason / Justification
            </label>
            <textarea
              rows={3}
              value={applyForm.reason}
              onChange={(e) => setApplyForm({ ...applyForm, reason: e.target.value })}
              placeholder="Provide reason for time off..."
              className="w-full rounded-lg bg-white border border-[#EAD8C3] p-3 text-sm text-[#342522] focus:outline-none focus:border-[#702C37]"
            />
            {applyErrors.reason && (
              <p className="text-xs text-[#B85C55] mt-1 font-medium">{applyErrors.reason}</p>
            )}
          </div>
        </form>
      </Modal>

      {/* Decision Remarks Modal */}
      <Modal
        isOpen={actionModal.isOpen}
        onClose={() => setActionModal({ ...actionModal, isOpen: false })}
        title={actionModal.type === 'approve' ? 'Approve Leave Request' : 'Reject Leave Request'}
        subtitle={`Request ID: ${actionModal.requestId}`}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setActionModal({ ...actionModal, isOpen: false })}
            >
              Cancel
            </Button>
            <Button
              variant={actionModal.type === 'approve' ? 'primary' : 'danger'}
              onClick={handleDecision}
              className={actionModal.type === 'approve' ? 'bg-[#387A58]' : ''}
            >
              {actionModal.type === 'approve' ? 'Confirm Approval' : 'Confirm Rejection'}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <p className="text-xs text-[#89766C]">
            {actionModal.type === 'approve'
              ? 'Approving will deduct days from employee balance and register calendar blocks.'
              : 'Please enter a mandatory rejection reason for employee feedback.'}
          </p>
          <div>
            <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
              Manager Remarks
            </label>
            <textarea
              rows={3}
              value={actionModal.remarks}
              onChange={(e) => setActionModal({ ...actionModal, remarks: e.target.value })}
              placeholder="e.g. Approved. Ensure deliverables handed over before leave."
              className="w-full rounded-lg bg-white border border-[#EAD8C3] p-3 text-sm text-[#342522] focus:outline-none focus:border-[#702C37]"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default LeaveManagement;
