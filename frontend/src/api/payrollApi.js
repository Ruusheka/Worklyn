/**
 * Payroll API Service
 * Owner: Person 4
 * Endpoints: /api/v1/payroll/*
 * Follows README specification Section 14.5
 */

import { get, post } from './client.js';

export const payrollApi = {
  /**
   * Execute batch payroll calculation (Admin, HR)
   * POST /api/v1/payroll/process
   */
  async processPayroll(period, idempotencyKey = null) {
    const key = idempotencyKey || `payroll_${period}_${Date.now()}`;
    return await post('/payroll/process', { period }, key);
  },

  /**
   * Get payroll batch periods
   * GET /api/v1/payroll/periods
   */
  async getPeriods() {
    return await get('/payroll/periods');
  },

  /**
   * Get payroll records for a period
   * GET /api/v1/payroll/records
   */
  async getPeriodRecords(period) {
    return await get('/payroll/records', { period });
  },

  /**
   * Get employee payslip breakdown
   * GET /api/v1/payroll/payslip
   */
  async getPayslip(period) {
    return await get('/payroll/payslip', { period });
  },

  /**
   * Finalize and lock a payroll period (Admin)
   * POST /api/v1/payroll/periods/{period}/finalize
   */
  async finalizePeriod(period) {
    return await post(`/payroll/periods/${period}/finalize`, {});
  }
};

export default payrollApi;
