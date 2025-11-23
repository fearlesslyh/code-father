// 吸积盘片段着色器 - 多层旋转的炽热气体
uniform float uTime;
uniform float uRotationSpeed;
uniform vec3 uColorHot;
uniform vec3 uColorCool;

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;

// 噪声函数
float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  
  float a = hash(i);
  float b = hash(i + vec2(1.0, 0.0));
  float c = hash(i + vec2(0.0, 1.0));
  float d = hash(i + vec2(1.0, 1.0));
  
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
  float value = 0.0;
  float amplitude = 0.5;
  float frequency = 1.0;
  
  for (int i = 0; i < 6; i++) {
    value += amplitude * noise(p * frequency);
    frequency *= 2.0;
    amplitude *= 0.5;
  }
  
  return value;
}

void main() {
  vec2 center = vec2(0.0);
  vec2 pos = vPosition.xy;
  
  float dist = length(pos);
  float angle = atan(pos.y, pos.x);
  
  // 旋转速度随距离变化（Keplerian rotation）
  float rotationFactor = 1.0 / sqrt(dist + 0.5);
  float rotatedAngle = angle + uTime * uRotationSpeed * rotationFactor;
  
  // 多层噪声创建湍流效果
  vec2 noiseCoord = vec2(rotatedAngle * 3.0, dist * 5.0);
  float turbulence = fbm(noiseCoord + uTime * 0.3);
  
  // 温度分布（越靠近黑洞越热）
  float temperature = 1.0 / (dist + 0.3);
  temperature = pow(temperature, 1.5);
  
  // 根据温度混合颜色
  vec3 diskColor = mix(uColorCool, uColorHot, temperature);
  diskColor *= (0.6 + turbulence * 0.8);
  
  // 添加亮度变化和细节
  float brightness = turbulence * temperature;
  diskColor += vec3(1.0, 0.9, 0.7) * brightness * 0.5;
  
  // 边缘渐变
  float edgeFade = smoothstep(2.5, 1.0, dist) * smoothstep(0.4, 0.6, dist);
  
  // 添加螺旋结构
  float spiral = sin(rotatedAngle * 3.0 - dist * 8.0) * 0.5 + 0.5;
  diskColor *= (0.7 + spiral * 0.3);
  
  float alpha = edgeFade * (0.6 + turbulence * 0.4);
  
  gl_FragColor = vec4(diskColor, alpha);
}

