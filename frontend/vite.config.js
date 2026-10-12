import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // /api 요청은 스프링부트(8080)로 넘김 → CORS 설정 불필요
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
