/**
 * Attendance API Service
 * Owner: Person 4
 * Endpoints: /api/v1/attendance/*
 * Follows README specification Section 14.3
 */

import { get, post } from './client.js';

export const attendanceApi = {
  /**
   * Daily check-in
   * POST /api/v1/attendance/check-in
   */
  async checkIn(idempotencyKey = null) {
    const key = idempotencyKey || `chk_in_${Date.now()}`;
    return await post('/attendance/check-in', {
      timestamp: new Date().toISOString(),
      idempotencyKey: key
    }, key);
  },

  /**
   * Daily check-out
   * POST /api/v1/attendance/check-out
   */
  async checkOut(idempotencyKey = null) {
    const key = idempotencyKey || `chk_out_${Date.now()}`;
    return await post('/attendance/check-out', {
      timestamp: new Date().toISOString(),
      idempotencyKey: key
    }, key);
  },

  /**
   * Fetch attendance history
   * GET /api/v1/attendance/history
   */
  async getHistory(params = {}) {
    return await get('/attendance/history', params);
  },

  /**
   * Alias for getHistory
   */
  async getMyHistory(params = {}) {
    return await this.getHistory(params);
  },

  /**
   * Get attendance summary KPIs
   * GET /api/v1/attendance/summary
   */
  async getSummary(date = null) {
    const params = date ? { date } : {};
    return await get('/attendance/summary', params);
  }
};

export default attendanceApi;
