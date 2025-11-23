/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export enum CodeGenTypeEnum {
  /** 原生 HTML 模式 */
  HTML = 'html',
  /** 原生多文件模式 */
  MULTI_FILE = 'multi_file'
}

/**
 * 代码生成类型显示名称映射
 */
export const CodeGenTypeNames: Record<CodeGenTypeEnum, string> = {
  [CodeGenTypeEnum.HTML]: '原生 HTML 模式',
  [CodeGenTypeEnum.MULTI_FILE]: '原生多文件模式'
}

/**
 * 代码生成类型选项列表（用于下拉选择框）
 */
export const CodeGenTypeOptions = [
  { label: CodeGenTypeNames[CodeGenTypeEnum.HTML], value: CodeGenTypeEnum.HTML },
  { label: CodeGenTypeNames[CodeGenTypeEnum.MULTI_FILE], value: CodeGenTypeEnum.MULTI_FILE }
]

/**
 * 获取代码生成类型显示名称
 */
export function getCodeGenTypeName(type?: string): string {
  if (!type) return '未知类型'
  return CodeGenTypeNames[type as CodeGenTypeEnum] || type
}

