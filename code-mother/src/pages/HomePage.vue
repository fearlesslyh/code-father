<script setup lang="ts">
import { useLoginUserStore } from '@/stores/loginUser'
import { storeToRefs } from 'pinia'
import { RocketOutlined, ThunderboltOutlined, SafetyOutlined, ApiOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const { userState } = storeToRefs(loginUserStore)
</script>

<template>
  <div class="home-page app-content tech-bg tech-particles">
    <div class="home-container">
      <!-- 欢迎区域 -->
      <section class="welcome-section">
        <div class="welcome-card tech-card">
          <div class="welcome-icon">👋</div>
          <h1 class="welcome-title">
            欢迎回来，{{ userState.user?.nickname || '访客' }}！
          </h1>
          <p class="welcome-subtitle">
            凌犀零代码平台助您快速构建应用，开启高效开发之旅
          </p>
        </div>
      </section>

      <!-- 功能特性区域 -->
      <section class="features-section">
        <h2 class="section-title">平台特性</h2>
        <div class="features-grid">
          <div class="feature-card tech-card">
            <div class="feature-icon">
              <RocketOutlined :style="{ fontSize: '32px', color: '#78dbff' }" />
            </div>
            <h3 class="feature-title">快速开发</h3>
            <p class="feature-desc">
              零代码拖拽式界面，几分钟内构建完整应用
            </p>
          </div>

          <div class="feature-card tech-card">
            <div class="feature-icon">
              <ThunderboltOutlined :style="{ fontSize: '32px', color: '#ffeb3b' }" />
            </div>
            <h3 class="feature-title">高性能</h3>
            <p class="feature-desc">
              优化的代码生成引擎，确保应用流畅运行
            </p>
          </div>

          <div class="feature-card tech-card">
            <div class="feature-icon">
              <SafetyOutlined :style="{ fontSize: '32px', color: '#4caf50' }" />
            </div>
            <h3 class="feature-title">安全可靠</h3>
            <p class="feature-desc">
              企业级安全防护，数据加密存储传输
            </p>
          </div>

          <div class="feature-card tech-card">
            <div class="feature-icon">
              <ApiOutlined :style="{ fontSize: '32px', color: '#ff9800' }" />
            </div>
            <h3 class="feature-title">灵活扩展</h3>
            <p class="feature-desc">
              支持自定义组件和API集成，满足各类需求
            </p>
          </div>
        </div>
      </section>

      <!-- 快速操作区域 -->
      <section class="quick-actions-section" v-if="userState.user?.userRole === 'admin'">
        <h2 class="section-title">快速操作</h2>
        <div class="actions-grid">
          <a-button 
            type="primary" 
            size="large" 
            class="tech-button action-btn"
            @click="() => $router.push('/admin/userManager')"
          >
            用户管理
          </a-button>
          <a-button 
            size="large" 
            class="action-btn"
          >
            数据分析
          </a-button>
          <a-button 
            size="large" 
            class="action-btn"
          >
            系统设置
          </a-button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: var(--content-min-height);
  padding: var(--spacing-xl);
  position: relative;
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: var(--spacing-2xl);
}

.welcome-card {
  text-align: center;
  padding: var(--spacing-2xl);
  animation: fadeInUp 0.6s ease-out;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-lg);
  animation: pulse 2s infinite;
}

.welcome-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-md) 0;
  text-shadow: 0 0 25px rgba(120, 219, 255, 0.6);
}

.welcome-subtitle {
  font-size: 18px;
  color: var(--text-tertiary);
  margin: 0;
}

/* 功能特性区域 */
.features-section {
  margin-bottom: var(--spacing-2xl);
}

.section-title {
  font-size: 28px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xl) 0;
  text-align: center;
  text-shadow: 0 0 20px rgba(120, 219, 255, 0.5);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
}

.feature-card {
  padding: var(--spacing-xl);
  text-align: center;
  transition: all var(--transition-normal);
  cursor: default;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
}

.feature-icon {
  margin-bottom: var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-title {
  font-size: 20px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-md) 0;
}

.feature-desc {
  font-size: 14px;
  color: var(--text-tertiary);
  line-height: 1.6;
  margin: 0;
}

/* 快速操作区域 */
.quick-actions-section {
  margin-bottom: var(--spacing-xl);
}

.actions-grid {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 150px;
  height: 48px;
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: var(--spacing-lg);
  }

  .welcome-title {
    font-size: 28px;
  }

  .welcome-subtitle {
    font-size: 16px;
  }

  .section-title {
    font-size: 24px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .home-page {
    padding: var(--spacing-md);
  }

  .welcome-card {
    padding: var(--spacing-xl);
  }

  .welcome-icon {
    font-size: 48px;
  }

  .welcome-title {
    font-size: 24px;
  }

  .welcome-subtitle {
    font-size: 14px;
  }

  .feature-card {
    padding: var(--spacing-lg);
  }
}

/* 日间模式调整 */
[data-theme='light'] .welcome-title,
[data-theme='light'] .section-title,
[data-theme='light'] .feature-title {
  text-shadow: none;
}

[data-theme='light'] .feature-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
</style>
