import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')

  // Proxy to API Gateway to avoid CORS during local dev.
  // Override with:
  //   VITE_API_TARGET=http://localhost:8081 npm run dev
  const apiTarget = env.VITE_API_TARGET ?? 'http://localhost:8081'

  return {
    plugins: [
      react(),
      tailwindcss(),
    ],
    server: {
      host: true,
      port: 5173,
      strictPort: true,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
        },
        '/graphql': {
          target: apiTarget,
          changeOrigin: true,
        },
        '/actuator': {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
