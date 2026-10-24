/**
 * Leave Management API Service
 * Owner: Person 4
 * Endpoints: /api/v1/leave/*
 */

import { get, post, isMockMode } from './client.js';
import { INITIAL_LEAVE_BALANCES, INITIAL_LEAVE_REQUESTS } from './mockData.js';

const LOCAL_REQUESTS_KEY = 'worklyn_mock_leave_requests';
const LOCAL_BALANCES_KEY = 'worklyn_mock_leave_balances';

function getMockRequests() {
  try {
    const saved = localStorage.getItem(LOCAL_REQUESTS_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_LEAVE_REQUESTS;
}

function saveMockRequests(list) {
  try {
    localStorage.setItem(LOCAL_REQUESTS_KEY, JSON.stringify(list));
  } catch (e) {
    // ignore
  }
}

function getMockBalances() {
  try {
    const saved = localStorage.getItem(LOCAL_BALANCES_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_LEAVE_BALANCES;
}

function saveMockBalances(b) {
  try {
    localStorage.setItem(LOCAL_BALANCES_KEY, JSON.stringify(b));
  } catch (e) {
    // ignore
  }
}

export const leaveApi = {
  /**
   * Apply for leave
   * POST /api/v1/leave/apply
   */
  async applyLeave(data) {
    if (isMockMode()) {
      const list = getMockRequests();
      const start = new Date(data.startDate);
      const end = new Date(data.endDate);
      const diffTime = Math.abs(end - start);
      const daysCount = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

      const newRequest = {
        id: `leave_${Date.now()}`,
        employeeId: "6714a8f9c102b34a11000104",
        employeeName: "David Chen",
        employeeCode: "EMP-2045",
        department: "ENGINEERING",
        leaveType: data.leaveType || 'CASUAL',
        startDate: data.startDate,
        endDate: data.endDate,
        daysCount: data.daysCount || daysCount || 1,
        reason: data.reason,
        status: "PENDING",
        appliedAt: new Date().toISOString(),
        managerRemarks: null
      };
      list.unshift(newRequest);
      saveMockRequests(list);
      return newRequest;
    }

    return await post('/leave/apply', data);
  },

  /**
   * Get caller leave balances
   * GET /api/v1/leave/balance
   */
  async getBalance() {
    if (isMockMode()) {
      return getMockBalances();
    }
    return await get('/leave/balance');
  },

  /**
   * List caller leave applications
   * GET /api/v1/leave/my-requests
   */
  async getMyRequests() {
    if (isMockMode()) {
      return getMockRequests();
    }
    return await get('/leave/my-requests');
  },

  /**
   * Approve leave request (Admin, HR, Manager)
   * POST /api/v1/leave/{id}/approve
   */
  async approveLeave(id, remarks = '') {
    if (isMockMode()) {
      const list = getMockRequests();
      const req = list.find(r => r.id === id);
      if (req) {
        req.status = 'APPROVED';
        req.managerRemarks = remarks || 'Approved by Manager (Mock)';
        saveMockRequests(list);

        const balances = getMockBalances();
        const typeKey = (req.leaveType || 'casual').toLowerCase();
        if (balances[typeKey]) {
          balances[typeKey].used += req.daysCount;
          balances[typeKey].remaining = Math.max(0, balances[typeKey].total - balances[typeKey].used);
          saveMockBalances(balances);
        }
        return req;
      }
      throw new Error(`Leave request ${id} not found`);
    }

    return await post(`/leave/${id}/approve`, { remarks });
  },

  /**
   * Reject leave request (Admin, HR, Manager)
   * POST /api/v1/leave/{id}/reject
   */
  async rejectLeave(id, remarks = '') {
    if (isMockMode()) {
      const list = getMockRequests();
      const req = list.find(r => r.id === id);
      if (req) {
        req.status = 'REJECTED';
        req.managerRemarks = remarks || 'Rejected by Manager (Mock)';
        saveMockRequests(list);
        return req;
      }
      throw new Error(`Leave request ${id} not found`);
    }

    return await post(`/leave/${id}/reject`, { remarks });
  },

  /**
   * Cancel leave request
   * POST /api/v1/leave/{id}/cancel
   */
  async cancelLeave(id) {
    if (isMockMode()) {
      const list = getMockRequests();
      const req = list.find(r => r.id === id);
      if (req) {
        req.status = 'CANCELLED';
        saveMockRequests(list);
        return req;
      }
      throw new Error(`Leave request ${id} not found`);
    }

    return await post(`/leave/${id}/cancel`, {});
  }
};

export default leaveApi;
