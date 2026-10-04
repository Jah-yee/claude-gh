import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

const sales = process.env.SALES_URL ?? 'http://localhost:8081';
const inventory = process.env.INVENTORY_URL ?? 'http://localhost:8082';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Same routing as nginx.conf in the Docker image.
    proxy: {
      '/api/sales': sales,
      '/api/alerts': sales,
      '/api/products': inventory,
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test-setup.ts'],
  },
});
