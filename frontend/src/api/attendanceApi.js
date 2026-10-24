/**
 * Attendance API Service
 * Owner: Person 4
 * Endpoints: /api/v1/attendance/*
 */

import { get, post, isMockMode } from './client.js';
import { INITIAL_ATTENDANCE } from './mockData.js';

const LOCAL_ATTENDANCE_KEY = 'worklyn_mock_attendance';

function getMockAttendance() {
  try {
    const saved = localStorage.getItem(LOCAL_ATTENDANCE_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_ATTENDANCE;
}

function saveMockAttendance(list) {
  try {
    localStorage.setItem(LOCAL_ATTENDANCE_KEY, JSON.stringify(list));
  } catch (e) {
    // ignore
  }
}

export const attendanceApi = {
  /**
   * Daily check-in
   * POST /api/v1/attendance/check-in
   */
  async checkIn(idempotencyKey = null) {
    if (isMockMode()) {
      const list = getMockAttendance();
      const today = new Date().toISOString().split('T')[0];
      const newRecord = {
        id: `att_${Date.now()}`,
        employeeId: "6714a8f9c102b34a11000104",
        employeeName: "David Chen",
        employeeCode: "EMP-2045",
        department: "ENGINEERING",
        attendanceDate: today,
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        durationMinutes: null,
        status: "PRESENT",
        ipAddress: "127.0.0.1 (Local Preview)"
      };
      list.unshift(newRecord);
      saveMockAttendance(list);
      return newRecord;
    }

    return await post('/attendance/check-in', {
      timestamp: new Date().toISOString(),
      idempotencyKey: idempotencyKey || `chk_in_${Date.now()}`
    }, idempotencyKey);
  },

  /**
   * Daily check-out
   * POST /api/v1/attendance/check-out
   */
  async checkOut(idempotencyKey = null) {
    if (isMockMode()) {
      const list = getMockAttendance();
      const today = new Date().toISOString().split('T')[0];
      const record = list.find(r => r.attendanceDate === today && !r.checkOutTime);
      if (record) {
        record.checkOutTime = new Date().toISOString();
        const checkIn = new Date(record.checkInTime);
        const checkOut = new Date(record.checkOutTime);
        record.durationMinutes = Math.round((checkOut - checkIn) / 60000);
        saveMockAttendance(list);
        return record;
      }
      const fallback = {
        id: `att_${Date.now()}`,
        employeeId: "6714a8f9c102b34a11000104",
        employeeName: "David Chen",
        employeeCode: "EMP-2045",
        department: "ENGINEERING",
        attendanceDate: today,
        checkInTime: new Date(Date.now() - 8 * 3600000).toISOString(),
        checkOutTime: new Date().toISOString(),
        durationMinutes: 480,
        status: "PRESENT",
        ipAddress: "127.0.0.1 (Local Preview)"
      };
      list.unshift(fallback);
      saveMockAttendance(list);
      return fallback;
    }

    return await post('/attendance/check-out', {
      timestamp: new Date().toISOString(),
      idempotencyKey: idempotencyKey || `chk_out_${Date.now()}`
    }, idempotencyKey);
  },

  /**
   * Get caller attendance history
   * GET /api/v1/attendance/my-history
   */
  async getMyHistory() {
    if (isMockMode()) {
      return getMockAttendance();
    }
    return await get('/attendance/my-history');
  },

  /**
   * Get specific employee history (Admin, HR, Manager)
   * GET /api/v1/attendance/employee/{empId}
   */
  async getEmployeeHistory(empId) {
    if (isMockMode()) {
      const list = getMockAttendance();
      return list.filter(r => r.employeeId === empId || r.employeeCode === empId);
    }
    return await get(`/attendance/employee/${empId}`);
  }
};

export default attendanceApi;
