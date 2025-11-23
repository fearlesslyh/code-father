<template>
  <div ref="containerRef" class="particle-flow-container" :class="{ 'dark-mode': isDark }">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as THREE from 'three'
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js'
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js'
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js'

// 导入 shader
import particleFlowVertShader from '@/shaders/particleFlow.vert.glsl?raw'
import particleFlowFragShader from '@/shaders/particleFlow.frag.glsl?raw'
import connectionLineVertShader from '@/shaders/connectionLine.vert.glsl?raw'
import connectionLineFragShader from '@/shaders/connectionLine.frag.glsl?raw'

interface Props {
  theme?: 'light' | 'dark'
  particleCount?: number
  enableBloom?: boolean
  enableConnections?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  theme: 'dark',
  particleCount: 50000,
  enableBloom: true,
  enableConnections: true
})

const containerRef = ref<HTMLDivElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)

const isDark = computed(() => props.theme === 'dark')

// Three.js 变量
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let composer: EffectComposer
let particles: THREE.Points
let connectionLines: THREE.LineSegments
let animationId: number

// 性能控制
let actualParticleCount = props.particleCount
let performanceLevel = 1.0

// 鼠标控制
const mouse = ref({ x: 0.5, y: 0.5 })
const mouseVelocity = ref({ x: 0, y: 0 })
let lastMousePos = { x: 0.5, y: 0.5 }
let mouseInfluence = 0
let scrollSpeed = 0

// 粒子数据
let particlePositions: Float32Array
let particleVelocities: Float32Array
let particlePhases: Float32Array
let particleSizes: Float32Array

onMounted(() => {
  if (containerRef.value && canvasRef.value) {
    detectPerformance()
    initScene()
    setupEventListeners()
    animate()
  }
})

onUnmounted(() => {
  cleanup()
})

watch(() => props.theme, () => {
  updateThemeColors()
})

// 性能检测
function detectPerformance() {
  // 基于设备像素比和可用内存估算性能
  const pixelRatio = window.devicePixelRatio || 1
  
  if (pixelRatio > 2) {
    performanceLevel = 0.7 // 高分屏减少粒子
  } else if (pixelRatio > 1.5) {
    performanceLevel = 0.85
  }
  
  // 根据性能调整粒子数
  actualParticleCount = Math.floor(props.particleCount * performanceLevel)
  actualParticleCount = Math.max(30000, Math.min(actualParticleCount, 80000))
}

function initScene() {
  // 创建场景
  scene = new THREE.Scene()
  
  // 背景色
  updateThemeColors()
  
  // 创建相机
  const aspect = containerRef.value!.clientWidth / containerRef.value!.clientHeight
  camera = new THREE.PerspectiveCamera(75, aspect, 0.1, 100)
  camera.position.z = 15
  
  // 创建渲染器
  renderer = new THREE.WebGLRenderer({
    canvas: canvasRef.value!,
    antialias: false, // 性能优化
    alpha: true,
    powerPreference: 'high-performance'
  })
  renderer.setSize(containerRef.value!.clientWidth, containerRef.value!.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2)) // 限制像素比
  
  // 创建粒子系统
  createParticles()
  
  // 创建连接线
  if (props.enableConnections) {
    createConnectionLines()
  }
  
  // 后处理 - Bloom
  if (props.enableBloom) {
    setupPostProcessing()
  }
}

function createParticles() {
  const geometry = new THREE.BufferGeometry()
  
  // 初始化粒子数据
  particlePositions = new Float32Array(actualParticleCount * 3)
  particleVelocities = new Float32Array(actualParticleCount * 3)
  particlePhases = new Float32Array(actualParticleCount)
  particleSizes = new Float32Array(actualParticleCount)
  
  // 在立方体空间中随机分布粒子
  for (let i = 0; i < actualParticleCount; i++) {
    const i3 = i * 3
    
    // 位置 - 更大的空间分布
    particlePositions[i3] = (Math.random() - 0.5) * 30
    particlePositions[i3 + 1] = (Math.random() - 0.5) * 30
    particlePositions[i3 + 2] = (Math.random() - 0.5) * 20
    
    // 速度
    particleVelocities[i3] = (Math.random() - 0.5) * 0.02
    particleVelocities[i3 + 1] = (Math.random() - 0.5) * 0.02
    particleVelocities[i3 + 2] = (Math.random() - 0.5) * 0.01
    
    // 相位
    particlePhases[i] = Math.random() * Math.PI * 2
    
    // 大小 - 变化更自然
    particleSizes[i] = Math.random() * 3 + 0.5
  }
  
  geometry.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3))
  geometry.setAttribute('velocity', new THREE.BufferAttribute(particleVelocities, 3))
  geometry.setAttribute('phase', new THREE.BufferAttribute(particlePhases, 1))
  geometry.setAttribute('size', new THREE.BufferAttribute(particleSizes, 1))
  
  // Shader Material
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uPixelRatio: { value: renderer.getPixelRatio() },
      uMouse: { value: new THREE.Vector2(0.5, 0.5) },
      uMouseInfluence: { value: 0 },
      uBrightness: { value: isDark.value ? 1.0 : 0.6 }
    },
    vertexShader: particleFlowVertShader,
    fragmentShader: particleFlowFragShader,
    transparent: true,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  })
  
  particles = new THREE.Points(geometry, material)
  scene.add(particles)
}

function createConnectionLines() {
  // 创建连接线的几何体
  const maxConnections = Math.min(actualParticleCount * 0.01, 500) // 最多500条线
  const linePositions = new Float32Array(maxConnections * 6) // 每条线2个点
  
  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(linePositions, 3))
  
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uLineColor: { value: new THREE.Color(isDark.value ? 0x4db8ff : 0xffd700) }
    },
    vertexShader: connectionLineVertShader,
    fragmentShader: connectionLineFragShader,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })
  
  connectionLines = new THREE.LineSegments(geometry, material)
  scene.add(connectionLines)
}

function updateConnectionLines() {
  if (!connectionLines || !props.enableConnections) return
  
  const positions = connectionLines.geometry.attributes.position.array as Float32Array
  const maxDistance = 3.0 // 连接距离阈值
  const maxConnections = positions.length / 6
  
  let connectionIndex = 0
  
  // 采样部分粒子进行连接检测（性能优化）
  const sampleRate = Math.max(1, Math.floor(actualParticleCount / 2000))
  
  for (let i = 0; i < actualParticleCount && connectionIndex < maxConnections; i += sampleRate) {
    const i3 = i * 3
    const x1 = particlePositions[i3]
    const y1 = particlePositions[i3 + 1]
    const z1 = particlePositions[i3 + 2]
    
    // 检查附近粒子
    for (let j = i + sampleRate; j < actualParticleCount && connectionIndex < maxConnections; j += sampleRate) {
      const j3 = j * 3
      const x2 = particlePositions[j3]
      const y2 = particlePositions[j3 + 1]
      const z2 = particlePositions[j3 + 2]
      
      const dx = x2 - x1
      const dy = y2 - y1
      const dz = z2 - z1
      const dist = Math.sqrt(dx * dx + dy * dy + dz * dz)
      
      if (dist < maxDistance) {
        // 添加连接线
        const lineIndex = connectionIndex * 6
        positions[lineIndex] = x1
        positions[lineIndex + 1] = y1
        positions[lineIndex + 2] = z1
        positions[lineIndex + 3] = x2
        positions[lineIndex + 4] = y2
        positions[lineIndex + 5] = z2
        connectionIndex++
      }
    }
  }
  
  // 清空剩余的线
  for (let i = connectionIndex * 6; i < positions.length; i++) {
    positions[i] = 0
  }
  
  connectionLines.geometry.attributes.position.needsUpdate = true
}

function setupPostProcessing() {
  composer = new EffectComposer(renderer)
  
  const renderPass = new RenderPass(scene, camera)
  composer.addPass(renderPass)
  
  const bloomPass = new UnrealBloomPass(
    new THREE.Vector2(window.innerWidth, window.innerHeight),
    isDark.value ? 0.8 : 0.4,  // strength
    0.6,  // radius
    0.85  // threshold
  )
  composer.addPass(bloomPass)
}

function setupEventListeners() {
  // 鼠标移动
  window.addEventListener('mousemove', onMouseMove)
  
  // 滚动
  window.addEventListener('wheel', onWheel, { passive: true })
  
  // 窗口大小改变
  window.addEventListener('resize', onResize)
}

function onMouseMove(event: MouseEvent) {
  const newX = event.clientX / window.innerWidth
  const newY = 1.0 - event.clientY / window.innerHeight
  
  // 计算鼠标速度
  mouseVelocity.value.x = newX - lastMousePos.x
  mouseVelocity.value.y = newY - lastMousePos.y
  
  mouse.value.x = newX
  mouse.value.y = newY
  
  lastMousePos = { x: newX, y: newY }
  
  // 增加鼠标影响
  mouseInfluence = Math.min(mouseInfluence + 0.1, 1.0)
}

function onWheel(event: WheelEvent) {
  // 滚动影响粒子流动速度
  scrollSpeed += event.deltaY * 0.0001
  scrollSpeed = THREE.MathUtils.clamp(scrollSpeed, -0.5, 0.5)
}

function onResize() {
  if (!containerRef.value) return
  
  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight
  
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  
  renderer.setSize(width, height)
  if (composer) {
    composer.setSize(width, height)
  }
}

function updateThemeColors() {
  if (scene) {
    scene.background = isDark.value 
      ? new THREE.Color(0x000510)  // 深蓝黑
      : new THREE.Color(0xe0f2fe)  // 浅蓝天空色
  }
  
  if (particles && particles.material instanceof THREE.ShaderMaterial) {
    particles.material.uniforms.uBrightness.value = isDark.value ? 1.0 : 0.6
  }
  
  if (connectionLines && connectionLines.material instanceof THREE.ShaderMaterial) {
    connectionLines.material.uniforms.uLineColor.value = new THREE.Color(
      isDark.value ? 0x4db8ff : 0xffd700
    )
  }
}

let frameCount = 0
function animate() {
  animationId = requestAnimationFrame(animate)
  
  const time = performance.now() * 0.001
  frameCount++
  
  // 更新鼠标影响衰减
  mouseInfluence *= 0.95
  scrollSpeed *= 0.95
  
  // 更新粒子 shader uniforms
  if (particles && particles.material instanceof THREE.ShaderMaterial) {
    particles.material.uniforms.uTime.value = time + scrollSpeed * 10
    particles.material.uniforms.uMouse.value.set(mouse.value.x, mouse.value.y)
    particles.material.uniforms.uMouseInfluence.value = mouseInfluence
  }
  
  // 每隔几帧更新连接线（性能优化）
  if (props.enableConnections && frameCount % 3 === 0) {
    updateConnectionLines()
  }
  
  // 更新连接线时间
  if (connectionLines && connectionLines.material instanceof THREE.ShaderMaterial) {
    connectionLines.material.uniforms.uTime.value = time
  }
  
  // 渲染
  if (composer && props.enableBloom) {
    composer.render()
  } else {
    renderer.render(scene, camera)
  }
}

function cleanup() {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('wheel', onWheel)
  window.removeEventListener('resize', onResize)
  
  // 清理 Three.js 资源
  scene?.traverse((object) => {
    if (object instanceof THREE.Points || object instanceof THREE.LineSegments) {
      object.geometry?.dispose()
      if (object.material instanceof THREE.Material) {
        object.material.dispose()
      }
    }
  })
  
  renderer?.dispose()
  composer?.dispose?.()
}
</script>

<style scoped>
.particle-flow-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: block;
}

/* 日间模式 */
.particle-flow-container:not(.dark-mode) {
  background: linear-gradient(135deg, 
    #e0f2fe 0%, 
    #bae6fd 25%, 
    #7dd3fc 50%, 
    #38bdf8 75%, 
    #0ea5e9 100%);
  background-size: 400% 400%;
  animation: gradientFlow 20s ease infinite;
}

/* 夜间模式 */
.particle-flow-container.dark-mode {
  background: radial-gradient(ellipse at center, 
    #0a1628 0%, 
    #000510 100%);
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 性能优化 - 减少重绘 */
.particle-canvas {
  will-change: auto;
  transform: translateZ(0);
}
</style>

