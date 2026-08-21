import axios from 'axios'

// 所有业务请求共享同一前缀和超时边界，避免页面散落后端地址
export const http = axios.create({
    baseURL: '/api/v1',
    timeout: 10_000,
    headers: {
        Accept: 'application/json',
    },
})


/**
 * 对应后端 Result<T> 的前端通用响应结构
 */
export interface ApiResult<T> {
    code: string,
    message: string,
    data: T
}

/**
 * 全局响应拦截器：统一处理网络与业务异常
 */
http.interceptors.response.use(
    (response) => {
        const data = response.data
        if (data && data.code && data.code !== 'SUCCESS') {
            alert(`业务提示: ${data.message}`)
            // 抛出错误阻断组件后续的 await 逻辑
            return Promise.reject(new Error(data.message))
        }
        return response
    },
    (error) => {
        const errorMsg = error.response?.data?.message || error.message || '网络请求异常'
        alert(`系统错误: ${errorMsg}`)
        return Promise.reject(error)
    }
)