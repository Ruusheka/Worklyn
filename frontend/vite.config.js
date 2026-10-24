import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('error', (err, _req, res) => {
            if (!res.headersSent) {
              res.writeHead(503, { 'Content-Type': 'application/json' });
              res.end(JSON.stringify({
                status: 503,
                error: 'Service Unavailable',
                code: 'BACKEND_OFFLINE',
                message: 'Spring Boot backend is not running at http://localhost:8080. Enable mock mode by setting VITE_USE_MOCK=true.',
              }));
            }
          });
        },
      },
      '/s': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('error', (err, _req, res) => {
            if (!res.headersSent) {
              res.writeHead(503, { 'Content-Type': 'application/json' });
              res.end(JSON.stringify({
                status: 503,
                error: 'Service Unavailable',
                code: 'BACKEND_OFFLINE',
                message: 'Short-link service is unavailable because backend is offline.',
              }));
            }
          });
        },
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
  },
});
