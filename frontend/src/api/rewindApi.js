/**
 * HR-REWIND Recovery Engine API Service
 * Owner: Person 4
 * Endpoints: /api/v1/rewind/*
 * Follows README specification Section 12 & 14.8
 */

import { get, post } from './client.js';

export const rewindApi = {
  /**
   * List all tracked multi-step workflows
   * GET /api/v1/rewind/workflows
   */
  async getWorkflows() {
    return await get('/rewind/workflows');
  },

  /**
   * Get detailed DAG step status
   * GET /api/v1/rewind/workflows/{id}
   */
  async getWorkflowById(id) {
    return await get(`/rewind/workflows/${id}`);
  },

  /**
   * Generate non-mutating recovery plan preview
   * GET /api/v1/rewind/workflows/{id}/preview
   */
  async getPreview(id) {
    return await get(`/rewind/workflows/${id}/preview`);
  },

  /**
   * Execute dependency-aware recovery under distributed lock and idempotency
   * POST /api/v1/rewind/workflows/{id}/recover
   */
  async recoverWorkflow(id, idempotencyKey = null) {
    const key = idempotencyKey || `rewind_${id}_${Date.now()}`;
    return await post(`/rewind/workflows/${id}/recover`, {}, key);
  }
};

export default rewindApi;
