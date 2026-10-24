/**
 * Distributed System & Utilities API Service
 * Owner: Person 4
 * Endpoints: /api/v1/autocomplete/*, /api/v1/shortlinks, /api/v1/system/*, /api/v1/crawler/*
 * Follows README specification Section 11 & 14.6-14.7
 */

import { get, post } from './client.js';

export const systemApi = {
  /**
   * Prefix suggestions for employee search (Redis + Radix Trie)
   * GET /api/v1/autocomplete/employees?q=...
   */
  async autocompleteEmployees(query) {
    if (!query || query.trim().length < 2) return [];
    return await get('/autocomplete/employees', { q: query.trim() });
  },

  /**
   * Create shortened URL
   * POST /api/v1/shortlinks
   */
  async createShortlink(destinationUrl) {
    return await post('/shortlinks', { destinationUrl });
  },

  /**
   * Inspect current consistent hash ring snapshot
   * GET /api/v1/system/hash-ring
   */
  async getHashRing() {
    return await get('/system/hash-ring');
  },

  /**
   * Trigger controlled crawl of whitelisted job board
   * POST /api/v1/crawler/jobs
   */
  async triggerCrawl(sourceUrl) {
    return await post('/crawler/jobs', { sourceUrl });
  }
};

export default systemApi;
