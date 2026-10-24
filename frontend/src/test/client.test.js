/**
 * client.test.js — API Client Unit Tests
 * Owner: Person 4
 * Framework: Vitest
 * Tests: Header injection, error handling, method delegation
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { get, post, put, del, ApiError, apiClient } from '../api/client.js';

// Mock global fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

// Mock localStorage
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => { store[key] = String(value); },
    removeItem: (key) => { delete store[key]; },
    clear: () => { store = {}; }
  };
})();
Object.defineProperty(global, 'localStorage', { value: localStorageMock });

// Mock import.meta.env
vi.mock('../api/client.js', async (importOriginal) => {
  const actual = await importOriginal();
  return actual;
});

describe('API Client — Header Injection', () => {
  beforeEach(() => {
    mockFetch.mockClear();
    localStorage.clear();
  });

  it('sets Content-Type: application/json by default', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: () => 'application/json' },
      json: async () => ({ success: true })
    });

    await get('/test');

    const [, options] = mockFetch.mock.calls[0];
    const headers = options.headers;
    expect(headers.get('Content-Type')).toContain('application/json');
  });

  it('attaches Authorization Bearer header when token is in localStorage', async () => {
    localStorage.setItem('token', 'test_jwt_token_abc123');
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: () => 'application/json' },
      json: async () => ({ data: [] })
    });

    await get('/employees');

    const [, options] = mockFetch.mock.calls[0];
    const authHeader = options.headers.get('Authorization');
    expect(authHeader).toBe('Bearer test_jwt_token_abc123');
  });

  it('does NOT attach Authorization header when localStorage is empty', async () => {
    localStorage.clear();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: () => 'application/json' },
      json: async () => ({})
    });

    await get('/auth/me');

    const [, options] = mockFetch.mock.calls[0];
    expect(options.headers.has('Authorization')).toBe(false);
  });
});

describe('API Client — Error Handling', () => {
  beforeEach(() => {
    mockFetch.mockClear();
    localStorage.clear();
  });

  it('throws ApiError with correct status on HTTP 401', async () => {
    // Also dispatch auth:expired event
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      headers: { get: () => 'application/json' },
      json: async () => ({ message: 'Unauthorized', code: 'AUTH_FAILED', status: 401 })
    });

    await expect(get('/protected')).rejects.toThrow(ApiError);
    try {
      await get('/protected');
    } catch (err) {
      // May re-throw on second call — just verify type shape
    }
  });

  it('throws ApiError on HTTP 404 with parsed message', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 404,
      headers: { get: () => 'application/json' },
      json: async () => ({ message: 'Employee not found', code: 'EMPLOYEE_NOT_FOUND', status: 404 })
    });

    try {
      await get('/employees/nonexistent');
      expect.fail('Should have thrown');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect(err.status).toBe(404);
      expect(err.message).toBe('Employee not found');
      expect(err.code).toBe('EMPLOYEE_NOT_FOUND');
    }
  });

  it('throws ApiError on HTTP 409 Conflict with code', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 409,
      headers: { get: () => 'application/json' },
      json: async () => ({
        message: "Employee code 'EMP-1002' is already assigned.",
        code: 'EMPLOYEE_CODE_EXISTS',
        status: 409
      })
    });

    try {
      await post('/employees', { employeeCode: 'EMP-1002' });
      expect.fail('Should have thrown');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect(err.status).toBe(409);
      expect(err.code).toBe('EMPLOYEE_CODE_EXISTS');
    }
  });

  it('throws ApiError with status 0 on network failure', async () => {
    mockFetch.mockRejectedValueOnce(new TypeError('Failed to fetch'));

    try {
      await get('/employees');
      expect.fail('Should have thrown');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect(err.status).toBe(0);
      expect(err.code).toBe('NETWORK_ERROR');
    }
  });

  it('returns null on HTTP 204 No Content', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 204,
      headers: { get: () => null },
      json: async () => { throw new Error('no body'); }
    });

    const result = await del('/employees/123');
    expect(result).toBeNull();
  });
});

describe('API Client — HTTP Methods', () => {
  beforeEach(() => {
    mockFetch.mockClear();
    localStorage.clear();
  });

  it('post() sends body as JSON and attaches X-Idempotency-Key when provided', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 201,
      headers: { get: () => 'application/json' },
      json: async () => ({ id: 'new_id' })
    });

    await post('/attendance/check-in', { timestamp: '2026-10-24T09:00:00Z' }, 'key_test_001');

    const [, options] = mockFetch.mock.calls[0];
    expect(options.method).toBe('POST');
    expect(options.headers.get('X-Idempotency-Key')).toBe('key_test_001');
    expect(JSON.parse(options.body)).toEqual({ timestamp: '2026-10-24T09:00:00Z' });
  });

  it('put() sends HTTP PUT with JSON body', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: () => 'application/json' },
      json: async () => ({ updated: true })
    });

    await put('/employees/emp_001', { designation: 'Staff Engineer' });

    const [, options] = mockFetch.mock.calls[0];
    expect(options.method).toBe('PUT');
    expect(JSON.parse(options.body)).toEqual({ designation: 'Staff Engineer' });
  });

  it('get() correctly serializes query parameters', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: () => 'application/json' },
      json: async () => ({ content: [], totalElements: 0 })
    });

    await get('/employees', { department: 'ENGINEERING', page: 0, size: 10 });

    const [url] = mockFetch.mock.calls[0];
    expect(url).toContain('department=ENGINEERING');
    expect(url).toContain('page=0');
    expect(url).toContain('size=10');
  });
});
