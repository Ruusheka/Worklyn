/**
 * Authentication API Service
 * Owner: Person 4
 * Endpoints: /api/v1/auth/*
 */

import { post, get, isMockMode } from './client.js';
import { INITIAL_USERS } from './mockData.js';

const LOCAL_USERS_KEY = 'worklyn_mock_users';

function getMockUsers() {
  try {
    const saved = localStorage.getItem(LOCAL_USERS_KEY);
    if (saved) return JSON.parse(saved);
  } catch (e) {
    // fallback
  }
  return INITIAL_USERS;
}

export const authApi = {
  /**
   * Authenticate user with email and password
   * POST /api/v1/auth/login
   */
  async login(email, password) {
    if (isMockMode()) {
      const users = getMockUsers();
      const found = users.find(u => u.email.toLowerCase() === email.toLowerCase());
      if (found && (password === found.password || password === 'Password@123' || password === 'password')) {
        const fakeToken = `mock_jwt_token_${found.id}_${Date.now()}`;
        return {
          token: fakeToken,
          tokenType: 'Bearer',
          expiresIn: 86400,
          user: {
            id: found.id,
            email: found.email,
            firstName: found.firstName,
            lastName: found.lastName,
            roles: found.roles,
            employeeId: found.employeeId,
            avatar: found.avatar,
            designation: found.designation,
            department: found.department,
          }
        };
      }
      throw new Error('Invalid email or password');
    }

    try {
      return await post('/auth/login', { email, password });
    } catch (error) {
      throw error;
    }
  },

  /**
   * Provision a new user account (ROLE_ADMIN)
   * POST /api/v1/auth/register
   */
  async register(userData) {
    if (isMockMode()) {
      const users = getMockUsers();
      const newUser = {
        id: `usr_${Date.now()}`,
        ...userData,
        employeeId: `emp_${Date.now()}`,
      };
      users.push(newUser);
      localStorage.setItem(LOCAL_USERS_KEY, JSON.stringify(users));
      return {
        id: newUser.id,
        email: newUser.email,
        firstName: newUser.firstName,
        lastName: newUser.lastName,
        roles: newUser.roles,
        employeeId: newUser.employeeId,
      };
    }

    return await post('/auth/register', userData);
  },

  /**
   * Retrieve current user profile and roles
   * GET /api/v1/auth/me
   */
  async getMe() {
    if (isMockMode()) {
      const savedUser = localStorage.getItem('user');
      if (savedUser) {
        try {
          return JSON.parse(savedUser);
        } catch (e) {
          // ignore
        }
      }
      const savedToken = localStorage.getItem('token');
      if (savedToken && savedToken.startsWith('mock_jwt_token_')) {
        const userId = savedToken.split('_')[3];
        const users = getMockUsers();
        const user = users.find(u => u.id === userId) || users[0];
        return {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          roles: user.roles,
          employeeId: user.employeeId,
          avatar: user.avatar,
          designation: user.designation,
          department: user.department,
        };
      }
      return INITIAL_USERS[0];
    }

    return await get('/auth/me');
  }
};

export default authApi;
