import { createOpenApiRequest as request } from '@/api/openapi';
import type {
  AppAddRequest,
  AppAdminUpdateRequest,
  AppDeployRequest,
  AppQueryRequest,
  AppUpdateRequest,
  BaseResponseLong,
  BaseResponseBoolean,
  BaseResponseAppVO,
  BaseResponsePageAppVO,
  BaseResponseString,
  OpenApiGeneratorOptions,
  chatToGenCodeParams,
  getAppVOByIdByAdminParams,
  getAppVOByIdParams,
  downloadAppCodeParams,
  DeleteRequest,
  ServerSentEventString
} from './typings';

/**
 * 添加应用
 * @param body
 * @param options
 * @returns
 */
export async function addApp(body: AppAddRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseLong>({
  url: '/app/add',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 管理员删除应用
 * @param body
 * @param options
 * @returns
 */
export async function deleteAppByAdmin(body: DeleteRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/app/admin/delete',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 管理员根据ID获取应用详情
 * @param params
 * @param options
 * @returns
 */
export async function getAppVoByIdByAdmin(
  params: getAppVOByIdByAdminParams,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseAppVO>({
  url: '/app/admin/get/vo',
  method: 'GET',
    params: {
        ...params,},
    ...(options || {}),
  });
}

/**
 * 管理员分页查询应用列表
 * @param body
 * @param options
 * @returns
 */
export async function listAppVoByPageByAdmin(body: AppQueryRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponsePageAppVO>({
  url: '/app/admin/list/page/vo',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 管理员更新应用
 * @param body
 * @param options
 * @returns
 */
export async function updateAppByAdmin(body: AppAdminUpdateRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/app/admin/update',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 聊天生成代码
 * @param params
 * @param options
 * @returns
 */
export async function chatToGenCode(
  params: chatToGenCodeParams,
  options ?: OpenApiGeneratorOptions
) {
  return request<ServerSentEventString[]>({
  url: '/app/chat/gen/code',
  method: 'GET',
    params: {
        ...params,},
    ...(options || {}),
  });
}

/**
 * 删除应用
 * @param body
 * @param options
 * @returns
 */
export async function deleteApp(body: DeleteRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/app/delete',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 部署应用
 * @param body
 * @param options
 * @returns
 */
export async function deployApp(body: AppDeployRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseString>({
  url: '/app/deploy',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 下载应用代码
 * @param params
 * @param options
 * @returns
 */
export async function downloadAppCode(
  params: downloadAppCodeParams,
  options ?: OpenApiGeneratorOptions
) {
  const response = await request<{ data: Blob; headers: any }>({
    url: `/app/download/${params.appId}`,
    method: 'GET',
    responseType: 'blob',
    ...(options || {}),
  });
  return response;
}

/**
 * 根据ID获取应用详情
 * @param params
 * @param options
 * @returns
 */
export async function getAppVoById(
  params: getAppVOByIdParams,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseAppVO>({
  url: '/app/get/vo',
  method: 'GET',
    params: {
        ...params,},
    ...(options || {}),
  });
}

/**
 * 分页查询优质应用列表
 * @param body
 * @param options
 * @returns
 */
export async function listGoodAppVoByPage(body: AppQueryRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponsePageAppVO>({
  url: '/app/good/list/page/vo',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 分页查询我的应用列表
 * @param body
 * @param options
 * @returns
 */
export async function listMyAppVoByPage(body: AppQueryRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponsePageAppVO>({
  url: '/app/my/list/page/vo',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 更新应用
 * @param body
 * @param options
 * @returns
 */
export async function updateApp(body: AppUpdateRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/app/update',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
