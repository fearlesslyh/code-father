/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export enum CodeGenTypeEnum {
  /** 原生 HTML 模式 */
  HTML = 'html',
  /** 原生多文件模式 */
  MULTI_FILE = 'multi_file',
  /** Vue 项目模式 */
  VUE_PROJECT = 'vue_project'
}

/**
 * 代码生成类型显示名称映射
 */
export const CodeGenTypeNames: Record<CodeGenTypeEnum, string> = {
  [CodeGenTypeEnum.HTML]: '原生 HTML 模式',
  [CodeGenTypeEnum.MULTI_FILE]: '原生多文件模式',
  [CodeGenTypeEnum.VUE_PROJECT]: 'Vue 项目模式'
}

/**
 * 代码生成类型选项列表（用于下拉选择框）
 */
export const CodeGenTypeOptions = [
  { label: CodeGenTypeNames[CodeGenTypeEnum.HTML], value: CodeGenTypeEnum.HTML },
  { label: CodeGenTypeNames[CodeGenTypeEnum.MULTI_FILE], value: CodeGenTypeEnum.MULTI_FILE },
  { label: CodeGenTypeNames[CodeGenTypeEnum.VUE_PROJECT], value: CodeGenTypeEnum.VUE_PROJECT }
]

/**
 * 获取代码生成类型显示名称
 */
export function getCodeGenTypeName(type?: string): string {
  if (!type) return '未知类型'
  return CodeGenTypeNames[type as CodeGenTypeEnum] || type
}

/**
 * 静态资源基础访问地址
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

/**
 * 获取静态资源预览 URL
 * 如果是 Vue 项目，浏览地址需要添加 dist/index.html
 */
export const getStaticPreviewUrl = (codeGenType: string, appId: string | number) => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}


