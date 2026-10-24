/**
 * Distributed System & Utilities API Service
 * Owner: Person 4
 * Endpoints: /api/v1/autocomplete/*, /api/v1/shortlinks, etc.
 */

import { get, post, isMockMode } from './client.js';
import { INITIAL_EMPLOYEES } from './mockData.js';

export const systemApi = {
  /**
   * Prefix suggestions for employee search
   * GET /api/v1/autocomplete/employees?q=...
   */
  async autocompleteEmployees(query) {
    if (!query || query.trim() === '') return [];
    if (isMockMode()) {
      const q = query.toLowerCase();
      return INITIAL_EMPLOYEES
        .filter(e =>
          e.firstName.toLowerCase().startsWith(q) ||
          e.lastName.toLowerCase().startsWith(q) ||
          `${e.firstName} ${e.lastName}`.toLowerCase().includes(q) ||
          e.employeeCode.toLowerCase().includes(q)
        )
        .map(e => ({
          id: e.id,
          employeeCode: e.employeeCode,
          name: `${e.firstName} ${e.lastName}`,
          department: e.department,
          designation: e.designation,
        }))
        .slice(0, 5);
    }

    return await get('/autocomplete/employees', { q: query });
  },

  /**
   * Create shortened URL
   * POST /api/v1/shortlinks
   */
  async createShortlink(destinationUrl) {
    if (isMockMode()) {
      const code = Math.random().toString(36).substring(2, 8);
      return {
        shortCode: code,
        shortUrl: `${typeof window !== 'undefined' ? window.location.origin : 'http://localhost:5173'}/s/${code}`,
        destinationUrl,
        createdAt: new Date().toISOString()
      };
    }

    return await post('/shortlinks', { destinationUrl });
  },

  /**
   * Inspect current consistent hash ring snapshot
   * GET /api/v1/system/hash-ring
   */
  async getHashRing() {
    if (isMockMode()) {
      return {
        ringSize: 450,
        shards: ["shard-alpha", "shard-beta", "shard-gamma"],
        vnodesPerShard: 150,
        status: "HEALTHY (Mock Snapshot)"
      };
    }

    return await get('/system/hash-ring');
  },

  /**
   * Trigger controlled crawl of whitelisted job board
   * POST /api/v1/crawler/jobs
   */
  async triggerCrawl(sourceUrl) {
    if (isMockMode()) {
      return {
        status: "SUCCESS (Mock Crawl)",
        jobsCrawled: 12,
        sourceUrl
      };
    }

    return await post('/crawler/jobs', { sourceUrl });
  }
};

export default systemApi;
