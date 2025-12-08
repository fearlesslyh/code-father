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
  id?: string;
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
  userPassword?: string;
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
};

export type UserUpdateMyProfileRequest = {
  userName?: string
  userAvatar?: string
  userProfile?: string
  currentPassword?: string
  newPassword?: string
  checkNewPassword?: string
};

// App相关类型定义
export type AppAddRequest = {
  initPrompt?: string;
};

export type AppAdminUpdateRequest = {
  id?: string;
  appName?: string;
  cover?: string;
  priority?: number;
};

export type AppDeployRequest = {
  appId?: string;
};

export type AppQueryRequest = {
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
  id?: string;
  appName?: string;
  cover?: string;
  initPrompt?: string;
  codeGenType?: string;
  deployKey?: string;
  priority?: number;
  userId?: string;
  visibility?: string; // 可见范围：public/private
  tags?: string[]; // 标签数组
};

export type downloadAppCodeParams = {
  appId: number;
};

export type AppUpdateRequest = {
  id?: string;
  appName?: string;
};

export type AppVO = {
  id?: string;
  appName?: string;
  cover?: string;
  initPrompt?: string;
  codeGenType?: string;
  deployKey?: string;
  deployedTime?: string;
  priority?: number;
  visibility?: string; // 可见范围：public/private
  tags?: string[]; // 标签数组
  version?: number; // 版本号
  generationStatus?: string; // 生成状态：idle/generating/completed/failed
  userId?: string;
  createTime?: string;
  updateTime?: string;
  user?: UserVO;
};

export type BaseResponseAppVO = {
  code?: number;
  data?: AppVO;
  message?: string;
};

export type BaseResponsePageAppVO = {
  code?: number;
  data?: PageAppVO;
  message?: string;
};

export type PageAppVO = {
  records?: AppVO[];
  pageNumber?: number;
  pageSize?: number;
  totalPage?: number;
  totalRow?: number;
  optimizeCountQuery?: boolean;
};

export type ServerSentEventString = true;

export type chatToGenCodeParams = {
  appId: string;
  message: string;
};

export type getAppVOByIdByAdminParams = {
  id: string;
};

export type getAppVOByIdParams = {
  id: string;
};

export type serveStaticResourceParams = {
  deployKey: string;
};

// ChatHistory相关类型定义
export type ChatHistory = {
  id?: number;
  message?: string;
  messageType?: string;
  appId?: string;
  userId?: string;
  createTime?: string;
  updateTime?: string;
  isDelete?: number;
};

export type ChatHistoryQueryRequest = {
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
  id?: number;
  message?: string;
  messageType?: string;
  appId?: string;
  userId?: string;
  lastCreateTime?: string;
};

export type listAppChatHistoryParams = {
  appId: string;
  pageSize?: number;
  lastCreateTime?: string;
};

export type PageChatHistory = {
  records?: ChatHistory[];
  pageNumber?: number;
  pageSize?: number;
  totalPage?: number;
  totalRow?: number;
  optimizeCountQuery?: boolean;
};

export type BaseResponsePageChatHistory = {
  code?: number;
  data?: PageChatHistory;
  message?: string;
};

