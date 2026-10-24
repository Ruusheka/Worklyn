import React, { useState } from 'react';
import {
  Briefcase,
  Users,
  UserPlus,
  Star,
  Search,
  Filter,
  Eye,
  PlusCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Table from '../components/common/Table.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import Modal from '../components/common/Modal.jsx';
import Input from '../components/common/Input.jsx';
import { useToast } from '../components/common/Toast.jsx';
import { INITIAL_RECRUITMENT } from '../api/mockData.js';

export function Recruitment() {
  const { hasRole } = useAuth();
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState('jobs');
  const [jobSearch, setJobSearch] = useState('');
  const [candidateSearch, setCandidateSearch] = useState('');
  const [isAddJobOpen, setIsAddJobOpen] = useState(false);
  const [jobs, setJobs] = useState(INITIAL_RECRUITMENT.jobs);
  const [candidates, setCandidates] = useState(INITIAL_RECRUITMENT.candidates);

  const [newJob, setNewJob] = useState({
    title: '',
    department: 'ENGINEERING',
    location: '',
    type: 'Full-Time',
    openings: 1
  });

  const stats = {
    openJobs: jobs.filter(j => j.status === 'OPEN').length,
    totalApplications: jobs.reduce((sum, j) => sum + j.applicantsCount, 0),
    shortlisted: candidates.filter(c => c.status === 'SHORTLISTED').length,
    offerExtended: candidates.filter(c => c.status === 'OFFER_EXTENDED').length
  };

  const filteredJobs = jobs.filter(j =>
    j.title.toLowerCase().includes(jobSearch.toLowerCase()) ||
    j.department.toLowerCase().includes(jobSearch.toLowerCase())
  );

  const filteredCandidates = candidates.filter(c =>
    c.name.toLowerCase().includes(candidateSearch.toLowerCase()) ||
    c.role.toLowerCase().includes(candidateSearch.toLowerCase())
  );

  const handleAddJob = (e) => {
    e.preventDefault();
    if (!newJob.title || !newJob.location) {
      addToast('Please fill in required fields', 'error');
      return;
    }
    const job = {
      id: `job_${Date.now()}`,
      ...newJob,
      applicantsCount: 0,
      status: 'OPEN',
      postedDate: new Date().toISOString().split('T')[0]
    };
    setJobs([job, ...jobs]);
    setIsAddJobOpen(false);
    setNewJob({ title: '', department: 'ENGINEERING', location: '', type: 'Full-Time', openings: 1 });
    addToast(`Job posting "${job.title}" created successfully!`, 'success');
  };

  const jobColumns = [
    {
      header: 'Position',
      key: 'title',
      render: (v, row) => (
        <div>
          <p className="text-xs font-bold text-[#342522]">{v}</p>
          <p className="text-[10px] text-[#89766C]">{row.location}</p>
        </div>
      )
    },
    {
      header: 'Department',
      key: 'department',
      render: (v) => <span className="text-xs text-[#342522]">{v.replace('_', ' ')}</span>
    },
    {
      header: 'Type',
      key: 'type',
      render: (v) => <span className="text-xs text-[#89766C]">{v}</span>
    },
    {
      header: 'Openings',
      key: 'openings',
      align: 'center',
      render: (v) => <span className="font-bold text-xs text-[#342522]">{v}</span>
    },
    {
      header: 'Applications',
      key: 'applicantsCount',
      align: 'center',
      render: (v) => (
        <span className="font-mono font-bold text-xs text-[#541923] bg-[#F4EBE6] px-2 py-0.5 rounded-full">{v}</span>
      )
    },
    {
      header: 'Posted',
      key: 'postedDate',
      render: (v) => <span className="text-xs text-[#89766C]">{v}</span>
    },
    {
      header: 'Status',
      key: 'status',
      render: (v) => <StatusBadge status={v} />
    }
  ];

  const candidateColumns = [
    {
      header: 'Candidate',
      key: 'name',
      render: (v, row) => (
        <div>
          <p className="text-xs font-bold text-[#342522]">{v}</p>
          <p className="text-[10px] text-[#89766C]">{row.email}</p>
        </div>
      )
    },
    {
      header: 'Role Applied',
      key: 'role',
      render: (v) => <span className="text-xs text-[#342522]">{v}</span>
    },
    {
      header: 'Stage',
      key: 'stage',
      render: (v) => (
        <span className="text-[11px] font-medium text-[#89766C] bg-[#F3E6D6] px-2 py-0.5 rounded">
          {v.replace(/_/g, ' ')}
        </span>
      )
    },
    {
      header: 'Rating',
      key: 'rating',
      render: (v) => (
        <div className="flex items-center gap-1">
          <Star className="w-3.5 h-3.5 text-[#C9A574] fill-[#C9A574]" />
          <span className="text-xs font-bold text-[#342522]">{v}</span>
        </div>
      )
    },
    {
      header: 'Applied',
      key: 'appliedDate',
      render: (v) => <span className="text-xs text-[#89766C]">{v}</span>
    },
    {
      header: 'Status',
      key: 'status',
      render: (v) => <StatusBadge status={v} />
    }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">Recruitment Hub</h1>
          <p className="text-xs sm:text-sm text-[#89766C] mt-0.5">
            Track open requisitions, manage candidate pipelines, and coordinate offer decisions
          </p>
        </div>
        {(hasRole('ROLE_ADMIN') || hasRole('ROLE_HR')) && (
          <Button variant="primary" icon={PlusCircle} onClick={() => setIsAddJobOpen(true)}>
            Post New Job
          </Button>
        )}
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Open Requisitions</span>
          <div className="mt-2 text-3xl font-extrabold text-[#541923]">{stats.openJobs}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Actively recruiting</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Total Applications</span>
          <div className="mt-2 text-3xl font-extrabold text-[#342522]">{stats.totalApplications}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Pipeline candidates</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Shortlisted</span>
          <div className="mt-2 text-3xl font-extrabold text-[#387A58]">{stats.shortlisted}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Passing screening</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Offers Extended</span>
          <div className="mt-2 text-3xl font-extrabold text-[#D69A43]">{stats.offerExtended}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Awaiting acceptance</p>
        </Card>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#EAD8C3] space-x-6">
        <button
          onClick={() => setActiveTab('jobs')}
          className={`pb-3 text-sm font-semibold border-b-2 transition-all ${activeTab === 'jobs' ? 'border-[#541923] text-[#541923]' : 'border-transparent text-[#89766C] hover:text-[#342522]'}`}
        >
          Job Postings ({jobs.length})
        </button>
        <button
          onClick={() => setActiveTab('candidates')}
          className={`pb-3 text-sm font-semibold border-b-2 transition-all ${activeTab === 'candidates' ? 'border-[#541923] text-[#541923]' : 'border-transparent text-[#89766C] hover:text-[#342522]'}`}
        >
          Candidate Pipeline ({candidates.length})
        </button>
      </div>

      {/* Jobs Table */}
      {activeTab === 'jobs' && (
        <Card
          title="Open Job Requisitions"
          subtitle="Current active hiring positions across all departments"
          action={
            <div className="relative">
              <Search className="w-4 h-4 text-[#89766C] absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={jobSearch}
                onChange={(e) => setJobSearch(e.target.value)}
                placeholder="Search positions..."
                className="bg-white border border-[#EAD8C3] rounded-xl pl-9 pr-4 py-2 text-xs text-[#342522] focus:outline-none focus:border-[#702C37]"
              />
            </div>
          }
        >
          <Table columns={jobColumns} data={filteredJobs} emptyMessage="No job postings match your search." />
        </Card>
      )}

      {/* Candidates Table */}
      {activeTab === 'candidates' && (
        <Card
          title="Candidate Pipeline"
          subtitle="All applicants and their interview stage progression"
          action={
            <div className="relative">
              <Search className="w-4 h-4 text-[#89766C] absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={candidateSearch}
                onChange={(e) => setCandidateSearch(e.target.value)}
                placeholder="Search candidates..."
                className="bg-white border border-[#EAD8C3] rounded-xl pl-9 pr-4 py-2 text-xs text-[#342522] focus:outline-none focus:border-[#702C37]"
              />
            </div>
          }
        >
          <Table columns={candidateColumns} data={filteredCandidates} emptyMessage="No candidates found." />
        </Card>
      )}

      {/* Add Job Modal */}
      <Modal
        isOpen={isAddJobOpen}
        onClose={() => setIsAddJobOpen(false)}
        title="Create Job Requisition"
        subtitle="Post a new hiring position to the recruitment pipeline"
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsAddJobOpen(false)}>Cancel</Button>
            <Button variant="primary" onClick={handleAddJob}>Create Posting</Button>
          </>
        }
      >
        <form onSubmit={handleAddJob} className="space-y-4">
          <Input
            label="Job Title"
            value={newJob.title}
            onChange={(e) => setNewJob({ ...newJob, title: e.target.value })}
            placeholder="e.g. Senior Backend Engineer"
          />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-[#342522] uppercase tracking-wider mb-1.5">Department</label>
              <select
                value={newJob.department}
                onChange={(e) => setNewJob({ ...newJob, department: e.target.value })}
                className="w-full rounded-lg bg-white border border-[#EAD8C3] px-3.5 py-2.5 text-sm text-[#342522] focus:outline-none"
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
              label="Openings"
              type="number"
              value={newJob.openings}
              onChange={(e) => setNewJob({ ...newJob, openings: parseInt(e.target.value) || 1 })}
              min="1"
            />
          </div>
          <Input
            label="Location"
            value={newJob.location}
            onChange={(e) => setNewJob({ ...newJob, location: e.target.value })}
            placeholder="e.g. San Francisco, CA (Hybrid)"
          />
        </form>
      </Modal>
    </div>
  );
}

export default Recruitment;
