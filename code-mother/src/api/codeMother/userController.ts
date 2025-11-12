import { createOpenApiRequest as request } from '@/api/openapi';
import type {
  UserAddRequest,
  OpenApiGeneratorOptions,
  BaseResponseLong,
  DeleteRequest,
  BaseResponseBoolean,
  getParams,
  BaseResponseUser,
  BaseResponseLoginUserVO,
  BaseResponseUserVO,
  UserQueryRequest,
  BaseResponsePageUserVO,
  UserLoginRequest,
  UserRegisterRequest,
  UserUpdateRequest
} from './typings';

/**
 * 添加用户
 * @param body 
 * @param options 
 * @returns 
 */
export async function addUser(body: UserAddRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseLong>({
  url: '/user/add',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 删除用户
 * @param body 
 * @param options 
 * @returns 
 */
export async function deleteUser(body: DeleteRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/user/delete',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 根据id获取用户
 * @param params 
 * @param getParams 
 * @param options 
 * @returns 
 */
export async function getUserById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: getParams,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseUser>({
  url: '/user/get',
  method: 'GET',
    params: {
        ...params,},
    ...(options || {}),
  });
}


/**
 * 获取当前登录用户
 * @param options 
 * @returns 
 */
export async function getLoginUser(
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseLoginUserVO>({
  url: '/user/get/login',
  method: 'GET',
    ...(options || {}),
  });
}

/**
 * 根据id获取用户详情
 * @param params 
 * @param getParams 
 * @param options 
 * @returns 
 */
export async function getUserVOById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: getParams,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseUserVO>({
  url: '/user/get/vo',
  method: 'GET',
    params: {
        ...params,},
    ...(options || {}),
  });
}
/**
 * 分页查询用户列表
 * @param body 
 * @param options 
 * @returns 
 */
export async function listUserVOByPage(body: UserQueryRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponsePageUserVO>({
  url: '/user/list/page/vo',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
 
/**
 * 用户登录
 * @param body 
 * @param options 
 * @returns 
 */
export async function userLogin(body: UserLoginRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseLoginUserVO>({
  url: '/user/login',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 *  用户登出
 * @param options 
 * @returns 
 */
export async function userLogout(
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/user/logout',
  method: 'POST',
    ...(options || {}),
  });
}

/**
 * 用户注册
 * @param body 
 * @param options 
 * @returns 
 */
export async function userRegister(body: UserRegisterRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseLong>({
  url: '/user/register',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/**
 * 更新用户
 * @param body 
 * @param options 
 * @returns 
 */
export async function updateUser(body: UserUpdateRequest,
  options ?: OpenApiGeneratorOptions
) {
  return request<BaseResponseBoolean>({
  url: '/user/update',
  method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

