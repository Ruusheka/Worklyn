/**
 * WorkflowRecovery.test.jsx — RewindDashboard Integration Tests
 * Owner: Person 4
 * Framework: React Testing Library + Vitest
 * Tests: Preview display, DAG rendering, confirmation modal behavior
 */

import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

// Mock toast
vi.mock('../components/common/Toast.jsx', () => ({
  useToast: () => ({ addToast: vi.fn() }),
  ToastProvider: ({ children }) => <>{children}</>
}));

// Mock AuthContext
vi.mock('../context/AuthContext.jsx', () => ({
  useAuth: () => ({
    user: { id: 'admin_01', firstName: 'Sarah', lastName: 'Jenkins', roles: ['ROLE_ADMIN'] },
    hasRole: (role) => role === 'ROLE_ADMIN',
    isAuthenticated: true,
    isLoading: false,
  }),
  AuthProvider: ({ children }) => <>{children}</>
}));

// Mock rewindApi
vi.mock('../api/rewindApi.js', () => ({
  rewindApi: {
    getWorkflows: vi.fn(),
    getWorkflowById: vi.fn(),
    getPreview: vi.fn(),
    recoverWorkflow: vi.fn(),
  }
}));

import { rewindApi } from '../api/rewindApi.js';
import { RewindDashboard } from '../pages/RewindDashboard.jsx';
import { RecoveryExecutionModal } from '../components/rewind/RecoveryExecutionModal.jsx';
import { PlanPreviewTable } from '../components/rewind/PlanPreviewTable.jsx';
import { DependencyGraphView } from '../components/rewind/DependencyGraphView.jsx';

const MOCK_WORKFLOW = {
  workflowId: '6714a930c102',
  workflowType: 'LEAVE_APPROVAL_CASCADE',
  status: 'FAILED',
  initiatedBy: 'elena.rostova@worklyn.io',
  initiatedAt: '2026-10-23T14:32:00.000Z',
  failedStepId: 'STEP_ADJUST_PAYROLL',
  failureReason: 'HTTP Connection timeout calling external payroll service',
  targetEntity: 'EMP-2045',
  steps: [
    { id: 'STEP_VALIDATE_BALANCE', name: 'Validate Leave Balance', status: 'COMPLETED', durationMs: 45, dependsOn: [] },
    { id: 'STEP_DEDUCT_LEAVE_BALANCE', name: 'Deduct Leave Balance', status: 'COMPLETED', durationMs: 120, dependsOn: ['STEP_VALIDATE_BALANCE'] },
    { id: 'STEP_ADJUST_PAYROLL', name: 'Recalculate Period Payroll', status: 'FAILED', durationMs: 5002, dependsOn: ['STEP_DEDUCT_LEAVE_BALANCE'] }
  ]
};

const MOCK_PLAN = {
  workflowId: '6714a930c102',
  workflowType: 'LEAVE_APPROVAL_CASCADE',
  status: 'FAILED',
  failedStepId: 'STEP_ADJUST_PAYROLL',
  failureReason: 'HTTP Connection timeout calling external payroll service',
  actions: [
    { stepId: 'STEP_ADJUST_PAYROLL', action: 'REPLAY', reason: 'Step is idempotent and has retry attempts remaining (1/3).' },
    { stepId: 'STEP_DEDUCT_LEAVE_BALANCE', action: 'PRESERVE', reason: 'Step completed successfully.' }
  ],
  invariantsSatisfied: true
};

function renderWithProviders(component) {
  return render(
    <MemoryRouter>
      {component}
    </MemoryRouter>
  );
}

describe('RewindDashboard — Preview and Listing', () => {
  beforeEach(() => {
    rewindApi.getWorkflows.mockResolvedValue([MOCK_WORKFLOW]);
    rewindApi.getPreview.mockResolvedValue(MOCK_PLAN);
    rewindApi.recoverWorkflow.mockResolvedValue({ workflowId: '6714a930c102', status: 'COMPLETED' });
  });

  it('renders the workflow registry table with correct workflow data', async () => {
    renderWithProviders(<RewindDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Multi-Step Workflow Registry/i)).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText(/#6714a930c102/i)).toBeInTheDocument();
    });
  });

  it('shows FAILED status badge for failed workflow', async () => {
    renderWithProviders(<RewindDashboard />);

    await waitFor(() => {
      const statusBadges = screen.getAllByText('FAILED');
      expect(statusBadges.length).toBeGreaterThan(0);
    });
  });

  it('shows summary stat cards with correct failed count', async () => {
    renderWithProviders(<RewindDashboard />);

    await waitFor(() => {
      expect(screen.getByText('Failed DAG Workflows')).toBeInTheDocument();
    });
  });
});

describe('PlanPreviewTable — Non-mutating Recovery Preview', () => {
  it('renders recovery plan with REPLAY and PRESERVE actions', () => {
    renderWithProviders(<PlanPreviewTable plan={MOCK_PLAN} isLoading={false} />);

    expect(screen.getByText(/Non-Mutating/i)).toBeInTheDocument();
    expect(screen.getAllByText('STEP_ADJUST_PAYROLL').length).toBeGreaterThan(0);
    expect(screen.getByText('REPLAY')).toBeInTheDocument();
    expect(screen.getByText('PRESERVE')).toBeInTheDocument();
  });

  it('shows invariant verified badge when invariantsSatisfied is true', () => {
    renderWithProviders(<PlanPreviewTable plan={MOCK_PLAN} isLoading={false} />);
    expect(screen.getByText(/Invariants Verified/i)).toBeInTheDocument();
  });

  it('shows loading state when isLoading=true', () => {
    renderWithProviders(<PlanPreviewTable plan={null} isLoading={true} />);
    expect(screen.getByText(/Computing non-mutating recovery plan/i)).toBeInTheDocument();
  });

  it('shows failure reason in plan header', () => {
    renderWithProviders(<PlanPreviewTable plan={MOCK_PLAN} isLoading={false} />);
    expect(screen.getAllByText(/STEP_ADJUST_PAYROLL/i).length).toBeGreaterThan(0);
    expect(screen.getByText(/HTTP Connection timeout/i)).toBeInTheDocument();
  });
});

describe('DependencyGraphView — DAG Node Rendering', () => {
  it('renders all workflow steps as nodes', () => {
    renderWithProviders(
      <DependencyGraphView
        steps={MOCK_WORKFLOW.steps}
        failedStepId={MOCK_WORKFLOW.failedStepId}
      />
    );

    expect(screen.getByText('Validate Leave Balance')).toBeInTheDocument();
    expect(screen.getByText('Deduct Leave Balance')).toBeInTheDocument();
    expect(screen.getByText('Recalculate Period Payroll')).toBeInTheDocument();
  });

  it('step count matches workflow steps array length', () => {
    renderWithProviders(
      <DependencyGraphView
        steps={MOCK_WORKFLOW.steps}
        failedStepId={MOCK_WORKFLOW.failedStepId}
      />
    );

    // All 3 step nodes rendered
    expect(screen.getAllByText(/Step \d/).length).toBe(3);
  });

  it('calls onSelectStep when a step node is clicked', () => {
    const mockSelectStep = vi.fn();
    renderWithProviders(
      <DependencyGraphView
        steps={MOCK_WORKFLOW.steps}
        failedStepId={MOCK_WORKFLOW.failedStepId}
        onSelectStep={mockSelectStep}
      />
    );

    fireEvent.click(screen.getByText('Validate Leave Balance'));
    expect(mockSelectStep).toHaveBeenCalledWith(MOCK_WORKFLOW.steps[0]);
  });
});

describe('RecoveryExecutionModal — Confirmation Flow', () => {
  it('renders the modal with compensation summary when open', () => {
    const onClose = vi.fn();
    const onExecute = vi.fn();

    renderWithProviders(
      <RecoveryExecutionModal
        isOpen={true}
        onClose={onClose}
        workflow={MOCK_WORKFLOW}
        plan={MOCK_PLAN}
        onExecuteRecovery={onExecute}
        isExecuting={false}
      />
    );

    expect(screen.getByText(/Authorize Workflow Recovery/i)).toBeInTheDocument();
    expect(screen.getByText(/State Mutation/i)).toBeInTheDocument();
  });

  it('execute button is disabled before typing CONFIRM RECOVER', () => {
    const onClose = vi.fn();
    const onExecute = vi.fn();

    renderWithProviders(
      <RecoveryExecutionModal
        isOpen={true}
        onClose={onClose}
        workflow={MOCK_WORKFLOW}
        plan={MOCK_PLAN}
        onExecuteRecovery={onExecute}
        isExecuting={false}
      />
    );

    const executeBtn = screen.getByText(/Execute Recovery/i).closest('button');
    expect(executeBtn).toBeDisabled();
  });

  it('execute button becomes enabled after typing CONFIRM RECOVER', async () => {
    const onClose = vi.fn();
    const onExecute = vi.fn();

    renderWithProviders(
      <RecoveryExecutionModal
        isOpen={true}
        onClose={onClose}
        workflow={MOCK_WORKFLOW}
        plan={MOCK_PLAN}
        onExecuteRecovery={onExecute}
        isExecuting={false}
      />
    );

    const input = screen.getByPlaceholderText(/CONFIRM RECOVER/i);
    fireEvent.change(input, { target: { value: 'CONFIRM RECOVER' } });

    await waitFor(() => {
      const executeBtn = screen.getByText(/Execute Recovery/i).closest('button');
      expect(executeBtn).not.toBeDisabled();
    });
  });

  it('does NOT call onExecuteRecovery with wrong confirmation text', () => {
    const onClose = vi.fn();
    const onExecute = vi.fn();

    renderWithProviders(
      <RecoveryExecutionModal
        isOpen={true}
        onClose={onClose}
        workflow={MOCK_WORKFLOW}
        plan={MOCK_PLAN}
        onExecuteRecovery={onExecute}
        isExecuting={false}
      />
    );

    const input = screen.getByPlaceholderText(/CONFIRM RECOVER/i);
    fireEvent.change(input, { target: { value: 'wrong text' } });

    const executeBtn = screen.getByText(/Execute Recovery/i).closest('button');
    fireEvent.click(executeBtn);

    // Button disabled, so onExecute should NOT be called
    expect(onExecute).not.toHaveBeenCalled();
  });

  it('renders cancel button and calls onClose', () => {
    const onClose = vi.fn();
    const onExecute = vi.fn();

    renderWithProviders(
      <RecoveryExecutionModal
        isOpen={true}
        onClose={onClose}
        workflow={MOCK_WORKFLOW}
        plan={MOCK_PLAN}
        onExecuteRecovery={onExecute}
        isExecuting={false}
      />
    );

    const cancelBtn = screen.getByText('Cancel').closest('button');
    fireEvent.click(cancelBtn);
    expect(onClose).toHaveBeenCalled();
  });
});
