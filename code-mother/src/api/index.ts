import type { AxiosRequestConfig } from 'axios';
import { httpClient } from './http';
export type { OpenApiGeneratorOptions } from './openapi';

export const request = async <T>(config: AxiosRequestConfig): Promise<T> => {
  return httpClient.request<T, T>(config);
};

export const get = async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({
    ...config,
    method: 'GET',
    url,
  });
};

export const post = async <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({
    ...config,
    method: 'POST',
    url,
    data,
  });
};

export const put = async <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({
    ...config,
    method: 'PUT',
    url,
    data,
  });
};

export const del = async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({
    ...config,
    method: 'DELETE',
    url,
  });
};
