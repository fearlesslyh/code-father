export type BaseResponseBoolean = {
  code?: number;
  data?: boolean;
  message?: string;
};

export type BaseResponseLoginUserVO = {
  code?: number;
  data?: LoginUserVO;
  message?: string;
};

export type BaseResponseLong = {
  code?: number;
  data?: number;
  message?: string;
};

export type BaseResponsePageUserVO = {
  code?: number;
  data?: PageUserVO;
  message?: string;
};

export type BaseResponseString = {
  code?: number;
  data?: string;
  message?: string;
};

export type BaseResponseUser = {
  code?: number;
  data?: User;
  message?: string;
};

export type BaseResponseUserVO = {
  code?: number;
  data?: UserVO;
  message?: string;
};

export type DeleteRequest = {
  id?: number;
};

export type getParams = {
  id: number;
};

export type LoginUserVO = {
  id?: number;
  userAccount?: string;
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
  createTime?: string;
  updateTime?: string;
};

export type PageUserVO = {
  records?: UserVO[];
  pageNumber?: number;
  pageSize?: number;
  totalPage?: number;
  totalRow?: number;
  optimizeCountQuery?: boolean;
};

export type User = {
  id?: number;
  userAccount?: string;
  userPassword?: string;
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
  editTime?: string;
  createTime?: string;
  updateTime?: string;
  isDelete?: number;
  vipExpireTime?: string;
  vipCode?: string;
  vipNumber?: number;
  shareCode?: string;
  inviteUser?: number;
};

export type UserAddRequest = {
  userName?: string;
  userAccount?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
};

export type UserLoginRequest = {
  userAccount?: string;
  userPassword?: string;
};

export type UserQueryRequest = {
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
  id?: number;
  userName?: string;
  userAccount?: string;
  userProfile?: string;
  userRole?: string;
};

export type UserRegisterRequest = {
  userAccount?: string;
  userPassword?: string;
  checkPassword?: string;
};

export type UserUpdateRequest = {
  id?: number;
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
};

export type UserVO = {
  id?: number;
  userAccount?: string;
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
  createTime?: string;
};

export type OpenApiGeneratorOptions = {
  axios?: unknown,
  mock?: boolean,
  mockData?: unknown
}

