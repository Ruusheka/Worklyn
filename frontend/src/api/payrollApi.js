/**
 * Payroll API Service
 * Owner: Person 4
 * Endpoints: /api/v1/payroll/*
 */

import { get, post, isMockMode } from './client.js';
import { INITIAL_PAYROLL_PERIODS, INITIAL_PAYSLIPS } from './mockData.js';

const LOCAL_PAYROLL_PERIODS_KEY = 'worklyn_mock_payroll_periods';

function getMockPeriods() {
  try {
    const saved = localStorage.getItem(LOCAL_PAYROLL_PERIODS_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_PAYROLL_PERIODS;
}

function saveMockPeriods(list) {
  try {
    localStorage.setItem(LOCAL_PAYROLL_PERIODS_KEY, JSON.stringify(list));
  } catch (e) {
    // ignore
  }
}

export const payrollApi = {
  /**
   * Execute batch payroll calculation (Admin, HR)
   * POST /api/v1/payroll/process
   */
  async processPayroll(period, idempotencyKey = null) {
    if (isMockMode()) {
      const periods = getMockPeriods();
      let existing = periods.find(p => p.period === period);
      if (existing) {
        existing.status = 'PROCESSED';
        existing.processedAt = new Date().toISOString();
      } else {
        existing = {
          period,
          status: 'PROCESSED',
          totalEmployees: 7,
          processedEmployees: 7,
          totalGross: 647000.00,
          totalDeductions: 58230.00,
          totalNetDisbursement: 588770.00,
          processedAt: new Date().toISOString()
        };
        periods.unshift(existing);
      }
      saveMockPeriods(periods);
      return existing;
    }

    return await post('/payroll/process', { period }, idempotencyKey);
  },

  /**
   * Get all payroll records for period
   * GET /api/v1/payroll/period/{period}
   */
  async getPeriodRecords(period) {
    if (isMockMode()) {
      const periods = getMockPeriods();
      const found = periods.find(p => p.period === period);
      return found || {
        period,
        status: 'UNPROCESSED',
        totalEmployees: 7,
        processedEmployees: 0,
        totalGross: 0,
        totalDeductions: 0,
        totalNetDisbursement: 0
      };
    }

    return await get(`/payroll/period/${period}`);
  },

  /**
   * Get caller payslip for period
   * GET /api/v1/payroll/payslip
   */
  async getPayslip(period = null) {
    if (isMockMode()) {
      return INITIAL_PAYSLIPS[0];
    }

    return await get('/payroll/payslip', period ? { period } : {});
  },

  /**
   * Lock period records
   * POST /api/v1/payroll/finalize
   */
  async finalizePeriod(period) {
    if (isMockMode()) {
      const periods = getMockPeriods();
      const item = periods.find(p => p.period === period);
      if (item) {
        item.status = 'FINALIZED';
        saveMockPeriods(periods);
        return item;
      }
      throw new Error(`Period ${period} not found`);
    }

    return await post('/payroll/finalize', { period });
  }
};

export default payrollApi;
