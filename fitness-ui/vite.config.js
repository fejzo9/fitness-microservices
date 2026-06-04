import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const apiBase = env.VITE_API_BASE || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 3000,
      allowedHosts: ['.ngrok-free.dev'],
      proxy: {
        '/auth': {
          target: apiBase,
          changeOrigin: true
        },
        '/users': {
          target: apiBase,
          changeOrigin: true
        },
        '/nutrition': {
          target: apiBase,
          changeOrigin: true
        },
        '/workouts': {
          target: apiBase,
          changeOrigin: true
        },
        '/notifications': {
          target: apiBase,
          changeOrigin: true
        },
      }
    }
  }
})
