import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Proxy to API Gateway to avoid CORS during local dev.
// If your api-gateway runs on another port, override with:
//   VITE_API_TARGET=http://localhost:XXXX npm run dev
const apiTarget = process.env.VITE_API_TARGET ?? 'http://localhost:8080'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': new URL('./src', import.meta.url).pathname,
    },
  },
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
      },
    },
  },
})

