import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeMode = 'dark' | 'light'

const THEME_STORAGE_KEY = 'app-theme-mode'

export const useThemeStore = defineStore('theme', () => {
  // 从localStorage读取主题，默认为白天模式
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as ThemeMode | null
  const currentTheme = ref<ThemeMode>(savedTheme || 'light')

  // 切换主题
  const toggleTheme = () => {
    currentTheme.value = currentTheme.value === 'dark' ? 'light' : 'dark'
  }

  // 设置指定主题
  const setTheme = (theme: ThemeMode) => {
    currentTheme.value = theme
  }

  // 监听主题变化并保存到localStorage
  watch(currentTheme, (newTheme) => {
    localStorage.setItem(THEME_STORAGE_KEY, newTheme)
    // 应用主题到document
    document.documentElement.setAttribute('data-theme', newTheme)
  }, { immediate: true })

  return {
    currentTheme,
    toggleTheme,
    setTheme,
  }
})
