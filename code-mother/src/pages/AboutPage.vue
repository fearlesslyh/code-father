<template>
  <div class="about">
    <div class="about-content">
      <!-- 主标题 -->
      <h1 class="about-title">凌犀零代码 — 让创意自由落地</h1>
      
      <!-- 副标题 -->
      <p class="about-subtitle">
        无需编码，即可快速构建应用。让每一位创作者都能把想法转化为现实，释放你的无限想象力。
      </p>
      
      <!-- 核心介绍区 -->
      <div class="about-sections">
        <!-- 我是谁 -->
        <section class="about-section">
          <h2 class="section-title">我是谁</h2>
          <p class="section-text">
            我是<strong>梁懿豪</strong>。一个有梦想，热爱生活的 Coder。这是我独立打造的零代码平台，专注于降低软件开发门槛，用科技赋能每一个创意灵感。
          </p>
        </section>
        
        <!-- 理念 -->
        <section class="about-section">
          <h2 class="section-title">理念</h2>
          <p class="section-text">
            <strong>零门槛、高效率、智能化</strong>——这是凌犀的核心理念。无论你是开发者、设计师，还是创业者，都能在平台上快速实现你的想法。我坚信"技术应服务于创意，而不是束缚想象"。
          </p>
          <p class="section-text">
            从平台架构到用户界面设计，每一处细节都追求简洁、直观、灵活，同时注重交互的未来感。
          </p>
          <p class="section-text">
            我不仅精通前后端开发，还兼具设计与用户体验理念，致力于打造一个有温度、有科技感的产品体验。
          </p>
        </section>
        
        <!-- 平台特色 -->
        <section class="about-section">
          <h2 class="section-title">平台特色</h2>
          <div class="features-grid">
            <div class="feature-item">
              <div class="feature-icon">🎨</div>
              <h3 class="feature-title">可视化构建</h3>
              <p class="feature-desc">通过直观拖拽，快速生成应用界面与逻辑</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🤖</div>
              <h3 class="feature-title">智能辅助</h3>
              <p class="feature-desc">内置 AI 提示与代码生成，让开发更轻松</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🚀</div>
              <h3 class="feature-title">多场景适用</h3>
              <p class="feature-desc">适合内部工具、创意应用或小型产品原型</p>
            </div>
          </div>
        </section>
        
        <!-- 创始人信息 -->
        <section class="about-section founder-section">
          <div class="founder-card">
            <h3 class="founder-name">梁懿豪</h3>
            <p class="founder-title">创始人 & 技术总监</p>
            <p class="founder-quote">
              秉承"简单即美"的原则，把复杂开发隐藏在易用的界面之下，让每个人都能轻松创造。
            </p>
          </div>
        </section>
      </div>
      
      <!-- Footer 信息 -->
      <footer class="about-footer">
        <p class="footer-copyright">© 2025 凌犀零代码平台. 让每一个创意都有机会落地。</p>
        <div class="contact-info">
          <p class="contact-item">📧 邮箱：fearless0101@qq.com</p>
          <p class="contact-item">📞 电话：13970029843</p>
        </div>
        <p class="footer-note">关注社交媒体获取最新资讯</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const { currentTheme } = storeToRefs(themeStore)
const isDarkMode = computed(() => currentTheme.value === 'dark')
</script>

<style scoped>
.about {
  position: relative;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
}

.about-content {
  position: relative;
  z-index: 10;
  padding: 100px 40px;
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.about-title {
  font-size: 48px;
  font-weight: bold;
  color: var(--text-highlight);
  margin-bottom: 40px;
  letter-spacing: 4px;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.about-text {
  font-size: 20px;
  line-height: 1.8;
  color: var(--text-primary);
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.about-text p {
  margin: 20px 0;
}

/* 背景由 BasicLayout 提供，保持透明 */
.about {
  background: transparent;
}

[data-theme='light'] .about-title {
  color: #0c4a6e;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.5),
               0 0 20px rgba(14, 165, 233, 0.3);
}

[data-theme='light'] .about-text {
  color: #075985;
}

/* 夜间模式特定样式 - 清晰锐利 */
[data-theme='dark'] .about-title {
  color: #ffffff;
  text-shadow: 0 0 30px rgba(59, 130, 246, 0.6),
               0 0 60px rgba(59, 130, 246, 0.3),
               0 2px 4px rgba(0, 0, 0, 0.8);
}

[data-theme='dark'] .about-text {
  color: rgba(226, 232, 240, 0.95);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 副标题样式 */
.about-subtitle {
  font-size: 20px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: 60px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

[data-theme='light'] .about-subtitle {
  color: #0c4a6e;
  opacity: 0.9;
}

[data-theme='dark'] .about-subtitle {
  color: rgba(203, 213, 225, 0.9);
}

/* 内容区块样式 */
.about-sections {
  margin-top: 60px;
}

.about-section {
  margin-bottom: 60px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-highlight);
  margin-bottom: 20px;
  text-align: center;
}

[data-theme='light'] .section-title {
  color: #0369a1;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

[data-theme='dark'] .section-title {
  color: rgba(147, 197, 253, 1);
  text-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
}

.section-text {
  font-size: 18px;
  line-height: 1.8;
  color: var(--text-primary);
  margin-bottom: 16px;
  text-align: left;
}

[data-theme='light'] .section-text {
  color: #0c4a6e;
}

[data-theme='dark'] .section-text {
  color: rgba(203, 213, 225, 0.95);
}

.section-text strong {
  font-weight: 600;
  color: var(--primary-color);
}

/* 特色功能网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-top: 30px;
}

.feature-item {
  padding: 30px;
  border-radius: 16px;
  transition: all 0.3s ease;
  text-align: center;
}

[data-theme='light'] .feature-item {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

[data-theme='dark'] .feature-item {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(100, 150, 200, 0.3);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.feature-item:hover {
  transform: translateY(-5px);
}

[data-theme='light'] .feature-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

[data-theme='dark'] .feature-item:hover {
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.2);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-highlight);
}

[data-theme='light'] .feature-title {
  color: #0369a1;
}

[data-theme='dark'] .feature-title {
  color: rgba(147, 197, 253, 1);
}

.feature-desc {
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-secondary);
}

[data-theme='light'] .feature-desc {
  color: #0c4a6e;
  opacity: 0.8;
}

[data-theme='dark'] .feature-desc {
  color: rgba(203, 213, 225, 0.8);
}

/* 创始人卡片 */
.founder-section {
  margin-top: 80px;
}

.founder-card {
  padding: 40px;
  border-radius: 20px;
  max-width: 700px;
  margin: 0 auto;
}

[data-theme='light'] .founder-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(186, 230, 253, 0.5) 100%);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

[data-theme='dark'] .founder-card {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(100, 150, 200, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.founder-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-highlight);
}

[data-theme='light'] .founder-name {
  color: #0369a1;
}

[data-theme='dark'] .founder-name {
  color: rgba(147, 197, 253, 1);
}

.founder-title {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

[data-theme='light'] .founder-title {
  color: #0c4a6e;
  opacity: 0.8;
}

[data-theme='dark'] .founder-title {
  color: rgba(148, 163, 184, 0.9);
}

.founder-quote {
  font-size: 18px;
  line-height: 1.8;
  font-style: italic;
  color: var(--text-primary);
  padding: 20px;
  border-left: 4px solid var(--primary-color);
  margin-top: 20px;
}

[data-theme='light'] .founder-quote {
  color: #0c4a6e;
  background: rgba(255, 255, 255, 0.5);
}

[data-theme='dark'] .founder-quote {
  color: rgba(203, 213, 225, 0.95);
  background: rgba(15, 23, 42, 0.5);
}

/* Footer 样式 */
.about-footer {
  margin-top: 100px;
  padding-top: 40px;
  border-top: 2px solid;
  text-align: center;
}

[data-theme='light'] .about-footer {
  border-top-color: rgba(14, 165, 233, 0.3);
}

[data-theme='dark'] .about-footer {
  border-top-color: rgba(59, 130, 246, 0.3);
}

.footer-copyright {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 20px;
}

[data-theme='light'] .footer-copyright {
  color: #0369a1;
}

[data-theme='dark'] .footer-copyright {
  color: rgba(147, 197, 253, 1);
}

.contact-info {
  margin: 20px 0;
}

.contact-item {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 8px 0;
}

[data-theme='light'] .contact-item {
  color: #0c4a6e;
}

[data-theme='dark'] .contact-item {
  color: rgba(203, 213, 225, 0.9);
}

.footer-note {
  font-size: 14px;
  color: var(--text-tertiary);
  margin-top: 20px;
  opacity: 0.8;
}

[data-theme='light'] .footer-note {
  color: #0c4a6e;
}

[data-theme='dark'] .footer-note {
  color: rgba(148, 163, 184, 0.8);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-title {
    font-size: 32px;
  }
  
  .about-subtitle {
    font-size: 16px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .about-content {
    padding: 60px 20px;
  }
}
</style>
