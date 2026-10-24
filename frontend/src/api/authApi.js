/**
 * Authentication API Service
 * Owner: Person 4
 * Endpoints: /api/v1/auth/*
 * Follows README specification Section 14.1
 */

import { post, get } from './client.js';

export const authApi = {
  /**
   * Authenticate user with email and password
   * POST /api/v1/auth/login
   */
  async login(email, password) {
    return await post('/auth/login', { email, password });
  },

  /**
   * Provision a new user account (ROLE_ADMIN)
   * POST /api/v1/auth/register
   */
  async register(userData) {
    return await post('/auth/register', userData);
  },

  /**
   * Retrieve current user profile and roles
   * GET /api/v1/auth/me
   */
  async getMe() {
    return await get('/auth/me');
  }
};

export default authApi;
