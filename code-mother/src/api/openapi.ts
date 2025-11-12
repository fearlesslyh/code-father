import type { AxiosRequestConfig } from 'axios'
import { request } from './index'

export interface OpenApiGeneratorOptions extends AxiosRequestConfig {
  showErrorMessage?: boolean
}

const normalizeConfig = (
  config: string | AxiosRequestConfig,
  options?: OpenApiGeneratorOptions,
): AxiosRequestConfig => {
  const baseConfig: AxiosRequestConfig = typeof config === 'string' ? { url: config } : config
  const mergedConfig: OpenApiGeneratorOptions = {
    ...baseConfig,
    ...(options ?? {}),
  }
  const { showErrorMessage: _showErrorMessage, ...axiosConfig } = mergedConfig

  if (typeof axiosConfig.url === 'string' && !axiosConfig.url.startsWith('/')) {
    axiosConfig.url = `/${axiosConfig.url}`
  }

  return axiosConfig
}

export const createOpenApiRequest = <T>(
  config: string | AxiosRequestConfig,
  options?: OpenApiGeneratorOptions,
): Promise<T> => {
  const finalConfig = normalizeConfig(config, options)
  return request<T>(finalConfig)
}
