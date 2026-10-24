import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Mail,
  Phone,
  Building,
  Briefcase,
  Calendar,
  DollarSign,
  Shield,
  Edit,
  Clock,
  CalendarDays,
  FileText,
  MapPin,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { employeeApi } from '../api/employeeApi.js';
import { attendanceApi } from '../api/attendanceApi.js';
import { leaveApi } from '../api/leaveApi.js';
import { payrollApi } from '../api/payrollApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import Table from '../components/common/Table.jsx';
import Modal from '../components/common/Modal.jsx';
import Input from '../components/common/Input.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function EmployeeDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const { addToast } = useToast();

  const [employee, setEmployee] = useState(null);
  const [activeTab, setActiveTab] = useState('profile');
  const [isLoading, setIsLoading] = useState(true);

  // Tab Data
  const [attendanceLogs, setAttendanceLogs] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [payslips, setPayslips] = useState([]);

  // Edit / Status Modal
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editForm, setEditForm] = useState({
    designation: '',
    department: '',
    phone: '',
    status: 'ACTIVE',
    baseSalary: '',
  });

  useEffect(() => {
    async function loadData() {
      setIsLoading(true);
      try {
        const emp = await employeeApi.getEmployeeById(id);
        setEmployee(emp);
        setEditForm({
          designation: emp.designation || '',
          department: emp.department || 'ENGINEERING',
          phone: emp.phone || '',
          status: emp.status || 'ACTIVE',
          baseSalary: emp.baseSalary || '',
        });

        const [attRes, leaveRes, payRes] = await Promise.all([
          attendanceApi.getEmployeeHistory(emp.id),
          leaveApi.getMyRequests(),
          payrollApi.getPayslip()
        ]);

        setAttendanceLogs(attRes);
        setLeaveRequests(leaveRes.filter(l => l.employeeId === emp.id || l.employeeCode === emp.employeeCode));
        setPayslips(payRes ? [payRes] : []);
      } catch (err) {
        console.error('Failed to load employee details', err);
        addToast(err.message || 'Failed to load employee profile', 'error');
      } finally {
        setIsLoading(false);
      }
    }

    if (id) {
      loadData();
    }
  }, [id]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      const updated = await employeeApi.updateEmployee(employee.id, {
        ...editForm,
        baseSalary: parseFloat(editForm.baseSalary) || employee.baseSalary,
      });
      setEmployee(updated);
      setIsEditModalOpen(false);
      addToast('Employee profile updated successfully!', 'success');
    } catch (err) {
      addToast(err.message || 'Failed to update profile', 'error');
    }
  };

  if (isLoading) {
    return (
      <div className="py-20 text-center text-[#89766C]">
        <div className="inline-block animate-spin h-8 w-8 border-2 border-[#541923] border-t-transparent rounded-full mb-3"></div>
        <p className="text-sm">Retrieving employee records...</p>
      </div>
    );
  }

  if (!employee) {
    return (
      <div className="py-16 text-center">
        <h2 className="text-xl font-bold text-[#342522]">Employee record not found</h2>
        <Button variant="primary" className="mt-4" onClick={() => navigate('/employees')}>
          Return to Directory
        </Button>
      </div>
    );
  }

  const canEdit = hasRole('ROLE_ADMIN') || hasRole('ROLE_HR');

  return (
    <div className="space-y-6">
      {/* Back button */}
      <div>
        <button
          onClick={() => navigate('/employees')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#541923] hover:underline"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Employee Directory
        </button>
      </div>

      {/* Header Profile Summary Card */}
      <Card bodyClassName="p-6">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center gap-4">
            <img
              src={
                employee.avatar ||
                'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'
              }
              alt={employee.firstName}
              className="w-18 h-18 rounded-2xl object-cover border-2 border-[#EAD8C3] shadow-soft"
            />
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-2xl font-extrabold text-[#342522]">
                  {employee.firstName} {employee.lastName}
                </h1>
                <StatusBadge status={employee.status} />
              </div>
              <p className="text-sm text-[#702C37] font-semibold mt-0.5">
                {employee.designation} &bull; {employee.department}
              </p>
              <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-[#89766C]">
                <span className="font-mono bg-[#F4EBE6] text-[#541923] font-bold px-2 py-0.5 rounded">
                  {employee.employeeCode}
                </span>
                <span className="flex items-center gap-1">
                  <Mail className="w-3.5 h-3.5" /> {employee.email}
                </span>
                {employee.phone && (
                  <span className="flex items-center gap-1">
                    <Phone className="w-3.5 h-3.5" /> {employee.phone}
                  </span>
                )}
              </div>
            </div>
          </div>

          {canEdit && (
            <Button
              variant="outline"
              size="sm"
              icon={Edit}
              onClick={() => setIsEditModalOpen(true)}
            >
              Edit Profile
            </Button>
          )}
        </div>
      </Card>

      {/* Navigation Tabs */}
      <div className="flex border-b border-[#EAD8C3] space-x-6">
        {[
          { id: 'profile', label: 'Job & Personal Info', icon: Briefcase },
          { id: 'attendance', label: 'Attendance Records', icon: Clock },
          { id: 'leave', label: 'Leaves Taken', icon: CalendarDays },
          { id: 'payroll', label: 'Payroll & Payslips', icon: FileText },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 pb-3 text-xs sm:text-sm font-semibold border-b-2 transition-all ${
                isActive
                  ? 'border-[#541923] text-[#541923]'
                  : 'border-transparent text-[#89766C] hover:text-[#342522]'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab 1: Profile Details */}
      {activeTab === 'profile' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card title="Job & Employment Details">
            <dl className="divide-y divide-[#EAD8C3]/60 text-xs">
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Department</dt>
                <dd className="font-semibold text-[#342522]">{employee.department}</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Designation</dt>
                <dd className="font-semibold text-[#342522]">{employee.designation}</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Date of Joining</dt>
                <dd className="font-semibold text-[#342522]">{employee.dateOfJoining || '2024-01-15'}</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Base Salary</dt>
                <dd className="font-mono font-bold text-[#387A58]">
                  ${Number(employee.baseSalary || 95000).toLocaleString()} / year
                </dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Reporting Manager</dt>
                <dd className="font-semibold text-[#342522]">{employee.managerId ? 'Elena Rostova (EMP-3088)' : 'Executive Board'}</dd>
              </div>
            </dl>
          </Card>

          <Card title="Personal & Emergency Contact">
            <dl className="divide-y divide-[#EAD8C3]/60 text-xs">
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Residential Address</dt>
                <dd className="font-semibold text-[#342522] text-right">{employee.address || '742 Evergreen Terr, Seattle, WA'}</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Emergency Contact</dt>
                <dd className="font-semibold text-[#342522]">{employee.emergencyContact || 'Robert Jenkins (+1 555-901-2345)'}</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">Work Authorization</dt>
                <dd className="font-semibold text-[#387A58]">Citizen / Valid Work Permit</dd>
              </div>
              <div className="py-2.5 flex justify-between">
                <dt className="text-[#89766C] font-medium">System UUID</dt>
                <dd className="font-mono text-[11px] text-[#89766C]">{employee.id}</dd>
              </div>
            </dl>
          </Card>
        </div>
      )}

      {/* Tab 2: Attendance Records */}
      {activeTab === 'attendance' && (
        <Card title="Attendance Log History" subtitle={`Recorded check-in & check-out entries for ${employee.firstName}`}>
          <Table
            columns={[
              { header: 'Date', key: 'attendanceDate' },
              {
                header: 'Check In',
                key: 'checkInTime',
                render: (val) => val ? new Date(val).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'
              },
              {
                header: 'Check Out',
                key: 'checkOutTime',
                render: (val) => val ? new Date(val).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Active'
              },
              {
                header: 'Work Duration',
                key: 'durationMinutes',
                render: (val) => val ? `${Math.floor(val / 60)}h ${val % 60}m` : 'In Progress'
              },
              {
                header: 'Status',
                key: 'status',
                render: (val) => <StatusBadge status={val} />
              }
            ]}
            data={attendanceLogs}
            emptyMessage="No attendance records recorded for this employee."
          />
        </Card>
      )}

      {/* Tab 3: Leaves Taken */}
      {activeTab === 'leave' && (
        <Card title="Leave Applications" subtitle="Approved, pending, and past requests">
          <Table
            columns={[
              { header: 'Leave Type', key: 'leaveType' },
              { header: 'From Date', key: 'startDate' },
              { header: 'To Date', key: 'endDate' },
              { header: 'Days', key: 'daysCount' },
              { header: 'Reason', key: 'reason' },
              {
                header: 'Status',
                key: 'status',
                render: (val) => <StatusBadge status={val} />
              }
            ]}
            data={leaveRequests}
            emptyMessage="No leave records found for this employee."
          />
        </Card>
      )}

      {/* Tab 4: Payroll History */}
      {activeTab === 'payroll' && (
        <Card title="Payroll Disbursements" subtitle="Monthly compensation records and payslip access">
          <Table
            columns={[
              { header: 'Period', key: 'period' },
              {
                header: 'Gross Pay',
                key: 'earnings',
                render: (val) => `$${Number(val?.grossPay || 95000 / 12).toLocaleString()}`
              },
              {
                header: 'Deductions',
                key: 'deductions',
                render: (val) => `$${Number(val?.totalDeductions || 1200).toLocaleString()}`
              },
              {
                header: 'Net Pay',
                key: 'netPay',
                render: (val) => (
                  <span className="font-mono font-bold text-[#387A58]">
                    ${Number(val || 79700).toLocaleString()}
                  </span>
                )
              },
              {
                header: 'Disbursement Status',
                key: 'status',
                render: (val) => <StatusBadge status={val || 'GENERATED'} />
              }
            ]}
            data={payslips}
            emptyMessage="No payroll disbursement records available."
          />
        </Card>
      )}

      {/* Edit Profile Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Edit Employee Profile"
        subtitle={`Modifying profile for ${employee.firstName} ${employee.lastName}`}
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleUpdateProfile}>
              Save Profile Changes
            </Button>
          </>
        }
      >
        <form onSubmit={handleUpdateProfile} className="space-y-4">
          <Input
            label="Designation"
            value={editForm.designation}
            onChange={(e) => setEditForm({ ...editForm, designation: e.target.value })}
          />
          <div>
            <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
              Department
            </label>
            <select
              value={editForm.department}
              onChange={(e) => setEditForm({ ...editForm, department: e.target.value })}
              className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none focus:border-[#702C37]"
            >
              <option value="ENGINEERING">Engineering</option>
              <option value="HUMAN_RESOURCES">Human Resources</option>
              <option value="DESIGN">Design</option>
              <option value="PRODUCT">Product</option>
              <option value="FINANCE">Finance</option>
              <option value="MARKETING">Marketing</option>
            </select>
          </div>
          <Input
            label="Phone"
            value={editForm.phone}
            onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
          />
          <div>
            <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
              Status
            </label>
            <select
              value={editForm.status}
              onChange={(e) => setEditForm({ ...editForm, status: e.target.value })}
              className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none focus:border-[#702C37]"
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="PROBATION">PROBATION</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="TERMINATED">TERMINATED</option>
            </select>
          </div>
          <Input
            label="Base Annual Salary ($)"
            type="number"
            value={editForm.baseSalary}
            onChange={(e) => setEditForm({ ...editForm, baseSalary: e.target.value })}
          />
        </form>
      </Modal>
    </div>
  );
}

export default EmployeeDetails;
