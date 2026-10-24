/**
 * AuthContext.test.jsx — Auth Context Unit Tests
 * Owner: Person 4
 * Framework: React Testing Library + Vitest
 */

import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../context/AuthContext.jsx';

// Mock authApi
vi.mock('../api/authApi.js', () => ({
  authApi: {
    login: vi.fn(),
    getMe: vi.fn(),
  }
}));

import { authApi } from '../api/authApi.js';

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

function TestConsumer() {
  const { user, isAuthenticated, isLoading, login, logout, hasRole, hasAnyRole } = useAuth();

  return (
    <div>
      <div data-testid="is-loading">{String(isLoading)}</div>
      <div data-testid="is-authenticated">{String(isAuthenticated)}</div>
      <div data-testid="user-email">{user?.email || 'none'}</div>
      <div data-testid="has-admin-role">{String(hasRole('ROLE_ADMIN'))}</div>
      <div data-testid="has-manager-role">{String(hasRole('ROLE_MANAGER'))}</div>
      <div data-testid="has-any-role">{String(hasAnyRole(['ROLE_MANAGER', 'ROLE_HR']))}</div>
      <button
        data-testid="login-btn"
        onClick={() => login('hr.admin@worklyn.io', 'Password@123')}
      >
        Login
      </button>
      <button data-testid="logout-btn" onClick={logout}>
        Logout
      </button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    authApi.login.mockClear();
    authApi.getMe.mockClear();
  });

  it('starts unauthenticated when no token in localStorage', async () => {
    authApi.getMe.mockRejectedValue({ status: 401 });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('is-loading').textContent).toBe('false');
    });

    expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
    expect(screen.getByTestId('user-email').textContent).toBe('none');
  });

  it('saves token to localStorage on successful login', async () => {
    authApi.getMe.mockRejectedValue({ status: 0 });
    authApi.login.mockResolvedValueOnce({
      token: 'test_token_abc',
      tokenType: 'Bearer',
      expiresIn: 86400,
      user: {
        id: 'uid_01',
        email: 'hr.admin@worklyn.io',
        firstName: 'Sarah',
        lastName: 'Jenkins',
        roles: ['ROLE_ADMIN', 'ROLE_HR'],
        employeeId: 'emp_01'
      }
    });

    const { getByTestId } = render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(getByTestId('is-loading').textContent).toBe('false');
    });

    await act(async () => {
      getByTestId('login-btn').click();
    });

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('test_token_abc');
    });

    expect(getByTestId('is-authenticated').textContent).toBe('true');
    expect(getByTestId('user-email').textContent).toBe('hr.admin@worklyn.io');
  });

  it('clears token from localStorage on logout', async () => {
    localStorage.setItem('token', 'existing_token');
    authApi.getMe.mockResolvedValueOnce({
      id: 'uid_01', email: 'hr.admin@worklyn.io', firstName: 'Sarah',
      lastName: 'Jenkins', roles: ['ROLE_ADMIN'], employeeId: 'emp_01'
    });

    const { getByTestId } = render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(getByTestId('is-loading').textContent).toBe('false');
    });

    await act(async () => {
      getByTestId('logout-btn').click();
    });

    expect(localStorage.getItem('token')).toBeNull();
    expect(getByTestId('is-authenticated').textContent).toBe('false');
  });

  it('correctly evaluates hasRole and hasAnyRole', async () => {
    localStorage.setItem('token', 'existing_token');
    authApi.getMe.mockResolvedValueOnce({
      id: 'uid_01',
      email: 'hr.admin@worklyn.io',
      firstName: 'Sarah',
      lastName: 'Jenkins',
      roles: ['ROLE_ADMIN', 'ROLE_HR'],
      employeeId: 'emp_01'
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('is-loading').textContent).toBe('false');
    });

    expect(screen.getByTestId('has-admin-role').textContent).toBe('true');
    expect(screen.getByTestId('has-manager-role').textContent).toBe('false');
    expect(screen.getByTestId('has-any-role').textContent).toBe('true');
  });

  it('logs out automatically on auth:expired event', async () => {
    localStorage.setItem('token', 'existing_token');
    authApi.getMe.mockResolvedValueOnce({
      id: 'uid_01',
      email: 'hr.admin@worklyn.io',
      firstName: 'Sarah',
      lastName: 'Jenkins',
      roles: ['ROLE_ADMIN'],
      employeeId: 'emp_01'
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
    });

    act(() => {
      window.dispatchEvent(new CustomEvent('auth:expired'));
    });

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
    });
    expect(localStorage.getItem('token')).toBeNull();
  });
});
