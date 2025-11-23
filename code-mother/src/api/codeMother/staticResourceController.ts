import { createOpenApiRequest as request } from '@/api/openapi';
import type {
  serveStaticResourceParams,
  OpenApiGeneratorOptions
} from './typings';

/**
 * 提供静态资源服务
 * @param params
 * @param options
 * @returns
 */
export async function serveStaticResource(
  params: serveStaticResourceParams,
  options ?: OpenApiGeneratorOptions
) {
  const { deployKey: param0, ...queryParams } = params;
  return request<string>(`/static/${param0}/**`, {
  method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  });
}