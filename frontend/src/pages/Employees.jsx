import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  UserPlus,
  Search,
  Filter,
  Eye,
  Edit,
  Building,
  Mail,
  Phone,
  Calendar,
  DollarSign
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { employeeApi } from '../api/employeeApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Table from '../components/common/Table.jsx';
import Modal from '../components/common/Modal.jsx';
import Input from '../components/common/Input.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function Employees() {
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const { addToast } = useToast();

  const [employees, setEmployees] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [status, setStatus] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modal State
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    employeeCode: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    department: 'ENGINEERING',
    designation: '',
    baseSalary: '',
    dateOfJoining: new Date().toISOString().split('T')[0],
  });
  const [formErrors, setFormErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadEmployees = async () => {
    setIsLoading(true);
    try {
      const res = await employeeApi.getEmployees({
        page,
        size: 8,
        search,
        department,
        status,
      });
      setEmployees(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch (err) {
      console.error('Error fetching employees', err);
      addToast(err.message || 'Failed to fetch employees', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadEmployees();
  }, [page, department, status, search]);

  const validateForm = () => {
    const errs = {};
    if (!formData.firstName.trim()) errs.firstName = 'First name is required';
    if (!formData.lastName.trim()) errs.lastName = 'Last name is required';
    if (!formData.email.trim()) {
      errs.email = 'Email is required';
    } else if (!formData.email.includes('@')) {
      errs.email = 'Must be a valid email';
    }
    if (!formData.designation.trim()) errs.designation = 'Designation is required';
    if (!formData.baseSalary || Number(formData.baseSalary) <= 0) {
      errs.baseSalary = 'Base salary must be greater than zero';
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleAddEmployee = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      const generatedCode = formData.employeeCode.trim() || `EMP-${Math.floor(1000 + Math.random() * 9000)}`;
      await employeeApi.createEmployee({
        ...formData,
        employeeCode: generatedCode,
        baseSalary: parseFloat(formData.baseSalary),
      });
      addToast(`Employee ${formData.firstName} ${formData.lastName} created!`, 'success');
      setIsAddModalOpen(false);
      setFormData({
        employeeCode: '',
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        department: 'ENGINEERING',
        designation: '',
        baseSalary: '',
        dateOfJoining: new Date().toISOString().split('T')[0],
      });
      loadEmployees();
    } catch (err) {
      addToast(err.message || 'Failed to create employee', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const canManage = hasRole('ROLE_ADMIN') || hasRole('ROLE_HR');

  const columns = [
    {
      header: 'Employee',
      key: 'name',
      render: (_, row) => (
        <div className="flex items-center gap-3">
          <img
            src={
              row.avatar ||
              'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'
            }
            alt={row.firstName}
            className="w-8 h-8 rounded-full object-cover border border-[#EAD8C3]"
          />
          <div>
            <p className="text-xs font-bold text-[#342522]">
              {row.firstName} {row.lastName}
            </p>
            <p className="text-[11px] text-[#89766C]">{row.email}</p>
          </div>
        </div>
      ),
    },
    {
      header: 'Code',
      key: 'employeeCode',
      render: (val) => (
        <span className="font-mono text-xs font-semibold text-[#541923] bg-[#F4EBE6] px-2 py-0.5 rounded">
          {val}
        </span>
      ),
    },
    {
      header: 'Department',
      key: 'department',
      render: (val) => (
        <span className="text-xs text-[#342522] font-medium">
          {val ? val.replace('_', ' ') : '—'}
        </span>
      ),
    },
    {
      header: 'Designation',
      key: 'designation',
      render: (val) => <span className="text-xs text-[#89766C]">{val || '—'}</span>,
    },
    {
      header: 'Status',
      key: 'status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (_, row) => (
        <div className="flex items-center justify-end gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/employees/${row.id || row.employeeCode}`)}
            className="p-1.5"
            title="View Details"
          >
            <Eye className="w-4 h-4 text-[#541923]" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Title & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Employee Directory</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            Manage employee master data, department affiliations, and role privileges
          </p>
        </div>
        {canManage && (
          <Button
            variant="primary"
            onClick={() => setIsAddModalOpen(true)}
            icon={UserPlus}
            className="shadow-sm"
          >
            Add New Employee
          </Button>
        )}
      </div>

      {/* Filter and Search Bar */}
      <Card bodyClassName="p-4">
        <div className="flex flex-col md:flex-row items-center justify-between gap-3">
          <div className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-[#89766C] absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              placeholder="Search by name, code, email..."
              className="w-full bg-white border border-[#EAD8C3] rounded-xl pl-9.5 pr-4 py-2 text-xs text-[#342522] focus:outline-none focus:border-[#702C37]"
            />
          </div>

          <div className="flex items-center gap-3 w-full md:w-auto">
            {/* Department Filter */}
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-[#89766C] whitespace-nowrap hidden sm:inline">Dept:</span>
              <select
                value={department}
                onChange={(e) => {
                  setDepartment(e.target.value);
                  setPage(0);
                }}
                className="bg-white border border-[#EAD8C3] rounded-xl px-3 py-2 text-xs text-[#342522] focus:outline-none focus:border-[#702C37]"
              >
                <option value="ALL">All Departments</option>
                <option value="ENGINEERING">Engineering</option>
                <option value="HUMAN_RESOURCES">Human Resources</option>
                <option value="DESIGN">Design</option>
                <option value="PRODUCT">Product</option>
                <option value="FINANCE">Finance</option>
                <option value="MARKETING">Marketing</option>
              </select>
            </div>

            {/* Status Filter */}
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-[#89766C] whitespace-nowrap hidden sm:inline">Status:</span>
              <select
                value={status}
                onChange={(e) => {
                  setStatus(e.target.value);
                  setPage(0);
                }}
                className="bg-white border border-[#EAD8C3] rounded-xl px-3 py-2 text-xs text-[#342522] focus:outline-none focus:border-[#702C37]"
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="PROBATION">Probation</option>
                <option value="INACTIVE">Inactive</option>
                <option value="TERMINATED">Terminated</option>
              </select>
            </div>
          </div>
        </div>
      </Card>

      {/* Employees Table */}
      <div className="space-y-4">
        <Table
          columns={columns}
          data={employees}
          isLoading={isLoading}
          emptyMessage="No employees found matching the specified filters."
          onRowClick={(row) => navigate(`/employees/${row.id || row.employeeCode}`)}
        />

        {/* Pagination Footer */}
        <div className="flex items-center justify-between px-2 text-xs text-[#89766C]">
          <span>
            Showing <strong className="text-[#342522]">{employees.length}</strong> of{' '}
            <strong className="text-[#342522]">{totalElements}</strong> employees
          </span>
          <div className="flex items-center gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
            >
              Previous
            </Button>
            <span className="font-semibold text-[#342522]">
              Page {page + 1} of {totalPages}
            </span>
            <Button
              variant="secondary"
              size="sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            >
              Next
            </Button>
          </div>
        </div>
      </div>

      {/* Add Employee Modal */}
      <Modal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title="Provision New Employee"
        subtitle="Registers employee profile and initializes leave allocations"
        maxWidth="max-w-xl"
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsAddModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleAddEmployee} isLoading={isSubmitting}>
              Create Employee Profile
            </Button>
          </>
        }
      >
        <form onSubmit={handleAddEmployee} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="First Name"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              error={formErrors.firstName}
              placeholder="e.g. David"
            />
            <Input
              label="Last Name"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              error={formErrors.lastName}
              placeholder="e.g. Chen"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Email Address"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              error={formErrors.email}
              placeholder="david.chen@worklyn.io"
            />
            <Input
              label="Phone Number"
              type="text"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              placeholder="+1 (555) 000-0000"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">
                Department
              </label>
              <select
                value={formData.department}
                onChange={(e) => setFormData({ ...formData, department: e.target.value })}
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
              label="Designation"
              value={formData.designation}
              onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
              error={formErrors.designation}
              placeholder="e.g. Backend Engineer"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Base Annual Salary ($)"
              type="number"
              value={formData.baseSalary}
              onChange={(e) => setFormData({ ...formData, baseSalary: e.target.value })}
              error={formErrors.baseSalary}
              placeholder="e.g. 95000"
            />
            <Input
              label="Employee Code (Optional)"
              value={formData.employeeCode}
              onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
              placeholder="Auto-generated if blank (e.g. EMP-2045)"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
}

export default Employees;
