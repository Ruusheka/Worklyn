import React, { useState, useEffect } from 'react';
import {
  RotateCcw,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Play,
  Eye,
  FileText,
  Shield
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { rewindApi } from '../api/rewindApi.js';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Table from '../components/common/Table.jsx';
import StatusBadge from '../components/common/StatusBadge.jsx';
import { DependencyGraphView } from '../components/rewind/DependencyGraphView.jsx';
import { PlanPreviewTable } from '../components/rewind/PlanPreviewTable.jsx';
import { RecoveryExecutionModal } from '../components/rewind/RecoveryExecutionModal.jsx';
import { useToast } from '../components/common/Toast.jsx';

export function RewindDashboard() {
  const { hasRole } = useAuth();
  const { addToast } = useToast();

  const [workflows, setWorkflows] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [plan, setPlan] = useState(null);
  const [selectedStep, setSelectedStep] = useState(null);
  const [isPlanLoading, setIsPlanLoading] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isExecuting, setIsExecuting] = useState(false);
  const [isRecoveryModalOpen, setIsRecoveryModalOpen] = useState(false);
  const [activeView, setActiveView] = useState('list'); // 'list' | 'detail'

  const loadWorkflows = async () => {
    setIsLoading(true);
    try {
      const data = await rewindApi.getWorkflows();
      setWorkflows(data);
    } catch (err) {
      addToast(err.message || 'Failed to load workflow registry', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadWorkflows();
  }, []);

  const handleSelectWorkflow = async (wf) => {
    setSelectedWorkflow(wf);
    setSelectedStep(null);
    setPlan(null);
    setActiveView('detail');

    if (wf.status === 'FAILED' || wf.status === 'REQUIRES_MANUAL_REVIEW') {
      setIsPlanLoading(true);
      try {
        const preview = await rewindApi.getPreview(wf.workflowId);
        setPlan(preview);
      } catch (err) {
        addToast('Could not fetch recovery preview from engine', 'error');
      } finally {
        setIsPlanLoading(false);
      }
    }
  };

  const handleExecuteRecovery = async (workflowId) => {
    setIsExecuting(true);
    try {
      const key = `rewind_${workflowId}_${Date.now()}`;
      const result = await rewindApi.recoverWorkflow(workflowId, key);
      addToast('Recovery executed! Workflow transitioned to COMPLETED.', 'success');
      setIsRecoveryModalOpen(false);

      // Refresh workflows list and re-select
      await loadWorkflows();
      const updated = workflows.find(w => w.workflowId === workflowId);
      if (updated) {
        setSelectedWorkflow({ ...updated, status: 'COMPLETED' });
      }
      setPlan(null);
    } catch (err) {
      addToast(err.message || 'Recovery execution failed', 'error');
    } finally {
      setIsExecuting(false);
    }
  };

  const workflowColumns = [
    {
      header: 'Workflow ID',
      key: 'workflowId',
      render: (v) => <span className="font-mono text-xs font-bold text-[#541923]">#{v}</span>
    },
    {
      header: 'Type',
      key: 'workflowType',
      render: (v) => <span className="text-xs font-semibold text-[#342522]">{v.replace(/_/g, ' ')}</span>
    },
    {
      header: 'Target',
      key: 'targetEntity',
      render: (v) => <span className="font-mono text-xs text-[#89766C]">{v}</span>
    },
    {
      header: 'Initiated By',
      key: 'initiatedBy',
      render: (v) => <span className="text-xs text-[#89766C]">{v}</span>
    },
    {
      header: 'Initiated At',
      key: 'initiatedAt',
      render: (v) => <span className="text-xs text-[#89766C]">{new Date(v).toLocaleString()}</span>
    },
    {
      header: 'Status',
      key: 'status',
      render: (v) => <StatusBadge status={v} />
    },
    {
      header: 'Actions',
      key: 'actions',
      align: 'right',
      render: (_, row) => (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => handleSelectWorkflow(row)}
          className="p-1.5"
          title="Inspect DAG & Recovery Plan"
        >
          <Eye className="w-4 h-4 text-[#541923]" />
        </Button>
      )
    }
  ];

  const failedCount = workflows.filter(w => w.status === 'FAILED').length;
  const reviewCount = workflows.filter(w => w.status === 'REQUIRES_MANUAL_REVIEW').length;
  const completedCount = workflows.filter(w => w.status === 'COMPLETED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-[#F4EBE6]">
              <RotateCcw className="w-5 h-5 text-[#541923]" />
            </div>
            <h1 className="text-2xl font-extrabold text-[#342522] tracking-tight">HR-REWIND Engine</h1>
          </div>
          <p className="text-xs sm:text-sm text-[#89766C] mt-1">
            Dependency-aware DAG recovery planner with compensating transaction rollbacks and invariant enforcement
          </p>
        </div>

        <div className="flex items-center gap-3">
          {activeView === 'detail' && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => { setActiveView('list'); setSelectedWorkflow(null); setPlan(null); }}
            >
              ← Back to Registry
            </Button>
          )}
          <Button
            variant="secondary"
            size="sm"
            icon={RefreshCw}
            onClick={loadWorkflows}
          >
            Refresh
          </Button>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Failed DAG Workflows</span>
          <div className="mt-2 text-3xl font-extrabold text-[#B85C55]">{failedCount}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Replay or compensate eligible</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Manual Review Required</span>
          <div className="mt-2 text-3xl font-extrabold text-[#D69A43]">{reviewCount}</div>
          <p className="text-[11px] text-[#89766C] mt-1">Invariant violations detected</p>
        </Card>
        <Card>
          <span className="text-xs font-bold text-[#89766C] uppercase">Successfully Completed</span>
          <div className="mt-2 text-3xl font-extrabold text-[#387A58]">{completedCount}</div>
          <p className="text-[11px] text-[#89766C] mt-1">All invariants satisfied</p>
        </Card>
      </div>

      {/* List View */}
      {activeView === 'list' && (
        <Card
          title="Multi-Step Workflow Registry"
          subtitle="All tracked HR workflows with DAG execution state. Click inspect icon to view recovery plan."
        >
          <Table
            columns={workflowColumns}
            data={workflows}
            isLoading={isLoading}
            emptyMessage="No workflow instances registered in the recovery engine."
            onRowClick={handleSelectWorkflow}
          />
        </Card>
      )}

      {/* Detail View */}
      {activeView === 'detail' && selectedWorkflow && (
        <div className="space-y-5">
          {/* Workflow Header Card */}
          <Card bodyClassName="p-5">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="text-[10px] font-bold text-[#89766C] uppercase tracking-wider">Workflow Instance</p>
                <h2 className="text-xl font-extrabold text-[#342522] font-mono">
                  #{selectedWorkflow.workflowId}
                </h2>
                <p className="text-sm text-[#702C37] font-semibold mt-0.5">
                  {selectedWorkflow.workflowType.replace(/_/g, ' ')}
                </p>
                <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-[#89766C]">
                  <span>Target: <strong className="text-[#342522] font-mono">{selectedWorkflow.targetEntity}</strong></span>
                  <span>By: {selectedWorkflow.initiatedBy}</span>
                  <span>Started: {new Date(selectedWorkflow.initiatedAt).toLocaleString()}</span>
                </div>
              </div>
              <div className="flex flex-col items-end gap-3">
                <StatusBadge status={selectedWorkflow.status} />
                {(selectedWorkflow.status === 'FAILED' || selectedWorkflow.status === 'REQUIRES_MANUAL_REVIEW') && (
                  <Button
                    variant="primary"
                    icon={Play}
                    onClick={() => setIsRecoveryModalOpen(true)}
                    disabled={!plan}
                    className="bg-[#541923] text-xs"
                  >
                    Execute Recovery
                  </Button>
                )}
              </div>
            </div>

            {selectedWorkflow.failureReason && (
              <div className="mt-4 p-3.5 bg-[#FBECEB] rounded-xl border border-[#B85C55]/30 flex items-start gap-3">
                <AlertTriangle className="w-4 h-4 text-[#B85C55] shrink-0 mt-0.5" />
                <div className="text-xs">
                  <p className="font-bold text-[#B85C55]">Root Failure Cause</p>
                  <p className="text-[#342522] mt-0.5">{selectedWorkflow.failureReason}</p>
                  <p className="text-[#89766C] mt-1">
                    Failed Step: <code className="font-mono font-bold text-[#541923] bg-white/80 px-1 rounded">{selectedWorkflow.failedStepId}</code>
                  </p>
                </div>
              </div>
            )}
          </Card>

          {/* DAG Visualization */}
          <Card title="Execution DAG Graph" subtitle="Interactive topological step view with dependency resolution order">
            <DependencyGraphView
              steps={selectedWorkflow.steps || []}
              failedStepId={selectedWorkflow.failedStepId}
              selectedStepId={selectedStep?.id}
              onSelectStep={setSelectedStep}
            />

            {selectedStep && (
              <div className="mt-4 p-4 bg-white rounded-xl border border-[#EAD8C3] text-xs">
                <p className="font-bold text-[#342522] mb-2">Selected Step Details</p>
                <div className="grid grid-cols-2 gap-x-6 gap-y-1.5 text-[#89766C]">
                  <span>Step ID:</span><span className="font-mono font-bold text-[#541923]">{selectedStep.id}</span>
                  <span>Name:</span><span className="font-semibold text-[#342522]">{selectedStep.name}</span>
                  <span>Status:</span><StatusBadge status={selectedStep.status} />
                  <span>Duration:</span><span className="font-mono">{selectedStep.durationMs}ms</span>
                  {selectedStep.retryCount !== undefined && (
                    <>
                      <span>Retry Attempts:</span>
                      <span className="font-mono">{selectedStep.retryCount}/{selectedStep.maxRetries}</span>
                    </>
                  )}
                  <span>Depends On:</span>
                  <span className="font-mono">{selectedStep.dependsOn?.join(', ') || 'None (Root step)'}</span>
                </div>
              </div>
            )}
          </Card>

          {/* Recovery Plan Preview */}
          {(selectedWorkflow.status === 'FAILED' || selectedWorkflow.status === 'REQUIRES_MANUAL_REVIEW') && (
            <Card title="Recovery Plan Preview" subtitle="Non-mutating preview — no state changes occur until you Execute Recovery">
              <PlanPreviewTable plan={plan} isLoading={isPlanLoading} />
            </Card>
          )}

          {selectedWorkflow.status === 'COMPLETED' && (
            <div className="p-6 text-center text-[#387A58] bg-[#EBF4EF] rounded-xl border border-[#387A58]/20">
              <CheckCircle2 className="w-8 h-8 mx-auto mb-2 text-[#387A58]" />
              <p className="font-bold text-sm">Workflow Completed Successfully</p>
              <p className="text-xs text-[#89766C] mt-1">
                All steps resolved. Invariants verified. No recovery action required.
              </p>
            </div>
          )}
        </div>
      )}

      {/* Execution Modal */}
      <RecoveryExecutionModal
        isOpen={isRecoveryModalOpen}
        onClose={() => setIsRecoveryModalOpen(false)}
        workflow={selectedWorkflow}
        plan={plan}
        onExecuteRecovery={handleExecuteRecovery}
        isExecuting={isExecuting}
      />
    </div>
  );
}

export default RewindDashboard;
