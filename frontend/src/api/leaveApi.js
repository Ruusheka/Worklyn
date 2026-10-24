/**
 * Leave Management API Service
 * Owner: Person 4
 * Endpoints: /api/v1/leave/*
 * Follows README specification Section 14.4
 */

import { get, post } from './client.js';

export const leaveApi = {
  /**
   * Apply for leave
   * POST /api/v1/leave/apply
   */
  async applyLeave(data) {
    return await post('/leave/apply', data);
  },

  /**
   * Get employee leave balance summary
   * GET /api/v1/leave/balances
   */
  async getBalances(employeeId = null) {
    const params = employeeId ? { employeeId } : {};
    return await get('/leave/balances', params);
  },

  /**
   * Alias for getBalances
   */
  async getBalance(employeeId = null) {
    return await this.getBalances(employeeId);
  },

  /**
   * Get employee's leave request history
   * GET /api/v1/leave/my-requests
   */
  async getMyRequests() {
    return await get('/leave/my-requests');
  },

  /**
   * Get pending leave requests requiring approval (HR / Manager)
   * GET /api/v1/leave/pending-approvals
   */
  async getPendingApprovals() {
    return await get('/leave/pending-approvals');
  },

  /**
   * Approve a leave request (Manager / HR)
   * POST /api/v1/leave/{id}/approve
   */
  async approveLeave(id, remarks = '') {
    return await post(`/leave/${id}/approve`, { remarks });
  },

  /**
   * Reject a leave request (Manager / HR)
   * POST /api/v1/leave/{id}/reject
   */
  async rejectLeave(id, remarks = '') {
    return await post(`/leave/${id}/reject`, { remarks });
  },

  /**
   * Cancel a pending leave request (Employee)
   * POST /api/v1/leave/{id}/cancel
   */
  async cancelLeave(id) {
    return await post(`/leave/${id}/cancel`, {});
  }
};

export default leaveApi;
