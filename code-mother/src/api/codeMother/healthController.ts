// @ts-ignore
/* eslint-disable */
import { createOpenApiRequest as request } from '@/api/openapi'


/** 此处后端没有提供注释 GET /health/ */
export async function healthCheck(options?: OpenApiGeneratorOptions) {
  return request<BaseResponseString>({
    url: '/health/',
    method: 'GET',
  }, options)
}
