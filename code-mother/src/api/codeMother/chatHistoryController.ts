import { createOpenApiRequest as request } from '@/api/openapi';
import type {
  ChatHistoryQueryRequest,
  BaseResponsePageChatHistory,
  OpenApiGeneratorOptions,
  listAppChatHistoryParams
} from './typings';

/**
 * 管理员分页查询所有对话历史
 * @param body
 * @param options
 * @returns
 */
export async function listAllChatHistoryByPageForAdmin(
  body: ChatHistoryQueryRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponsePageChatHistory>({
  url: '/chatHistory/admin/list/page/vo',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 根据应用ID查询对话历史
 * @param params
 * @param options
 * @returns
 */
export async function listAppChatHistory(
  params: listAppChatHistoryParams,
  options ?: OpenApiGeneratorOptions
) {
  const { appId: param0, ...queryParams } = params;
  return request<BaseResponsePageChatHistory>({
  url: `/chatHistory/app/${param0}`,
  method: 'GET',
    params: {
      // pageSize has a default value: 10
      pageSize: '10',
      ...queryParams,
    },
    ...(options || {}),
  });
}

