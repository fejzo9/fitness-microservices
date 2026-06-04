import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    allowedHosts: ['.ngrok-free.dev'],
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/users': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/nutrition': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/workouts': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/notifications': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
    }
  }
})
