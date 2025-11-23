<template>
  <div 
    ref="containerRef" 
    class="astronaut-helmet-effect"
    :class="{ 'show-hud': showHUD }"
    @click.stop="toggleHUD"
  >
    <canvas ref="canvasRef" class="helmet-canvas"></canvas>
    
    <!-- 护目镜HUD数据展示 -->
    <div class="helmet-hud" v-if="showHUD">
      <div class="hud-overlay">
        <!-- 左上角数据 -->
        <div class="hud-corner hud-top-left">
          <div class="hud-data">
            <div class="hud-label">ALTITUDE</div>
            <div class="hud-value">{{ altitude.toFixed(0) }} KM</div>
          </div>
          <div class="hud-data">
            <div class="hud-label">VELOCITY</div>
            <div class="hud-value">{{ velocity.toFixed(0) }} M/S</div>
          </div>
        </div>
        
        <!-- 右上角数据 -->
        <div class="hud-corner hud-top-right">
          <div class="hud-data">
            <div class="hud-label">GRAVITY</div>
            <div class="hud-value">{{ gravity.toFixed(2) }} G</div>
          </div>
          <div class="hud-data">
            <div class="hud-label">DISTANCE</div>
            <div class="hud-value">{{ distance.toFixed(2) }} AU</div>
          </div>
        </div>
        
        <!-- 底部中央数据 -->
        <div class="hud-corner hud-bottom-center">
          <div class="hud-data">
            <div class="hud-label">BLACK HOLE MASS</div>
            <div class="hud-value">{{ blackHoleMass.toFixed(2) }} × 10³⁰ KG</div>
          </div>
          <div class="hud-data">
            <div class="hud-label">EVENT HORIZON</div>
            <div class="hud-value">{{ eventHorizon.toFixed(2) }} KM</div>
          </div>
        </div>
        
        <!-- 中央十字准星 -->
        <div class="hud-crosshair">
          <div class="crosshair-line crosshair-h"></div>
          <div class="crosshair-line crosshair-v"></div>
          <div class="crosshair-dot"></div>
        </div>
        
        <!-- 扫描线效果 -->
        <div class="hud-scanline"></div>
      </div>
    </div>
    
    <!-- 护目镜边框效果 -->
    <div class="helmet-frame">
      <div class="frame-top"></div>
      <div class="frame-bottom"></div>
      <div class="frame-left"></div>
      <div class="frame-right"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const containerRef = ref<HTMLElement>()
const canvasRef = ref<HTMLCanvasElement>()
const showHUD = ref(false)

// HUD数据
const altitude = ref(0)
const velocity = ref(0)
const gravity = ref(0)
const distance = ref(0)
const blackHoleMass = ref(4.3)
const eventHorizon = ref(12.7)

let animationId: number | null = null
let ctx: CanvasRenderingContext2D | null = null
let mouseX = 0
let mouseY = 0
let targetRotationX = 0
let targetRotationY = 0
let currentRotationX = 0
let currentRotationY = 0
let blackHoleRadius = 0
let isConsumed = false
let consumptionProgress = 0

// 星星数据
const stars: Array<{ x: number; y: number; z: number; size: number }> = []

// 初始化星星
function initStars() {
  stars.length = 0
  for (let i = 0; i < 2000; i++) {
    stars.push({
      x: (Math.random() - 0.5) * 2000,
      y: (Math.random() - 0.5) * 2000,
      z: Math.random() * 2000,
      size: Math.random() * 2 + 0.5
    })
  }
}

// 绘制黑洞
function drawBlackHole() {
  if (!ctx || !canvasRef.value) return
  
  const centerX = canvasRef.value.width / 2
  const centerY = canvasRef.value.height / 2
  
  // 黑洞引力效果 - 扭曲周围空间
  const gradient = ctx.createRadialGradient(centerX, centerY, blackHoleRadius, centerX, centerY, blackHoleRadius * 3)
  gradient.addColorStop(0, 'rgba(0, 0, 0, 1)')
  gradient.addColorStop(0.3, 'rgba(20, 20, 40, 0.8)')
  gradient.addColorStop(0.6, 'rgba(40, 20, 60, 0.4)')
  gradient.addColorStop(1, 'rgba(0, 0, 0, 0)')
  
  // 事件视界
  ctx.fillStyle = gradient
  ctx.beginPath()
  ctx.arc(centerX, centerY, blackHoleRadius * 3, 0, Math.PI * 2)
  ctx.fill()
  
  // 黑洞本体 - 吸积盘效果
  const accretionGradient = ctx.createRadialGradient(centerX, centerY, 0, centerX, centerY, blackHoleRadius)
  accretionGradient.addColorStop(0, '#000000')
  accretionGradient.addColorStop(0.7, '#1a0033')
  accretionGradient.addColorStop(0.9, '#330066')
  accretionGradient.addColorStop(1, '#4d0099')
  
  ctx.fillStyle = accretionGradient
  ctx.beginPath()
  ctx.arc(centerX, centerY, blackHoleRadius, 0, Math.PI * 2)
  ctx.fill()
  
  // 吸积盘光环
  for (let i = 0; i < 3; i++) {
    const ringRadius = blackHoleRadius * (1.2 + i * 0.3)
    const ringGradient = ctx.createRadialGradient(centerX, centerY, ringRadius - 2, centerX, centerY, ringRadius + 2)
    ringGradient.addColorStop(0, `rgba(100, 50, 150, ${0.3 - i * 0.1})`)
    ringGradient.addColorStop(1, 'rgba(0, 0, 0, 0)')
    
    ctx.strokeStyle = ringGradient
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.arc(centerX, centerY, ringRadius, 0, Math.PI * 2)
    ctx.stroke()
  }
  
  // 黑洞中心高光
  ctx.fillStyle = 'rgba(150, 100, 200, 0.3)'
  ctx.beginPath()
  ctx.arc(centerX, centerY, blackHoleRadius * 0.3, 0, Math.PI * 2)
  ctx.fill()
}

// 绘制星星
function drawStars() {
  if (!ctx || !canvasRef.value) return
  
  const centerX = canvasRef.value.width / 2
  const centerY = canvasRef.value.height / 2
  
  stars.forEach(star => {
    // 应用旋转
    const cosX = Math.cos(currentRotationX)
    const sinX = Math.sin(currentRotationX)
    const cosY = Math.cos(currentRotationY)
    const sinY = Math.sin(currentRotationY)
    
    let x = star.x
    let y = star.y * cosX - star.z * sinX
    let z = star.z * cosX + star.y * sinX
    
    const tempX = x
    x = x * cosY - z * sinY
    z = z * cosY + tempX * sinY
    
    // 透视投影
    const perspective = 500
    const scale = perspective / (perspective + z)
    const screenX = centerX + x * scale
    const screenY = centerY + y * scale
    const screenSize = star.size * scale
    
    // 计算到黑洞的距离
    const dx = screenX - centerX
    const dy = screenY - centerY
    const distToBlackHole = Math.sqrt(dx * dx + dy * dy)
    
    // 黑洞引力扭曲效果
    if (distToBlackHole < blackHoleRadius * 5 && ctx) {
      const pullStrength = (1 - distToBlackHole / (blackHoleRadius * 5)) * 0.5
      const angle = Math.atan2(dy, dx)
      const pullX = Math.cos(angle) * pullStrength * 50
      const pullY = Math.sin(angle) * pullStrength * 50
      
      ctx.save()
      ctx.translate(screenX + pullX, screenY + pullY)
      ctx.rotate(angle + Math.PI / 2)
      ctx.scale(1, 1 - pullStrength)
      ctx.restore()
    }
    
    // 绘制星星
    if (ctx && screenX > 0 && screenX < canvasRef.value!.width && 
        screenY > 0 && screenY < canvasRef.value!.height && z > -perspective) {
      const brightness = Math.min(1, (perspective + z) / perspective)
      ctx.fillStyle = `rgba(255, 255, 255, ${brightness})`
      ctx.beginPath()
      ctx.arc(screenX, screenY, screenSize, 0, Math.PI * 2)
      ctx.fill()
    }
    
    // 更新星星位置（向黑洞移动）
    if (!isConsumed) {
      const dx = star.x
      const dy = star.y
      const dz = star.z
      const dist = Math.sqrt(dx * dx + dy * dy + dz * dz)
      
      if (dist > blackHoleRadius * 2) {
        const pull = 0.02
        star.x -= dx * pull / dist
        star.y -= dy * pull / dist
        star.z -= dz * pull / dist
      }
    }
  })
}

// 动画循环
function animate() {
  if (!ctx || !canvasRef.value) return
  
  // 清空画布
  ctx.fillStyle = '#000011'
  ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  
  // 平滑旋转
  currentRotationX += (targetRotationX - currentRotationX) * 0.1
  currentRotationY += (targetRotationY - currentRotationY) * 0.1
  
  // 更新黑洞大小
  if (!isConsumed) {
    blackHoleRadius = Math.min(80, blackHoleRadius + 0.1)
    
    // 检查是否被吞噬
    if (blackHoleRadius >= 80) {
      isConsumed = true
    }
  } else {
    // 被吞噬后的效果 - 视角快速旋转看向宇宙
    consumptionProgress += 0.01
    currentRotationX += 0.02
    currentRotationY += 0.01
    
    // 黑洞逐渐消失，星星重新出现
    blackHoleRadius = Math.max(0, blackHoleRadius - 0.5)
    
    // 重新生成星星（看向宇宙）
    if (consumptionProgress > 0.5 && stars.length < 2000) {
      for (let i = stars.length; i < 2000; i++) {
        stars.push({
          x: (Math.random() - 0.5) * 3000,
          y: (Math.random() - 0.5) * 3000,
          z: Math.random() * 3000 + 1000,
          size: Math.random() * 3 + 1
        })
      }
    }
  }
  
  // 绘制星星
  drawStars()
  
  // 绘制黑洞
  if (blackHoleRadius > 0) {
    drawBlackHole()
  }
  
  // 更新HUD数据
  if (showHUD.value) {
    altitude.value = 1000 - blackHoleRadius * 10
    velocity.value = 5000 + blackHoleRadius * 50
    gravity.value = 1 + blackHoleRadius * 0.1
    distance.value = 0.1 - blackHoleRadius * 0.001
    eventHorizon.value = blackHoleRadius * 0.16
  }
  
  animationId = requestAnimationFrame(animate)
}

// 鼠标移动处理
function handleMouseMove(e: MouseEvent) {
  if (!containerRef.value || !canvasRef.value) return
  
  const rect = containerRef.value.getBoundingClientRect()
  mouseX = e.clientX - rect.left
  mouseY = e.clientY - rect.top
  
  if (!isConsumed) {
    // 鼠标控制视角（被黑洞牵引）
    const centerX = canvasRef.value.width / 2
    const centerY = canvasRef.value.height / 2
    
    // 计算鼠标相对于中心的位置，但被黑洞牵引
    const dx = mouseX - centerX
    const dy = mouseY - centerY
    
    // 黑洞牵引效果 - 视角被拉向黑洞
    const pullX = -dx * 0.3
    const pullY = -dy * 0.3
    
    targetRotationY = (pullX / centerX) * 0.5
    targetRotationX = (pullY / centerY) * 0.5
  }
}

// 切换HUD显示
function toggleHUD() {
  showHUD.value = !showHUD.value
}

// 窗口大小调整
function handleResize() {
  if (!canvasRef.value || !containerRef.value) return
  
  canvasRef.value.width = containerRef.value.clientWidth
  canvasRef.value.height = containerRef.value.clientHeight
}

onMounted(() => {
  if (!canvasRef.value || !containerRef.value) return
  
  ctx = canvasRef.value.getContext('2d')
  if (!ctx) return
  
  handleResize()
  initStars()
  
  // 初始黑洞大小
  blackHoleRadius = 20
  
  // 事件监听 - 监听整个窗口的鼠标移动，不阻止内容交互
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('resize', handleResize)
  
  // 开始动画
  animate()
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.astronaut-helmet-effect {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  cursor: crosshair;
  background: #000011;
}

.helmet-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

/* 护目镜边框 */
.helmet-frame {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.frame-top,
.frame-bottom {
  position: absolute;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to bottom, 
    rgba(100, 150, 200, 0.3) 0%,
    rgba(100, 150, 200, 0.1) 50%,
    transparent 100%);
  border-bottom: 2px solid rgba(100, 150, 200, 0.4);
  box-shadow: 0 0 20px rgba(100, 150, 200, 0.3);
}

.frame-top {
  top: 0;
  border-bottom: 2px solid rgba(100, 150, 200, 0.4);
}

.frame-bottom {
  bottom: 0;
  border-top: 2px solid rgba(100, 150, 200, 0.4);
  border-bottom: none;
  background: linear-gradient(to top, 
    rgba(100, 150, 200, 0.3) 0%,
    rgba(100, 150, 200, 0.1) 50%,
    transparent 100%);
}

.frame-left,
.frame-right {
  position: absolute;
  top: 60px;
  bottom: 60px;
  width: 40px;
  background: linear-gradient(to right, 
    rgba(100, 150, 200, 0.3) 0%,
    rgba(100, 150, 200, 0.1) 50%,
    transparent 100%);
  border-right: 2px solid rgba(100, 150, 200, 0.4);
  box-shadow: 0 0 20px rgba(100, 150, 200, 0.3);
}

.frame-left {
  left: 0;
}

.frame-right {
  right: 0;
  border-left: 2px solid rgba(100, 150, 200, 0.4);
  border-right: none;
  background: linear-gradient(to left, 
    rgba(100, 150, 200, 0.3) 0%,
    rgba(100, 150, 200, 0.1) 50%,
    transparent 100%);
}

/* HUD数据展示 */
.helmet-hud {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 2;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.astronaut-helmet-effect.show-hud .helmet-hud {
  opacity: 1;
}

.hud-overlay {
  position: relative;
  width: 100%;
  height: 100%;
  font-family: 'Courier New', monospace;
}

.hud-corner {
  position: absolute;
  padding: 20px;
}

.hud-top-left {
  top: 80px;
  left: 60px;
}

.hud-top-right {
  top: 80px;
  right: 60px;
  text-align: right;
}

.hud-bottom-center {
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.hud-data {
  margin-bottom: 15px;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.8),
               0 0 20px rgba(0, 255, 255, 0.5);
}

.hud-label {
  font-size: 12px;
  letter-spacing: 2px;
  opacity: 0.8;
  margin-bottom: 5px;
}

.hud-value {
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 1px;
}

/* 十字准星 */
.hud-crosshair {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
}

.crosshair-line {
  position: absolute;
  background: rgba(0, 255, 255, 0.6);
  box-shadow: 0 0 5px rgba(0, 255, 255, 0.8);
}

.crosshair-h {
  top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  transform: translateY(-50%);
}

.crosshair-v {
  left: 50%;
  top: 0;
  width: 2px;
  height: 100%;
  transform: translateX(-50%);
}

.crosshair-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4px;
  height: 4px;
  background: rgba(0, 255, 255, 0.8);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 255, 255, 1);
}

/* 扫描线效果 */
.hud-scanline {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(to right,
    transparent 0%,
    rgba(0, 255, 255, 0.5) 50%,
    transparent 100%);
  animation: scanline 3s linear infinite;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
}

@keyframes scanline {
  0% {
    top: 0;
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0.3;
  }
}
</style>
