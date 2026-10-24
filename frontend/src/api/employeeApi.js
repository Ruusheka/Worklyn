/**
 * Employee API Service
 * Owner: Person 4
 * Endpoints: /api/v1/employees/*
 */

import { get, post, put, apiClient, isMockMode } from './client.js';
import { INITIAL_EMPLOYEES } from './mockData.js';

const LOCAL_EMPLOYEES_KEY = 'worklyn_mock_employees';

function getMockEmployees() {
  try {
    const saved = localStorage.getItem(LOCAL_EMPLOYEES_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_EMPLOYEES;
}

function saveMockEmployees(list) {
  try {
    localStorage.setItem(LOCAL_EMPLOYEES_KEY, JSON.stringify(list));
  } catch (e) {
    // ignore
  }
}

function filterMockEmployees(params = {}) {
  let list = [...getMockEmployees()];
  const { department, status, search, page = 0, size = 10 } = params;

  if (department && department !== 'ALL') {
    list = list.filter(e => e.department === department);
  }
  if (status && status !== 'ALL') {
    list = list.filter(e => e.status === status);
  }
  if (search) {
    const q = search.toLowerCase();
    list = list.filter(e =>
      e.firstName.toLowerCase().includes(q) ||
      e.lastName.toLowerCase().includes(q) ||
      e.employeeCode.toLowerCase().includes(q) ||
      e.email.toLowerCase().includes(q) ||
      (e.designation && e.designation.toLowerCase().includes(q))
    );
  }

  const totalElements = list.length;
  const totalPages = Math.ceil(totalElements / size) || 1;
  const startIndex = Number(page) * Number(size);
  const pagedContent = list.slice(startIndex, startIndex + Number(size));

  return {
    content: pagedContent,
    pageNumber: Number(page),
    pageSize: Number(size),
    totalElements,
    totalPages,
    last: Number(page) >= totalPages - 1,
  };
}

export const employeeApi = {
  /**
   * Search and filter employees
   * GET /api/v1/employees
   */
  async getEmployees(params = {}) {
    if (isMockMode()) {
      return filterMockEmployees(params);
    }
    return await get('/employees', params);
  },

  /**
   * Fetch employee by ID
   * GET /api/v1/employees/{id}
   */
  async getEmployeeById(id) {
    if (isMockMode()) {
      const list = getMockEmployees();
      const found = list.find(e => e.id === id || e.employeeCode === id);
      if (found) return found;
      throw new Error(`Employee ${id} not found`);
    }
    return await get(`/employees/${id}`);
  },

  /**
   * Create employee profile
   * POST /api/v1/employees
   */
  async createEmployee(data) {
    if (isMockMode()) {
      const list = getMockEmployees();
      const newEmployee = {
        id: `emp_${Date.now()}`,
        employeeCode: data.employeeCode || `EMP-${Math.floor(1000 + Math.random() * 9000)}`,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        ...data,
        baseSalary: Number(data.baseSalary) || 75000,
      };
      list.unshift(newEmployee);
      saveMockEmployees(list);
      return newEmployee;
    }
    return await post('/employees', data);
  },

  /**
   * Update employee profile
   * PUT /api/v1/employees/{id}
   */
  async updateEmployee(id, data) {
    if (isMockMode()) {
      const list = getMockEmployees();
      const index = list.findIndex(e => e.id === id || e.employeeCode === id);
      if (index !== -1) {
        list[index] = { ...list[index], ...data };
        saveMockEmployees(list);
        return list[index];
      }
      throw new Error(`Employee ${id} not found`);
    }
    return await put(`/employees/${id}`, data);
  },

  /**
   * Update employee status
   * PATCH /api/v1/employees/{id}/status
   */
  async updateStatus(id, status) {
    if (isMockMode()) {
      const list = getMockEmployees();
      const emp = list.find(e => e.id === id || e.employeeCode === id);
      if (emp) {
        emp.status = status;
        saveMockEmployees(list);
        return emp;
      }
      throw new Error(`Employee ${id} not found`);
    }
    return await apiClient(`/employees/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  }
};

export default employeeApi;
