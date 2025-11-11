export default {
  schemaPath: 'http://localhost:8123/api/v3/api-docs',
  serversPath: './src/api',
  requestLibPath: "import { createOpenApiRequest as request } from '@/api/openapi';",
  requestOptionsType: 'OpenApiGeneratorOptions',
  namespace: false,
  useStaticMethod: true,
  projectName: 'codeMother',
  mock: false,
  hook: {
    customFunctionName(path: string, method: string | undefined) {
      const normalizedMethod = typeof method === 'string' ? method.toLowerCase() : 'get'
      if (typeof path !== 'string' || path.length === 0) {
        return normalizedMethod
      }
      const formattedSegments = path
        .split('/')
        .filter(Boolean)
        .map((segment) =>
          segment
            .replace(/\{(.*)\}/, '$1')
            .replace(/(^|[-_])(\w)/g, (_, __, letter) => letter.toUpperCase()),
        )
        .join('')
      return `${normalizedMethod}${formattedSegments}`
    },
  },
}
