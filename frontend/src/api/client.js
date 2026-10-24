/**
 * Centralized API Client for Worklyn
 * Owner: Person 4
 * Follows README specification Section 13.2
 */

export class ApiError extends Error {
  constructor(status, message, code = null, validationErrors = null, raw = null) {
    super(message || `API error with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.validationErrors = validationErrors;
    this.raw = raw;
  }
}

const DEFAULT_BASE_URL = '/api/v1';

export function getBaseUrl() {
  if (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  return DEFAULT_BASE_URL;
}

/**
 * Check whether mock mode is enabled.
 * Priority:
 * 1. URL query param ?mock=true or ?mock=false
 * 2. localStorage 'worklyn_mock_mode' ('true' | 'false')
 * 3. Environment variable VITE_USE_MOCK ('true' | 'false')
 * 4. Default: true (standalone preview without requiring Spring Boot backend)
 */
export function isMockMode() {
  if (typeof window !== 'undefined') {
    try {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('mock')) {
        return urlParams.get('mock') !== 'false';
      }
    } catch (e) {
      // ignore
    }

    try {
      const stored = localStorage.getItem('worklyn_mock_mode');
      if (stored !== null) {
        return stored === 'true';
      }
    } catch (e) {
      // ignore
    }
  }

  if (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_USE_MOCK !== undefined) {
    return String(import.meta.env.VITE_USE_MOCK).toLowerCase() === 'true';
  }

  // Default to true for independent preview
  return true;
}

export function setMockMode(enabled) {
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem('worklyn_mock_mode', String(enabled));
  }
}

/**
 * Core apiClient wrapper around fetch
 */
export async function apiClient(endpoint, options = {}) {
  const baseUrl = getBaseUrl().replace(/\/$/, '');
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  const url = `${baseUrl}${cleanEndpoint}`;

  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json; charset=UTF-8');
  }

  // Auto-attach JWT from localStorage if present
  try {
    const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null;
    if (token && !headers.has('Authorization')) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  } catch (err) {
    // ignore in environments without localStorage
  }

  const fetchOptions = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, fetchOptions);

    if (response.status === 401) {
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('auth:expired', { detail: { endpoint } }));
      }
    }

    if (!response.ok) {
      let errorData = null;
      try {
        errorData = await response.json();
      } catch (e) {
        // Response might not be JSON
        errorData = { message: response.statusText || 'Unknown server error' };
      }

      throw new ApiError(
        response.status,
        errorData?.message || `HTTP ${response.status} error`,
        errorData?.code || null,
        errorData?.validationErrors || null,
        errorData
      );
    }

    // HTTP 204 No Content
    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(0, error.message || 'Network connection failed', 'NETWORK_ERROR', null, error);
  }
}

/**
 * GET request helper with query parameter serialization
 */
export async function get(path, params = {}) {
  let queryString = '';
  if (params && Object.keys(params).length > 0) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        searchParams.append(key, String(val));
      }
    });
    const qs = searchParams.toString();
    if (qs) {
      queryString = `?${qs}`;
    }
  }

  return apiClient(`${path}${queryString}`, {
    method: 'GET',
  });
}

/**
 * POST request helper with optional X-Idempotency-Key
 */
export async function post(path, body = {}, idempotencyKey = null) {
  const headers = {};
  if (idempotencyKey) {
    headers['X-Idempotency-Key'] = idempotencyKey;
  }

  return apiClient(path, {
    method: 'POST',
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
}

/**
 * PUT request helper
 */
export async function put(path, body = {}) {
  return apiClient(path, {
    method: 'PUT',
    body: body ? JSON.stringify(body) : undefined,
  });
}

/**
 * DELETE request helper
 */
export async function del(path) {
  return apiClient(path, {
    method: 'DELETE',
  });
}

export default {
  apiClient,
  get,
  post,
  put,
  del,
  ApiError,
  isMockMode,
  setMockMode,
};
