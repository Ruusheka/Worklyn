/**
 * HR-REWIND Recovery Engine API Service
 * Owner: Person 4
 * Endpoints: /api/v1/rewind/*
 */

import { get, post, isMockMode } from './client.js';
import { INITIAL_REWIND_WORKFLOWS } from './mockData.js';

const LOCAL_WORKFLOWS_KEY = 'worklyn_mock_rewind_workflows';

function getMockWorkflows() {
  try {
    const saved = localStorage.getItem(LOCAL_WORKFLOWS_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_REWIND_WORKFLOWS;
}

function saveMockWorkflows(list) {
  try {
    localStorage.setItem(LOCAL_WORKFLOWS_KEY, JSON.stringify(list));
  } catch (e) {
    // ignore
  }
}

export const rewindApi = {
  /**
   * List all tracked multi-step workflows
   * GET /api/v1/rewind/workflows
   */
  async getWorkflows() {
    if (isMockMode()) {
      return getMockWorkflows();
    }
    return await get('/rewind/workflows');
  },

  /**
   * Get detailed DAG step status
   * GET /api/v1/rewind/workflows/{id}
   */
  async getWorkflowById(id) {
    if (isMockMode()) {
      const list = getMockWorkflows();
      const found = list.find(w => w.workflowId === id);
      if (found) return found;
      throw new Error(`Workflow ${id} not found`);
    }
    return await get(`/rewind/workflows/${id}`);
  },

  /**
   * Generate non-mutating recovery plan preview
   * GET /api/v1/rewind/workflows/{id}/preview
   */
  async getPreview(id) {
    if (isMockMode()) {
      const list = getMockWorkflows();
      const wf = list.find(w => w.workflowId === id) || list[0];

      return {
        workflowId: wf.workflowId,
        workflowType: wf.workflowType,
        status: wf.status,
        failedStepId: wf.failedStepId || 'STEP_ADJUST_PAYROLL',
        failureReason: wf.failureReason || 'HTTP Connection timeout calling external payroll service',
        actions: [
          {
            stepId: "STEP_ADJUST_PAYROLL",
            action: "REPLAY",
            reason: "Step is marked idempotent and has retry attempts remaining (1/3)."
          },
          {
            stepId: "STEP_MARK_ATTENDANCE",
            action: "PRESERVE",
            reason: "Step completed successfully and has no failed upstream dependency."
          },
          {
            stepId: "STEP_DEDUCT_LEAVE_BALANCE",
            action: "PRESERVE",
            reason: "Step completed successfully."
          },
          {
            stepId: "STEP_VALIDATE_BALANCE",
            action: "PRESERVE",
            reason: "Pre-validation completed without side-effects."
          }
        ],
        invariantsSatisfied: true
      };
    }

    return await get(`/rewind/workflows/${id}/preview`);
  },

  /**
   * Execute dependency-aware recovery
   * POST /api/v1/rewind/workflows/{id}/recover
   */
  async recoverWorkflow(id, idempotencyKey = null) {
    if (isMockMode()) {
      const list = getMockWorkflows();
      const wf = list.find(w => w.workflowId === id);
      if (wf) {
        wf.status = 'COMPLETED';
        wf.failedStepId = null;
        wf.failureReason = null;
        wf.steps = wf.steps.map(s => ({ ...s, status: 'COMPLETED' }));
        saveMockWorkflows(list);
        return {
          workflowId: wf.workflowId,
          status: 'COMPLETED',
          message: 'Workflow successfully recovered and all steps resolved (Mock Simulation).',
          recoveredAt: new Date().toISOString(),
          invariantsSatisfied: true
        };
      }
      throw new Error(`Workflow ${id} not found`);
    }

    return await post(`/rewind/workflows/${id}/recover`, {}, idempotencyKey);
  }
};

export default rewindApi;
