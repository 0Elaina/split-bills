import axios from 'axios'

// 所有业务请求共享同一前缀和超时边界，避免页面散落后端地址
export const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10_000,
  headers: {
    Accept: 'application/json',
  },
})
