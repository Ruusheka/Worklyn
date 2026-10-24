/**
 * Employee API Service
 * Owner: Person 4
 * Endpoints: /api/v1/employees/*
 * Follows README specification Section 14.2
 */

import { get, post, put, patch } from './client.js';

export const employeeApi = {
  /**
   * Search and filter employees
   * GET /api/v1/employees
   */
  async getEmployees(params = {}) {
    const cleanParams = {};
    if (params.page !== undefined && params.page !== null) cleanParams.page = params.page;
    if (params.size !== undefined && params.size !== null) cleanParams.size = params.size;
    if (params.q) cleanParams.q = params.q;
    else if (params.search) cleanParams.q = params.search;
    if (params.department && params.department !== 'ALL') cleanParams.department = params.department;
    if (params.status && params.status !== 'ALL') cleanParams.status = params.status;
    if (params.sortBy) cleanParams.sortBy = params.sortBy;
    if (params.direction) cleanParams.direction = params.direction;
    return await get('/employees', cleanParams);
  },

  /**
   * Fetch employee by ID
   * GET /api/v1/employees/{id}
   */
  async getEmployeeById(id) {
    return await get(`/employees/${id}`);
  },

  /**
   * Create employee profile
   * POST /api/v1/employees
   */
  async createEmployee(data) {
    return await post('/employees', data);
  },

  /**
   * Update employee profile
   * PUT /api/v1/employees/{id}
   */
  async updateEmployee(id, data) {
    return await put(`/employees/${id}`, data);
  },

  /**
   * Update employee status
   * PATCH /api/v1/employees/{id}/status
   */
  async updateStatus(id, status) {
    return await patch(`/employees/${id}/status`, { status });
  }
};

export default employeeApi;
