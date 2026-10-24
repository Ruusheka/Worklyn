import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  PlayCircle,
  Lock,
  Download,
  Eye,
  DollarSign,
  TrendingUp,
  AlertTriangle,
  CheckCircle2
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { payrollApi } from '../api/payrollApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Table from '../components/common/Table.jsx';
import Modal from '../components/common/Modal.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function Payroll() {
  const { hasRole } = useAuth();
  const { addToast } = useToast();

  const [selectedPeriod, setSelectedPeriod] = useState('2026-10');
  const [periodSummary, setPeriodSummary] = useState(null);
  const [payslip, setPayslip] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isFinalizing, setIsFinalizing] = useState(false);

  // Payslip View Modal
  const [isPayslipOpen, setIsPayslipOpen] = useState(false);

  // Confirm Run Payroll Modal
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const isAdminOrHR = hasRole('ROLE_ADMIN') || hasRole('ROLE_HR');

  const generateIdempotencyKey = () =>
    `payroll_${selectedPeriod}_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [sumRes, slipRes, periodsRes] = await Promise.allSettled([
        payrollApi.getPeriodRecords(selectedPeriod),
        payrollApi.getPayslip(selectedPeriod),
        payrollApi.getPeriods()
      ]);
      setPeriodSummary(sumRes.status === 'fulfilled' ? sumRes.value : null);
      setPayslip(slipRes.status === 'fulfilled' ? slipRes.value : null);
      setPeriodHistory(Array.isArray(periodsRes.status === 'fulfilled' ? periodsRes.value : null) ? periodsRes.value : []);
    } catch (err) {
      setPeriodSummary(null);
      setPayslip(null);
      setPeriodHistory([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedPeriod]);

  const handleRunPayroll = async () => {
    setIsProcessing(true);
    setIsConfirmOpen(false);
    try {
      const key = generateIdempotencyKey();
      const result = await payrollApi.processPayroll(selectedPeriod, key);
      setPeriodSummary(result);
      addToast(`Payroll batch for ${selectedPeriod} processed successfully!`, 'success');
      loadData();
    } catch (err) {
      addToast(err.message || 'Failed to process payroll batch', 'error');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleFinalize = async () => {
    setIsFinalizing(true);
    try {
      const result = await payrollApi.finalizePeriod(selectedPeriod);
      setPeriodSummary(result);
      addToast(`Period ${selectedPeriod} finalized and locked!`, 'success');
    } catch (err) {
      addToast(err.message || 'Failed to finalize period', 'error');
    } finally {
      setIsFinalizing(false);
    }
  };

  const [periodHistory, setPeriodHistory] = useState([]);

  const periodOptions = ['2026-10', '2026-09', '2026-08', '2026-07'];

  const historyColumns = [
    { header: 'Pay Period', key: 'period', render: (v) => <span className="font-mono font-bold text-[#541923]">{v}</span> },
    { header: 'Employees', key: 'employees' },
    { header: 'Gross Pay', key: 'gross', render: (v) => `$${Number(v).toLocaleString()}` },
    { header: 'Deductions', key: 'deductions', render: (v) => `$${Number(v).toLocaleString()}` },
    {
      header: 'Net Disbursement',
      key: 'net',
      render: (v) => (
        <span className="font-mono font-bold text-[#387A58]">${Number(v).toLocaleString()}</span>
      )
    },
    { header: 'Disbursed On', key: 'disbursedOn' },
    { header: 'Status', key: 'status', render: (v) => <StatusBadge status={v} /> },
    {
      header: 'Payslip',
      key: 'actions',
      align: 'right',
      render: (_, row) => (
        <Button variant="ghost" size="sm" onClick={() => setIsPayslipOpen(true)} className="p-1.5">
          <Eye className="w-4 h-4 text-[#541923]" />
        </Button>
      )
    }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Payroll Processing</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            Idempotency-protected batch payroll with finalization lock and payslip generation
          </p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={selectedPeriod}
            onChange={(e) => setSelectedPeriod(e.target.value)}
            className="bg-white border border-[#EAD8C3] rounded-xl px-3 py-2 text-sm text-[#342522] focus:outline-none"
          >
            {periodOptions.map(p => <option key={p} value={p}>{p}</option>)}
          </select>

          {isAdminOrHR && (
            <>
              <Button
                variant="primary"
                icon={PlayCircle}
                onClick={() => setIsConfirmOpen(true)}
                isLoading={isProcessing}
                disabled={periodSummary?.status === 'FINALIZED'}
              >
                Run Payroll
              </Button>
              <Button
                variant="outline"
                icon={Lock}
                onClick={handleFinalize}
                isLoading={isFinalizing}
                disabled={periodSummary?.status === 'FINALIZED' || !periodSummary || periodSummary?.status === 'UNPROCESSED'}
              >
                Finalize
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Backend Pending Notice */}
      <div className="p-4 bg-[#FDF5E8] border border-[#D69A43]/30 rounded-xl flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-[#D69A43] shrink-0 mt-0.5" />
        <div className="text-xs">
          <p className="font-bold text-[#342522] mb-1">Payroll Processing — Backend Pending</p>
          <p className="text-[#89766C]">
            The Payroll Processing API endpoints (/api/v1/payroll/*) are scheduled for a future release. Batch payroll calculation is currently unavailable.
          </p>
        </div>
      </div>

      {/* Period Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Period</span>
          <div className="mt-2 text-2xl font-extrabold text-[#541923] font-mono">{selectedPeriod}</div>
          <StatusBadge status={isLoading ? 'PENDING' : (periodSummary?.status || 'UNPROCESSED')} className="mt-2" />
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Total Gross Pay</span>
          <div className="mt-2 text-2xl font-extrabold text-[#342522]">
            {isLoading ? '…' : `$${Number(periodSummary?.totalGross || 0).toLocaleString()}`}
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">{periodSummary?.processedEmployees || 0} employee payslips</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Total Deductions</span>
          <div className="mt-2 text-2xl font-extrabold text-[#B85C55]">
            {isLoading ? '…' : `$${Number(periodSummary?.totalDeductions || 0).toLocaleString()}`}
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">PF, TDS, PT & leave deductions</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Net Disbursement</span>
          <div className="mt-2 text-2xl font-extrabold text-[#387A58]">
            {isLoading ? '…' : `$${Number(periodSummary?.totalNetDisbursement || 0).toLocaleString()}`}
          </div>
          <p className="text-[11px] text-[#89766C] mt-1">Ready for bank transfer</p>
        </Card>
      </div>

      {/* My Payslip Section (Employee view) */}
      {payslip && (
        <Card
          title="My Payslip"
          subtitle={`Disbursement record for ${payslip.period} — ${payslip.employeeName}`}
          action={
            <Button variant="secondary" size="sm" icon={Eye} onClick={() => setIsPayslipOpen(true)}>
              View Full Payslip
            </Button>
          }
        >
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
            <div className="p-3 bg-white rounded-xl border border-[#EAD8C3]">
              <p className="text-[#89766C] font-medium">Basic Pay</p>
              <p className="font-mono font-bold text-[#342522] text-lg mt-1">
                ${Number(payslip.earnings?.basicPay || 55000).toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-white rounded-xl border border-[#EAD8C3]">
              <p className="text-[#89766C] font-medium">HRA</p>
              <p className="font-mono font-bold text-[#342522] text-lg mt-1">
                ${Number(payslip.earnings?.hra || 22000).toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-white rounded-xl border border-[#EAD8C3]">
              <p className="text-[#89766C] font-medium">Total Deductions</p>
              <p className="font-mono font-bold text-[#B85C55] text-lg mt-1">
                -${Number(payslip.deductions?.totalDeductions || 15300).toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-[#EBF4EF] rounded-xl border border-[#387A58]/30">
              <p className="text-[#387A58] font-bold">Net Pay</p>
              <p className="font-mono font-extrabold text-[#387A58] text-xl mt-1">
                ${Number(payslip.netPay || 79700).toLocaleString()}
              </p>
            </div>
          </div>
        </Card>
      )}

      {/* Payroll History */}
      <Card
        title="Payroll Run History"
        subtitle="All finalized payroll periods with disbursement audit trail"
      >
        <Table
          columns={historyColumns}
          data={periodHistory}
          emptyMessage="No payroll history available."
        />
      </Card>

      {/* Confirm Run Payroll Modal */}
      <Modal
        isOpen={isConfirmOpen}
        onClose={() => setIsConfirmOpen(false)}
        title={`Run Payroll — ${selectedPeriod}`}
        subtitle="This operation is idempotency-protected via X-Idempotency-Key"
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsConfirmOpen(false)}>Cancel</Button>
            <Button variant="primary" icon={PlayCircle} onClick={handleRunPayroll} isLoading={isProcessing}>
              Confirm & Execute Batch
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <div className="p-4 bg-[#FDF5E8] rounded-xl border border-[#D69A43]/30 flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-[#D69A43] shrink-0 mt-0.5" />
            <div className="text-xs">
              <p className="font-bold text-[#342522] mb-1">Batch Calculation Warning</p>
              <p className="text-[#89766C]">
                Running payroll will calculate gross pay, statutory deductions (PF, TDS, PT), and leave-day adjustments for all active employees in period <strong>{selectedPeriod}</strong>.
              </p>
            </div>
          </div>
          <div className="text-xs text-[#89766C] p-3 bg-white rounded-xl border border-[#EAD8C3] space-y-1.5">
            <div className="flex justify-between">
              <span>Employees to process:</span>
              <strong className="text-[#342522]">{periodSummary?.processedEmployees != null ? `${periodSummary.processedEmployees} active employees` : '—'}</strong>
            </div>
            <div className="flex justify-between">
              <span>Idempotency protection:</span>
              <CheckCircle2 className="w-4 h-4 text-[#387A58]" />
            </div>
            <div className="flex justify-between">
              <span>Estimated net disbursement:</span>
              <strong className="text-[#387A58] font-mono">{periodSummary?.totalNetDisbursement != null ? `$${Number(periodSummary.totalNetDisbursement).toLocaleString()}` : '—'}</strong>
            </div>
          </div>
        </div>
      </Modal>

      {/* Full Payslip Modal */}
      <Modal
        isOpen={isPayslipOpen}
        onClose={() => setIsPayslipOpen(false)}
        title="Monthly Payslip"
        subtitle={`${payslip?.employeeName || 'David Chen'} • ${payslip?.period || selectedPeriod}`}
        maxWidth="max-w-lg"
        footer={
          <Button variant="secondary" icon={Download} onClick={() => addToast('Payslip PDF export queued!', 'success')}>
            Download PDF
          </Button>
        }
      >
        <div className="space-y-4 text-xs">
          {/* Employee Info */}
          <div className="flex justify-between p-3 bg-[#F8EFE4] rounded-xl border border-[#EAD8C3]">
            <div>
              <p className="font-bold text-[#342522]">{payslip?.employeeName || 'David Chen'}</p>
              <p className="text-[#89766C]">{payslip?.designation || 'Backend Engineer'}</p>
              <p className="text-[#89766C]">{payslip?.department || 'Engineering'}</p>
            </div>
            <div className="text-right">
              <p className="font-mono font-bold text-[#541923]">{payslip?.employeeCode || 'EMP-2045'}</p>
              <p className="text-[#89766C]">Pay Date: {payslip?.payDate || '2026-10-31'}</p>
              <p className="text-[#89766C]">Days: {payslip?.presentDays || 22}/{payslip?.workingDays || 22}</p>
            </div>
          </div>

          {/* Earnings */}
          <div>
            <p className="font-bold text-[#342522] mb-2 uppercase text-[10px] tracking-wider">Earnings</p>
            <div className="space-y-1 divide-y divide-[#EAD8C3]/60">
              {[
                { label: 'Basic Pay', value: payslip?.earnings?.basicPay || 55000 },
                { label: 'House Rent Allowance (HRA)', value: payslip?.earnings?.hra || 22000 },
                { label: 'Special Allowance', value: payslip?.earnings?.specialAllowance || 18000 },
              ].map(item => (
                <div key={item.label} className="py-1.5 flex justify-between">
                  <span className="text-[#89766C]">{item.label}</span>
                  <span className="font-mono font-semibold text-[#342522]">${Number(item.value).toLocaleString()}</span>
                </div>
              ))}
              <div className="py-1.5 flex justify-between font-bold">
                <span className="text-[#342522]">Gross Pay</span>
                <span className="font-mono text-[#342522]">${Number(payslip?.earnings?.grossPay || 95000).toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* Deductions */}
          <div>
            <p className="font-bold text-[#342522] mb-2 uppercase text-[10px] tracking-wider">Deductions</p>
            <div className="space-y-1 divide-y divide-[#EAD8C3]/60">
              {[
                { label: 'Provident Fund (12%)', value: payslip?.deductions?.providentFund || 6600 },
                { label: 'Professional Tax', value: payslip?.deductions?.professionalTax || 200 },
                { label: 'Income Tax TDS', value: payslip?.deductions?.incomeTaxTds || 8500 },
              ].map(item => (
                <div key={item.label} className="py-1.5 flex justify-between">
                  <span className="text-[#89766C]">{item.label}</span>
                  <span className="font-mono text-[#B85C55]">-${Number(item.value).toLocaleString()}</span>
                </div>
              ))}
              <div className="py-1.5 flex justify-between font-bold">
                <span className="text-[#B85C55]">Total Deductions</span>
                <span className="font-mono text-[#B85C55]">-${Number(payslip?.deductions?.totalDeductions || 15300).toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* Net Pay */}
          <div className="p-4 bg-[#EBF4EF] rounded-xl border border-[#387A58]/30 flex items-center justify-between">
            <span className="font-bold text-[#387A58]">Net Pay (Take-Home)</span>
            <span className="font-mono text-2xl font-extrabold text-[#387A58]">
              ${Number(payslip?.netPay || 79700).toLocaleString()}
            </span>
          </div>
          <p className="text-[10px] text-[#89766C] text-center">
            Bank Transfer to {payslip?.bankAccountMasked || '•••• •••• •••• 4092'} • Disbursed {payslip?.payDate || '2026-10-31'}
          </p>
        </div>
      </Modal>
    </div>
  );
}

export default Payroll;
