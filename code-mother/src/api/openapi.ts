import type { AxiosRequestConfig } from 'axios'
import { request } from './index'

export interface OpenApiGeneratorOptions {
  signal?: AbortSignal
  showErrorMessage?: boolean
}

export const createOpenApiRequest = <T>(
  config: AxiosRequestConfig,
  options?: OpenApiGeneratorOptions,
): Promise<T> => {
  const mergedConfig: AxiosRequestConfig = {
    ...config,
    signal: options?.signal,
  }
  return request<T>({
    ...mergedConfig,
    ...(options?.showErrorMessage === false ? { showErrorMessage: false } : {}),
  })
}
