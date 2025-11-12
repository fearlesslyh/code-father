import axios, { type AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'ant-design-vue';

type Nullable<T> = T | null;

export interface ApiSuccessResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface ApiErrorResponse {
  code: number;
  message: string;
  data?: unknown;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';
const REQUEST_TIMEOUT = 10000;
const AUTH_TOKEN_STORAGE_KEY = 'codeFatherAuthToken';
const SUCCESS_CODE = 0;
const AUTH_ERROR_CODES = new Set([10001, 10002]);

const getStoredAuthToken = (): Nullable<string> => {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY);
};

export const persistAuthToken = (token: string): void => {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
};

export const clearStoredAuthToken = (): void => {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
};

const handleAuthFailure = (): void => {
  clearStoredAuthToken();
  message.error('登录状态已失效，请重新登录');
};

const transformResponse = <T>(response: AxiosResponse<ApiSuccessResponse<T> | ApiErrorResponse>): T => {
  const body = response.data;

  if (typeof body !== 'object' || body === null || typeof body.code !== 'number') {
    message.error('接口返回格式异常');
    throw new Error('Unexpected API response structure');
  }

  if (body.code === SUCCESS_CODE) {
    return body as T;
  }

  if (AUTH_ERROR_CODES.has(body.code)) {
    handleAuthFailure();
  } else {
    message.error(body.message || '请求失败');
  }

  throw body as ApiErrorResponse;
};

const createHttpClient = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: REQUEST_TIMEOUT,
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = getStoredAuthToken();
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
      if (config.url && config.url.startsWith('/')) {
        config.url = config.url.replace(/\/+/g, '/');
      }
      return config;
    },
    (error: AxiosError) => Promise.reject(error),
  );

  instance.interceptors.response.use(
    (response: AxiosResponse) => transformResponse(response),
    (error: AxiosError<ApiErrorResponse>) => {
      if (error.response) {
        const { code, message: serverMessage } = error.response.data ?? {};
        if (code && AUTH_ERROR_CODES.has(code)) {
          handleAuthFailure();
        } else {
          message.error(serverMessage ?? error.message ?? '网络错误');
        }
      } else {
        message.error(error.message ?? '网络错误');
      }
      return Promise.reject(error);
    },
  );

  return instance;
};

export const httpClient = createHttpClient();
