<template>
  <div ref="containerRef" class="blackhole-container">
    <!-- HUD 信息框 - 星星点击 -->
    <div
      v-if="starHUD.visible"
      class="star-hud"
      :style="{
        left: starHUD.x + 'px',
        top: starHUD.y + 'px'
      }"
    >
      <div class="hud-header">恒星数据</div>
      <div class="hud-content">
        <div class="hud-line">
          <span class="hud-label">编号:</span>
          <span class="hud-value">{{ starHUD.data.id }}</span>
        </div>
        <div class="hud-line">
          <span class="hud-label">亮度:</span>
          <span class="hud-value">{{ starHUD.data.brightness }}</span>
        </div>
        <div class="hud-line">
          <span class="hud-label">温度:</span>
          <span class="hud-value">{{ starHUD.data.temperature }}</span>
        </div>
        <div class="hud-line">
          <span class="hud-label">距离:</span>
          <span class="hud-value">{{ starHUD.data.distance }}</span>
        </div>
      </div>
    </div>

    <!-- 主 HUD 界面 - 黑洞内部 -->
    <div v-if="showMainHUD" class="main-hud">
      <div class="hud-frame">
        <div class="hud-corner top-left"></div>
        <div class="hud-corner top-right"></div>
        <div class="hud-corner bottom-left"></div>
        <div class="hud-corner bottom-right"></div>
        
        <div class="hud-title">BLACK HOLE TELEMETRY</div>
        
        <div class="hud-data-grid">
          <div class="hud-data-item">
            <div class="data-label">相机距离</div>
            <div class="data-value">{{ hudData.cameraDistance }} AU</div>
            <div class="data-bar">
              <div class="data-bar-fill" :style="{ width: hudData.distancePercent + '%' }"></div>
            </div>
          </div>
          
          <div class="hud-data-item">
            <div class="data-label">扭曲强度</div>
            <div class="data-value">{{ hudData.distortion }}x</div>
            <div class="data-bar">
              <div class="data-bar-fill distortion" :style="{ width: hudData.distortionPercent + '%' }"></div>
            </div>
          </div>
          
          <div class="hud-data-item">
            <div class="data-label">模拟重力</div>
            <div class="data-value">{{ hudData.gravity }} G</div>
            <div class="data-bar">
              <div class="data-bar-fill gravity" :style="{ width: hudData.gravityPercent + '%' }"></div>
            </div>
          </div>
          
          <div class="hud-data-item">
            <div class="data-label">时间膨胀</div>
            <div class="data-value">{{ hudData.timeDilation }}x</div>
            <div class="data-bar">
              <div class="data-bar-fill time" :style="{ width: hudData.timeDilationPercent + '%' }"></div>
            </div>
          </div>
        </div>
        
        <div class="hud-status">
          <div class="status-indicator" :class="hudData.status"></div>
          <div class="status-text">{{ hudData.statusText }}</div>
        </div>
        
        <div class="scanlines"></div>
      </div>
    </div>
    
    <!-- 提示信息 -->
    <div class="controls-hint">
      <div v-if="!insideBlackHole">滚轮缩放 | 点击星星查看信息</div>
      <div v-else>点击屏幕开启 HUD | ESC 退出</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

// 导入 shader 代码
import blackHoleVertShader from '@/shaders/blackHole.vert.glsl?raw'
import blackHoleFra from '@/shaders/blackHole.frag.glsl?raw'
import accretionDiskVertShader from '@/shaders/accretionDisk.vert.glsl?raw'
import accretionDiskFragShader from '@/shaders/accretionDisk.frag.glsl?raw'
import starfieldVertShader from '@/shaders/starfield.vert.glsl?raw'
import starfieldFragShader from '@/shaders/starfield.frag.glsl?raw'
import blackHoleInteriorFragShader from '@/shaders/blackHoleInterior.frag.glsl?raw'

const containerRef = ref<HTMLDivElement | null>(null)

// 状态
const insideBlackHole = ref(false)
const showMainHUD = ref(false)
const starHUD = ref({
  visible: false,
  x: 0,
  y: 0,
  data: {
    id: 'STAR-0000',
    brightness: '0.0 Lux',
    temperature: '0 K',
    distance: '0 ly'
  }
})

const hudData = ref({
  cameraDistance: '0.00',
  distancePercent: 0,
  distortion: '0.00',
  distortionPercent: 0,
  gravity: '0.00',
  gravityPercent: 0,
  timeDilation: '1.00',
  timeDilationPercent: 0,
  status: 'normal',
  statusText: 'NOMINAL'
})

// Three.js 变量
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let animationId: number

// 场景对象
let blackHoleMesh: THREE.Mesh
let accretionDiskMesh: THREE.Mesh
let starfield: THREE.Points
let interiorMesh: THREE.Mesh

// 控制变量
let targetZoom = 10
let currentZoom = 10
const minZoom = 2
const maxZoom = 20
const zoomSpeed = 0.1
const eventHorizonThreshold = 3

// Raycaster for star picking
let raycaster: THREE.Raycaster
let mouse: THREE.Vector2
let selectedStar: number | null = null

onMounted(() => {
  if (containerRef.value) {
    initScene()
    animate()
    setupEventListeners()
  }
})

onUnmounted(() => {
  cleanup()
})

function initScene() {
  // 创建场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x000510)

  // 创建相机
  camera = new THREE.PerspectiveCamera(
    45,
    containerRef.value!.clientWidth / containerRef.value!.clientHeight,
    0.1,
    1000
  )
  camera.position.z = currentZoom

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(containerRef.value!.clientWidth, containerRef.value!.clientHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  containerRef.value!.appendChild(renderer.domElement)

  // 创建星空背景
  createStarfield()

  // 创建黑洞
  createBlackHole()

  // 创建吸积盘
  createAccretionDisk()

  // 创建黑洞内部场景（初始隐藏）
  createBlackHoleInterior()

  // 初始化 Raycaster
  raycaster = new THREE.Raycaster()
  raycaster.params.Points!.threshold = 0.1
  mouse = new THREE.Vector2()
}

function createStarfield() {
  const starCount = 15000
  const geometry = new THREE.BufferGeometry()
  
  const positions = new Float32Array(starCount * 3)
  const colors = new Float32Array(starCount * 3)
  const sizes = new Float32Array(starCount)
  
  for (let i = 0; i < starCount; i++) {
    const i3 = i * 3
    
    // 球形分布
    const radius = 50 + Math.random() * 100
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)
    
    positions[i3] = radius * Math.sin(phi) * Math.cos(theta)
    positions[i3 + 1] = radius * Math.sin(phi) * Math.sin(theta)
    positions[i3 + 2] = radius * Math.cos(phi)
    
    // 星星颜色（模拟不同温度）
    const temp = Math.random()
    if (temp < 0.3) {
      // 蓝色 - 热星
      colors[i3] = 0.7 + Math.random() * 0.3
      colors[i3 + 1] = 0.8 + Math.random() * 0.2
      colors[i3 + 2] = 1.0
    } else if (temp < 0.7) {
      // 白色 - 中等温度
      colors[i3] = 0.9 + Math.random() * 0.1
      colors[i3 + 1] = 0.9 + Math.random() * 0.1
      colors[i3 + 2] = 0.8 + Math.random() * 0.2
    } else {
      // 橙红色 - 冷星
      colors[i3] = 1.0
      colors[i3 + 1] = 0.6 + Math.random() * 0.3
      colors[i3 + 2] = 0.3 + Math.random() * 0.3
    }
    
    sizes[i] = Math.random() * 2 + 0.5
  }
  
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('customColor', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))
  
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uBrightness: { value: 1.0 }
    },
    vertexShader: starfieldVertShader,
    fragmentShader: starfieldFragShader,
    transparent: true,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  })
  
  starfield = new THREE.Points(geometry, material)
  scene.add(starfield)
}

function createBlackHole() {
  // 创建星空纹理用于引力透镜
  const starfieldTexture = createStarfieldTexture()
  
  const geometry = new THREE.PlaneGeometry(8, 8, 128, 128)
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uResolution: { value: new THREE.Vector2(512, 512) },
      uDistortion: { value: 1.5 },
      uStarfield: { value: starfieldTexture }
    },
    vertexShader: blackHoleVertShader,
    fragmentShader: blackHoleFra,
    transparent: false
  })
  
  blackHoleMesh = new THREE.Mesh(geometry, material)
  blackHoleMesh.position.z = -2
  scene.add(blackHoleMesh)
}

function createStarfieldTexture(): THREE.Texture {
  const size = 512
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')!
  
  // 深色宇宙背景
  ctx.fillStyle = '#000510'
  ctx.fillRect(0, 0, size, size)
  
  // 绘制星星
  for (let i = 0; i < 2000; i++) {
    const x = Math.random() * size
    const y = Math.random() * size
    const radius = Math.random() * 1.5
    const brightness = Math.random()
    
    ctx.fillStyle = `rgba(255, 255, 255, ${brightness})`
    ctx.beginPath()
    ctx.arc(x, y, radius, 0, Math.PI * 2)
    ctx.fill()
  }
  
  const texture = new THREE.CanvasTexture(canvas)
  texture.wrapS = THREE.RepeatWrapping
  texture.wrapT = THREE.RepeatWrapping
  return texture
}

function createAccretionDisk() {
  const geometry = new THREE.RingGeometry(1.5, 4, 128, 32)
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uRotationSpeed: { value: 0.5 },
      uColorHot: { value: new THREE.Color(1.0, 0.9, 0.7) },
      uColorCool: { value: new THREE.Color(1.0, 0.5, 0.2) }
    },
    vertexShader: accretionDiskVertShader,
    fragmentShader: accretionDiskFragShader,
    transparent: true,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })
  
  accretionDiskMesh = new THREE.Mesh(geometry, material)
  accretionDiskMesh.rotation.x = Math.PI * 0.25
  scene.add(accretionDiskMesh)
}

function createBlackHoleInterior() {
  const geometry = new THREE.PlaneGeometry(20, 20)
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uResolution: { value: new THREE.Vector2(window.innerWidth, window.innerHeight) },
      uDistortionStrength: { value: 1.0 },
      uCameraPosition: { value: new THREE.Vector3(0, 0, 5) }
    },
    vertexShader: blackHoleVertShader, // 使用简单的顶点着色器
    fragmentShader: blackHoleInteriorFragShader,
    transparent: false
  })
  
  interiorMesh = new THREE.Mesh(geometry, material)
  interiorMesh.position.z = -2
  interiorMesh.visible = false
  scene.add(interiorMesh)
}

function setupEventListeners() {
  // 滚轮缩放
  containerRef.value!.addEventListener('wheel', onWheel, { passive: false })
  
  // 点击事件
  containerRef.value!.addEventListener('click', onClick)
  containerRef.value!.addEventListener('mousemove', onMouseMove)
  
  // 键盘事件
  window.addEventListener('keydown', onKeyDown)
  
  // 窗口大小改变
  window.addEventListener('resize', onResize)
}

function onWheel(event: WheelEvent) {
  event.preventDefault()
  
  if (insideBlackHole.value) return
  
  // 滚轮控制缩放
  const delta = event.deltaY * 0.01
  targetZoom = THREE.MathUtils.clamp(targetZoom + delta, minZoom, maxZoom)
}

function onMouseMove(event: MouseEvent) {
  if (insideBlackHole.value) return
  
  const rect = containerRef.value!.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
}

function onClick(event: MouseEvent) {
  if (insideBlackHole.value) {
    // 在黑洞内部，点击切换 HUD
    showMainHUD.value = !showMainHUD.value
    return
  }
  
  // 射线检测星星
  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObject(starfield)
  
  if (intersects.length > 0) {
    const intersection = intersects[0]
    if (intersection && intersection.index !== undefined) {
      selectedStar = intersection.index
      
      // 显示星星 HUD
      const rect = containerRef.value!.getBoundingClientRect()
      starHUD.value = {
        visible: true,
        x: event.clientX - rect.left + 20,
        y: event.clientY - rect.top + 20,
        data: generateStarData(selectedStar)
      }
      
      // 星星亮度提升动画
      animateStarBrightness(selectedStar)
      
      // 3秒后自动隐藏
      setTimeout(() => {
        starHUD.value.visible = false
        selectedStar = null
      }, 3000)
    }
  } else {
    starHUD.value.visible = false
    selectedStar = null
  }
}

function onKeyDown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    if (insideBlackHole.value) {
      exitBlackHole()
    }
    showMainHUD.value = false
  }
}

function onResize() {
  if (!containerRef.value) return
  
  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight
  
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  
  renderer.setSize(width, height)
}

function generateStarData(index: number) {
  const random = (seed: number) => {
    const x = Math.sin(seed) * 10000
    return x - Math.floor(x)
  }
  
  return {
    id: `STAR-${String(index).padStart(4, '0')}`,
    brightness: `${(random(index) * 100).toFixed(2)} Lux`,
    temperature: `${Math.floor(random(index + 1000) * 20000 + 3000)} K`,
    distance: `${(random(index + 2000) * 1000).toFixed(2)} ly`
  }
}

function animateStarBrightness(index: number) {
  // 临时提升星星亮度
  const material = starfield.material as THREE.ShaderMaterial
  if (material.uniforms && material.uniforms.uBrightness) {
    const originalBrightness = material.uniforms.uBrightness.value
    
    material.uniforms.uBrightness.value = 2.0
    
    setTimeout(() => {
      if (material.uniforms && material.uniforms.uBrightness) {
        material.uniforms.uBrightness.value = originalBrightness
      }
    }, 500)
  }
}

function checkEventHorizon() {
  if (currentZoom <= eventHorizonThreshold && !insideBlackHole.value) {
    enterBlackHole()
  } else if (currentZoom > eventHorizonThreshold && insideBlackHole.value) {
    exitBlackHole()
  }
}

function enterBlackHole() {
  insideBlackHole.value = true
  
  // 隐藏外部场景
  blackHoleMesh.visible = false
  accretionDiskMesh.visible = false
  starfield.visible = false
  
  // 显示内部场景
  interiorMesh.visible = true
  
  // 相机动画进入
  targetZoom = 1
  
  // 更新 HUD 数据
  updateHUDData()
}

function exitBlackHole() {
  insideBlackHole.value = false
  showMainHUD.value = false
  
  // 显示外部场景
  blackHoleMesh.visible = true
  accretionDiskMesh.visible = true
  starfield.visible = true
  
  // 隐藏内部场景
  interiorMesh.visible = false
  
  targetZoom = eventHorizonThreshold + 0.5
}

function updateHUDData() {
  const distance = currentZoom
  const maxDistance = 20
  
  hudData.value = {
    cameraDistance: distance.toFixed(2),
    distancePercent: Math.min((1 - distance / maxDistance) * 100, 100),
    distortion: (3 - distance).toFixed(2),
    distortionPercent: Math.min((3 - distance) / 3 * 100, 100),
    gravity: (Math.pow(10, (maxDistance - distance) / 5)).toFixed(2),
    gravityPercent: Math.min((maxDistance - distance) / maxDistance * 100, 100),
    timeDilation: (1 + (maxDistance - distance) * 0.5).toFixed(2),
    timeDilationPercent: Math.min((maxDistance - distance) / maxDistance * 100, 100),
    status: distance < 2 ? 'critical' : distance < 5 ? 'warning' : 'normal',
    statusText: distance < 2 ? 'CRITICAL - EVENT HORIZON' : distance < 5 ? 'WARNING - APPROACHING' : 'NOMINAL'
  }
}

function animate() {
  animationId = requestAnimationFrame(animate)
  
  const time = performance.now() * 0.001
  
  // 平滑缩放
  currentZoom += (targetZoom - currentZoom) * zoomSpeed
  camera.position.z = currentZoom
  
  // 检查是否进入事件视界
  checkEventHorizon()
  
  if (!insideBlackHole.value) {
    // 外部场景动画
    
    // 更新黑洞 shader
    if (blackHoleMesh.material instanceof THREE.ShaderMaterial && blackHoleMesh.material.uniforms.uTime) {
      blackHoleMesh.material.uniforms.uTime.value = time
    }
    
    // 更新吸积盘
    if (accretionDiskMesh.material instanceof THREE.ShaderMaterial && accretionDiskMesh.material.uniforms.uTime) {
      accretionDiskMesh.material.uniforms.uTime.value = time
    }
    accretionDiskMesh.rotation.z = time * 0.1
    
    // 更新星空
    if (starfield.material instanceof THREE.ShaderMaterial && starfield.material.uniforms.uTime) {
      starfield.material.uniforms.uTime.value = time
    }
  } else {
    // 内部场景动画
    if (interiorMesh.material instanceof THREE.ShaderMaterial) {
      if (interiorMesh.material.uniforms.uTime) {
        interiorMesh.material.uniforms.uTime.value = time
      }
      if (interiorMesh.material.uniforms.uDistortionStrength) {
        interiorMesh.material.uniforms.uDistortionStrength.value = 1.5 + Math.sin(time * 0.5) * 0.5
      }
      if (interiorMesh.material.uniforms.uCameraPosition) {
        interiorMesh.material.uniforms.uCameraPosition.value.copy(camera.position)
      }
    }
    
    // 更新 HUD 数据
    if (showMainHUD.value) {
      updateHUDData()
    }
  }
  
  renderer.render(scene, camera)
}

function cleanup() {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  if (containerRef.value && renderer) {
    containerRef.value.removeChild(renderer.domElement)
  }
  
  // 清理事件监听
  window.removeEventListener('resize', onResize)
  window.removeEventListener('keydown', onKeyDown)
  
  // 清理 Three.js 资源
  scene?.traverse((object) => {
    if (object instanceof THREE.Mesh) {
      object.geometry?.dispose()
      if (object.material instanceof THREE.Material) {
        object.material.dispose()
      }
    }
  })
  
  renderer?.dispose()
}
</script>

<style scoped>
.blackhole-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #000510;
}

/* 星星 HUD */
.star-hud {
  position: absolute;
  background: rgba(0, 100, 255, 0.15);
  border: 2px solid rgba(100, 180, 255, 0.8);
  border-radius: 8px;
  padding: 16px;
  min-width: 220px;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(100, 180, 255, 0.5),
              inset 0 0 20px rgba(100, 180, 255, 0.1);
  pointer-events: none;
  z-index: 100;
  animation: hudFadeIn 0.3s ease-out;
}

@keyframes hudFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.hud-header {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  font-weight: bold;
  color: #64b4ff;
  text-transform: uppercase;
  margin-bottom: 12px;
  text-shadow: 0 0 10px rgba(100, 180, 255, 0.8);
  letter-spacing: 2px;
}

.hud-content {
  font-family: 'Courier New', monospace;
}

.hud-line {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 12px;
}

.hud-label {
  color: rgba(180, 220, 255, 0.8);
  margin-right: 12px;
}

.hud-value {
  color: #ffffff;
  font-weight: bold;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
}

/* 主 HUD 界面 */
.main-hud {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 200;
  animation: hudSlideIn 0.5s ease-out;
}

@keyframes hudSlideIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

.hud-frame {
  position: relative;
  background: rgba(20, 15, 10, 0.85);
  border: 3px solid rgba(200, 150, 80, 0.8);
  padding: 40px 60px;
  min-width: 500px;
  box-shadow: 0 0 40px rgba(200, 150, 80, 0.6),
              inset 0 0 30px rgba(200, 150, 80, 0.1);
}

.hud-corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 200, 100, 1);
}

.hud-corner.top-left {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.hud-corner.top-right {
  top: -3px;
  right: -3px;
  border-left: none;
  border-bottom: none;
}

.hud-corner.bottom-left {
  bottom: -3px;
  left: -3px;
  border-right: none;
  border-top: none;
}

.hud-corner.bottom-right {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
}

.hud-title {
  font-family: 'Courier New', monospace;
  font-size: 24px;
  font-weight: bold;
  color: #ffc864;
  text-align: center;
  margin-bottom: 30px;
  text-shadow: 0 0 15px rgba(255, 200, 100, 0.8);
  letter-spacing: 4px;
}

.hud-data-grid {
  display: grid;
  gap: 20px;
}

.hud-data-item {
  margin-bottom: 15px;
}

.data-label {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: rgba(255, 200, 100, 0.9);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.data-value {
  font-family: 'Courier New', monospace;
  font-size: 20px;
  color: #ffffff;
  font-weight: bold;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.data-bar {
  width: 100%;
  height: 8px;
  background: rgba(50, 40, 30, 0.8);
  border: 1px solid rgba(200, 150, 80, 0.5);
  overflow: hidden;
  position: relative;
}

.data-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, rgba(255, 200, 100, 0.8), rgba(255, 150, 50, 1));
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(255, 200, 100, 0.8);
}

.data-bar-fill.distortion {
  background: linear-gradient(90deg, rgba(255, 100, 100, 0.8), rgba(255, 50, 50, 1));
  box-shadow: 0 0 10px rgba(255, 100, 100, 0.8);
}

.data-bar-fill.gravity {
  background: linear-gradient(90deg, rgba(150, 100, 255, 0.8), rgba(100, 50, 255, 1));
  box-shadow: 0 0 10px rgba(150, 100, 255, 0.8);
}

.data-bar-fill.time {
  background: linear-gradient(90deg, rgba(100, 255, 200, 0.8), rgba(50, 255, 150, 1));
  box-shadow: 0 0 10px rgba(100, 255, 200, 0.8);
}

.hud-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid rgba(200, 150, 80, 0.5);
}

.status-indicator {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 12px;
  animation: statusPulse 2s ease-in-out infinite;
}

.status-indicator.normal {
  background: #00ff00;
  box-shadow: 0 0 15px #00ff00;
}

.status-indicator.warning {
  background: #ffff00;
  box-shadow: 0 0 15px #ffff00;
}

.status-indicator.critical {
  background: #ff0000;
  box-shadow: 0 0 15px #ff0000;
}

@keyframes statusPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.status-text {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  color: #ffc864;
  font-weight: bold;
  letter-spacing: 2px;
}

/* 扫描线效果 */
.scanlines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(0, 0, 0, 0.3) 2px,
    rgba(0, 0, 0, 0.3) 4px
  );
  pointer-events: none;
  opacity: 0.3;
  animation: scanlineMove 10s linear infinite;
}

@keyframes scanlineMove {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(20px);
  }
}

/* 控制提示 */
.controls-hint {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  backdrop-filter: blur(5px);
  z-index: 50;
  pointer-events: none;
}
</style>

