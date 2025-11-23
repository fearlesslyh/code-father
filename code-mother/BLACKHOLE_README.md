# 《星际穿越》风格黑洞可视化

这是一个基于 Three.js 和 Vue 3 构建的交互式黑洞可视化项目，灵感来自电影《星际穿越》。

## 功能特性

### Task 1: 基础黑洞 + 宇宙背景
- ✅ Kerr 黑洞引力透镜效果（光线弯曲）
- ✅ 多层旋转吸积盘（湍流噪声 + 温度梯度）
- ✅ 高质量星空背景（15000+ 星点，不同颜色温度）
- ✅ 禁用旋转控制，仅支持滚轮缩放
- ✅ 平滑阻尼缩放效果

### Task 2: 星空交互
- ✅ 点击星星触发交互效果
- ✅ 星点亮度提升动画
- ✅ 航空风格 HUD 信息框
- ✅ 显示星体伪数据（编号、亮度、温度、距离）
- ✅ 跟随鼠标的半透明蓝色 HUD
- ✅ 优化的 Raycaster 射线检测

### Task 3: 黑洞穿越
- ✅ 自动检测事件视界（相机距离 < 3）
- ✅ 平滑过渡动画
- ✅ 黑洞内部：无限折叠反射效果
- ✅ 色散效果（chromatic aberration）
- ✅ 旋涡扭曲（swirl distortion）
- ✅ 棕黄色复古 HUD 界面
- ✅ 实时显示：相机距离、扭曲强度、模拟重力、时间膨胀
- ✅ ESC 键退出黑洞

## 项目结构

```
src/
├── components/
│   └── InterstellarBlackHole.vue    # 主黑洞组件
├── pages/
│   └── BlackHolePage.vue            # 黑洞页面
├── shaders/                         # GLSL 着色器文件
│   ├── blackHole.vert.glsl          # 黑洞顶点着色器
│   ├── blackHole.frag.glsl          # 黑洞片段着色器（引力透镜）
│   ├── accretionDisk.vert.glsl      # 吸积盘顶点着色器
│   ├── accretionDisk.frag.glsl      # 吸积盘片段着色器
│   ├── starfield.vert.glsl          # 星空顶点着色器
│   ├── starfield.frag.glsl          # 星空片段着色器
│   └── blackHoleInterior.frag.glsl  # 黑洞内部着色器
└── router/
    └── index.ts                      # 路由配置
```

## 技术实现

### 核心技术栈
- **Vue 3** (Composition API)
- **Three.js** (WebGL 3D 渲染)
- **TypeScript** (类型安全)
- **GLSL** (自定义着色器)

### 关键实现

#### 1. 引力透镜效果
使用 Kerr 黑洞模型，实现光线弯曲：
```glsl
float bend = strength * eventHorizon * eventHorizon / (dist * dist + 0.01);
angle += bend * 0.5 + uTime * 0.1 * smoothstep(...);
```

#### 2. 吸积盘动画
- FBM (Fractional Brownian Motion) 噪声生成湍流
- Keplerian 旋转（越靠近黑洞转速越快）
- 温度梯度着色（内圈更热）

#### 3. 星空粒子系统
- BufferGeometry 高效渲染
- 自定义 vertex attributes（大小、颜色）
- Raycaster 点击检测

#### 4. 黑洞内部
- 3D 噪声生成空间扭曲
- 无限折叠算法创造神秘感
- 色散和旋涡效果结合

## 使用说明

### 基础操作
- **滚轮**: 缩放相机（范围: 2-20 单位）
- **点击星星**: 查看星体数据
- **缩放至阈值**: 自动进入黑洞（距离 < 3）

### 黑洞内部
- **点击屏幕**: 开启/关闭 HUD 界面
- **ESC 键**: 退出黑洞返回外部

### 访问方式
1. 启动开发服务器: `npm run dev`
2. 访问: `http://localhost:5173/blackhole`
3. 或通过导航菜单: "黑洞探索"

## Shader 参数说明

### 黑洞 Shader
- `uTime`: 动画时间
- `uDistortion`: 引力扭曲强度（1.5）
- `uStarfield`: 背景星空纹理

### 吸积盘 Shader
- `uRotationSpeed`: 旋转速度（0.5）
- `uColorHot`: 高温颜色（白黄色）
- `uColorCool`: 低温颜色（橙红色）

### 星空 Shader
- `uBrightness`: 整体亮度（1.0，点击时变为 2.0）

### 内部 Shader
- `uDistortionStrength`: 扭曲强度（1.0-2.0 动态）
- `uCameraPosition`: 相机位置（用于视差）

## 性能优化

- 星空粒子数: 15000（可根据设备调整）
- 黑洞几何体分段: 128x128
- 吸积盘分段: 128 径向 x 32 环向
- 使用 `requestAnimationFrame` 优化动画
- Shader 中避免复杂循环

## 浏览器兼容性

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- 需要 WebGL 2.0 支持

## 已知问题

- 在低端设备上可能出现卡顿（可减少粒子数）
- Safari 上部分 shader 效果可能略有差异

## 未来改进

- [ ] 添加 VR 支持
- [ ] 更真实的广义相对论模拟
- [ ] 多个黑洞系统
- [ ] 音效和背景音乐
- [ ] 移动端触摸优化

## 参考资料

- 电影《星际穿越》视觉效果
- Kip Thorne 的黑洞论文
- Three.js 官方文档
- WebGL Shader 编程指南

---

**作者**: 梁懿豪
**项目**: 凌犀零代码平台
**日期**: 2025-11-23

