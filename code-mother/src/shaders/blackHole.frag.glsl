// 黑洞引力透镜片段着色器 - Kerr Black Hole
uniform float uTime;
uniform vec2 uResolution;
uniform float uDistortion;
uniform sampler2D uStarfield;

varying vec2 vUv;
varying vec3 vPosition;

// 黑洞参数
const float blackHoleRadius = 0.3;
const float eventHorizon = 0.35;
const float photonSphere = 0.5;

// 引力透镜扭曲函数
vec2 gravitationalLensing(vec2 uv, vec2 center, float strength) {
  vec2 delta = uv - center;
  float dist = length(delta);
  
  if (dist < eventHorizon) {
    // 事件视界内完全黑暗
    return vec2(-1.0);
  }
  
  // Kerr 黑洞的光线弯曲
  float bend = strength * eventHorizon * eventHorizon / (dist * dist + 0.01);
  float angle = atan(delta.y, delta.x);
  
  // 添加旋转效应（Kerr 黑洞特征）
  angle += bend * 0.5 + uTime * 0.1 * (1.0 - smoothstep(eventHorizon, photonSphere, dist));
  
  vec2 offset = vec2(cos(angle), sin(angle)) * bend;
  return uv + offset * delta;
}

void main() {
  vec2 uv = vUv;
  vec2 center = vec2(0.5);
  
  // 计算到中心的距离
  float dist = length(uv - center);
  
  // 应用引力透镜效果
  vec2 distortedUv = gravitationalLensing(uv, center, uDistortion);
  
  if (distortedUv.x < 0.0) {
    // 事件视界内 - 纯黑
    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    return;
  }
  
  // 采样背景星空（被引力扭曲）
  vec3 color = texture2D(uStarfield, fract(distortedUv)).rgb;
  
  // 在光子球附近添加发光环
  if (dist > eventHorizon && dist < photonSphere + 0.1) {
    float ringIntensity = 1.0 - abs(dist - photonSphere) / 0.1;
    ringIntensity = pow(ringIntensity, 2.0);
    color += vec3(1.0, 0.7, 0.4) * ringIntensity * 0.5;
  }
  
  // 添加事件视界边缘发光
  if (dist > eventHorizon - 0.02 && dist < eventHorizon + 0.05) {
    float glowIntensity = 1.0 - abs(dist - eventHorizon) / 0.05;
    glowIntensity = pow(glowIntensity, 3.0);
    color += vec3(0.3, 0.5, 1.0) * glowIntensity * 0.3;
  }
  
  gl_FragColor = vec4(color, 1.0);
}

