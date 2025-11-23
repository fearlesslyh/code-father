// 粒子流动场顶点着色器
attribute float size;
attribute vec3 velocity;
attribute float phase;

uniform float uTime;
uniform float uPixelRatio;
uniform vec2 uMouse;
uniform float uMouseInfluence;

varying vec3 vColor;
varying float vAlpha;

// 3D Simplex Noise
vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x*34.0)+1.0)*x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v) { 
  const vec2 C = vec2(1.0/6.0, 1.0/3.0);
  const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

  vec3 i  = floor(v + dot(v, C.yyy));
  vec3 x0 = v - i + dot(i, C.xxx);

  vec3 g = step(x0.yzx, x0.xyz);
  vec3 l = 1.0 - g;
  vec3 i1 = min(g.xyz, l.zxy);
  vec3 i2 = max(g.xyz, l.zxy);

  vec3 x1 = x0 - i1 + C.xxx;
  vec3 x2 = x0 - i2 + C.yyy;
  vec3 x3 = x0 - D.yyy;

  i = mod289(i); 
  vec4 p = permute(permute(permute( 
            i.z + vec4(0.0, i1.z, i2.z, 1.0))
          + i.y + vec4(0.0, i1.y, i2.y, 1.0)) 
          + i.x + vec4(0.0, i1.x, i2.x, 1.0));

  float n_ = 0.142857142857;
  vec3 ns = n_ * D.wyz - D.xzx;

  vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

  vec4 x_ = floor(j * ns.z);
  vec4 y_ = floor(j - 7.0 * x_);

  vec4 x = x_ *ns.x + ns.yyyy;
  vec4 y = y_ *ns.x + ns.yyyy;
  vec4 h = 1.0 - abs(x) - abs(y);

  vec4 b0 = vec4(x.xy, y.xy);
  vec4 b1 = vec4(x.zw, y.zw);

  vec4 s0 = floor(b0)*2.0 + 1.0;
  vec4 s1 = floor(b1)*2.0 + 1.0;
  vec4 sh = -step(h, vec4(0.0));

  vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
  vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;

  vec3 p0 = vec3(a0.xy, h.x);
  vec3 p1 = vec3(a0.zw, h.y);
  vec3 p2 = vec3(a1.xy, h.z);
  vec3 p3 = vec3(a1.zw, h.w);

  vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2,p2), dot(p3,p3)));
  p0 *= norm.x;
  p1 *= norm.y;
  p2 *= norm.z;
  p3 *= norm.w;

  vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
  m = m * m;
  return 42.0 * dot(m*m, vec4(dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3)));
}

void main() {
  // 计算流动场偏移
  vec3 pos = position;
  
  // 使用 noise 创建流动效果
  float noiseScale = 0.3;
  float noiseTime = uTime * 0.15;
  
  vec3 noisePos = pos * noiseScale + vec3(noiseTime);
  float noise1 = snoise(noisePos);
  float noise2 = snoise(noisePos + vec3(100.0));
  float noise3 = snoise(noisePos + vec3(200.0));
  
  vec3 flowOffset = vec3(noise1, noise2, noise3) * 2.0;
  pos += flowOffset;
  
  // 添加循环运动
  pos.x += sin(uTime * 0.2 + phase) * 0.5;
  pos.y += cos(uTime * 0.15 + phase * 1.3) * 0.5;
  pos.z += sin(uTime * 0.1 + phase * 0.7) * 0.3;
  
  // 鼠标影响
  vec2 mousePos = uMouse * 2.0 - 1.0;
  vec2 particleScreen = (modelViewMatrix * vec4(position, 1.0)).xy;
  float distToMouse = length(particleScreen - mousePos * 10.0);
  float mouseEffect = exp(-distToMouse * 0.1) * uMouseInfluence;
  
  // 鼠标排斥效果
  vec2 mouseDir = normalize(particleScreen - mousePos * 10.0);
  pos.xy += mouseDir * mouseEffect * 2.0;
  
  // 计算颜色 - 金色到青蓝渐变
  float colorPhase = (pos.z + 10.0) / 20.0;
  vec3 color1 = vec3(1.0, 0.84, 0.0);  // 金色
  vec3 color2 = vec3(0.2, 0.8, 1.0);   // 青蓝
  vec3 color3 = vec3(0.4, 0.6, 1.0);   // 亮蓝
  
  vColor = mix(color1, color2, colorPhase);
  vColor = mix(vColor, color3, abs(sin(uTime * 0.5 + phase)));
  
  // 透明度变化
  vAlpha = 0.3 + abs(sin(uTime * 0.8 + phase)) * 0.5;
  vAlpha *= (1.0 + mouseEffect * 2.0);
  
  // 计算最终位置
  vec4 mvPosition = modelViewMatrix * vec4(pos, 1.0);
  gl_Position = projectionMatrix * mvPosition;
  
  // 粒子大小 - 带景深
  float sizeAttenuation = 300.0 / -mvPosition.z;
  gl_PointSize = size * uPixelRatio * sizeAttenuation * (1.0 + mouseEffect * 0.5);
}

