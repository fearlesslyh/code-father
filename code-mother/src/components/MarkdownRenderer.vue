<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

// 配置 marked 的选项类型
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error('代码高亮失败:', err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

interface Props {
  content: string
}

const props = defineProps<Props>()

const htmlContent = ref('')

// 渲染 Markdown
const renderMarkdown = () => {
  if (!props.content) {
    htmlContent.value = ''
    return
  }
  
  try {
    htmlContent.value = marked.parse(props.content) as string
  } catch (error) {
    console.error('Markdown渲染失败:', error)
    htmlContent.value = props.content.replace(/\n/g, '<br>')
  }
}

watch(() => props.content, renderMarkdown, { immediate: true })

onMounted(() => {
  renderMarkdown()
})
</script>

<template>
  <div class="markdown-renderer" v-html="htmlContent"></div>
</template>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
  color: var(--text-primary);
}

.markdown-renderer :deep(h1),
.markdown-renderer :deep(h2),
.markdown-renderer :deep(h3),
.markdown-renderer :deep(h4),
.markdown-renderer :deep(h5),
.markdown-renderer :deep(h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.markdown-renderer :deep(p) {
  margin-bottom: 1em;
}

.markdown-renderer :deep(ul),
.markdown-renderer :deep(ol) {
  margin-bottom: 1em;
  padding-left: 2em;
}

.markdown-renderer :deep(li) {
  margin-bottom: 0.5em;
}

.markdown-renderer :deep(code) {
  background: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-renderer :deep(pre) {
  background: var(--bg-secondary);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin-bottom: 1em;
  border: 1px solid var(--border-primary);
}

.markdown-renderer :deep(pre code) {
  background: transparent;
  padding: 0;
  border-radius: 0;
}

.markdown-renderer :deep(blockquote) {
  border-left: 4px solid var(--primary-color);
  padding-left: var(--spacing-md);
  margin: 1em 0;
  color: var(--text-secondary);
}

.markdown-renderer :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
}

.markdown-renderer :deep(a:hover) {
  text-decoration: underline;
}

.markdown-renderer :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1em;
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  border: 1px solid var(--border-primary);
  padding: var(--spacing-sm);
  text-align: left;
}

.markdown-renderer :deep(th) {
  background: var(--bg-secondary);
  font-weight: var(--font-weight-semibold);
}
</style>

