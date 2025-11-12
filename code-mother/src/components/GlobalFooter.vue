<script setup lang="ts">
import { GithubOutlined } from '@ant-design/icons-vue'
</script>

<template>
  <footer class="app-footer glass-effect">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-left">
          <div class="brand-info">
            <span class="brand-text">凌犀零代码平台</span>
            <span class="brand-separator">|</span>
            <span class="author-text">by 梁懿豪</span>
          </div>
        </div>
        
        <div class="footer-right">
          <div class="social-links">
            <a
              href="https://github.com/fearlesslyh"
              target="_blank"
              rel="noopener noreferrer"
              class="social-link github-link neon-glow"
              title="GitHub"
            >
              <GithubOutlined />
              <span class="link-text">GitHub</span>
            </a>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <div class="copyright">
          <span class="copyright-text">© 2024 凌犀零代码平台. All rights reserved.</span>
        </div>
        <div class="tech-indicator">
          <span class="status-dot status-online"></span>
          <span class="status-text">System Online</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
/* 底部容器样式 */
.app-footer {
  position: relative;
  border-top: 1px solid var(--border-secondary);
  backdrop-filter: blur(20px);
  overflow: hidden;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-md);
}

/* 左侧品牌信息 */
.footer-left {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.brand-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.brand-text {
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  text-shadow: 0 0 10px rgba(120, 219, 255, 0.4);
}

.brand-separator {
  color: var(--text-tertiary);
  font-size: 14px;
}

.author-text {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
}

.tech-info {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.tech-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--spacing-sm);
  background: var(--primary-light);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--primary-color);
  font-weight: var(--font-weight-medium);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all var(--transition-fast);
}

.tech-badge:hover {
  background: rgba(120, 219, 255, 0.2);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

/* 右侧社交链接 */
.footer-right {
  display: flex;
  align-items: center;
}

.social-links {
  display: flex;
  gap: var(--spacing-md);
}

.social-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-glass-light);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 16px;
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.social-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left var(--transition-slow);
}

.social-link:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.social-link:hover::before {
  left: 100%;
}

.github-link:hover {
  color: #78dbff;
  border-color: #78dbff;
  box-shadow: 0 0 20px rgba(120, 219, 255, 0.4);
}

.link-text {
  font-size: 14px;
}

/* 底部版权和状态 */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-secondary);
}

.copyright {
  display: flex;
  align-items: center;
}

.copyright-text {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: var(--font-weight-normal);
}

.tech-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
  animation: pulse 2s infinite;
}

.status-online {
  background: #52c41a;
  box-shadow: 0 0 10px rgba(82, 196, 26, 0.6);
}

.status-text {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: var(--font-weight-medium);
  font-family: 'Courier New', monospace;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 背景装饰效果 */
.app-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    var(--primary-color) 20%, 
    var(--primary-dark) 50%, 
    var(--primary-color) 80%, 
    transparent 100%);
  animation: glowMove 3s linear infinite;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .footer-container {
    padding: var(--spacing-md);
  }
  
  .brand-text {
    font-size: 15px;
  }
  
  .link-text {
    display: none;
  }
  
  .social-link {
    padding: var(--spacing-sm);
  }
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-md);
  }
  
  .footer-left {
    width: 100%;
  }
  
  .footer-right {
    width: 100%;
    justify-content: center;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: center;
    text-align: center;
  }
  
  .tech-info {
    justify-content: center;
  }
  
  .brand-info {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .brand-text {
    font-size: 14px;
  }
  
  .author-text {
    font-size: 13px;
  }
  
  .tech-badge {
    font-size: 10px;
    padding: 1px var(--spacing-xs);
  }
  
  .social-link {
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: 14px;
  }
  
  .copyright-text,
  .status-text {
    font-size: 11px;
  }
}

/* 动画效果 */
@keyframes glowMove {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
</style>
