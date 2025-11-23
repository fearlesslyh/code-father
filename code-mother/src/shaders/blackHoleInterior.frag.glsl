// 黑洞内部着色器 - 无限折叠扭曲空间
uniform float uTime;
uniform vec2 uResolution;
uniform float uDistortionStrength;
uniform vec3 uCameraPosition;

varying vec2 vUv;

// 3D噪声函数
vec3 hash3(vec3 p) {
  p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
           dot(p, vec3(269.5, 183.3, 246.1)),
           dot(p, vec3(113.5, 271.9, 124.6)));
  return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise3D(vec3 p) {
  vec3 i = floor(p);
  vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  
  return mix(
    mix(mix(dot(hash3(i + vec3(0, 0, 0)), f - vec3(0, 0, 0)),
            dot(hash3(i + vec3(1, 0, 0)), f - vec3(1, 0, 0)), f.x),
        mix(dot(hash3(i + vec3(0, 1, 0)), f - vec3(0, 1, 0)),
            dot(hash3(i + vec3(1, 1, 0)), f - vec3(1, 1, 0)), f.x), f.y),
    mix(mix(dot(hash3(i + vec3(0, 0, 1)), f - vec3(0, 0, 1)),
            dot(hash3(i + vec3(1, 0, 1)), f - vec3(1, 0, 1)), f.x),
        mix(dot(hash3(i + vec3(0, 1, 1)), f - vec3(0, 1, 1)),
            dot(hash3(i + vec3(1, 1, 1)), f - vec3(1, 1, 1)), f.x), f.y), f.z);
}

// 色散效果
vec3 chromaticAberration(vec2 uv, float amount) {
  vec2 direction = uv - vec2(0.5);
  float r = length(texture2D(uResolution, uv + direction * amount * 0.01).r);
  float g = length(texture2D(uResolution, uv).g);
  float b = length(texture2D(uResolution, uv - direction * amount * 0.01).b);
  return vec3(r, g, b);
}

// 旋涡扭曲
vec2 swirlDistortion(vec2 uv, float strength) {
  vec2 center = vec2(0.5);
  vec2 delta = uv - center;
  float dist = length(delta);
  float angle = atan(delta.y, delta.x);
  
  // 创建旋涡
  angle += strength * (1.0 - dist) * sin(uTime * 0.5);
  angle += noise3D(vec3(delta * 5.0, uTime * 0.3)) * 0.5;
  
  return center + vec2(cos(angle), sin(angle)) * dist;
}

// 无限折叠
vec3 infiniteFold(vec3 p, float time) {
  float scale = 2.0;
  for (int i = 0; i < 4; i++) {
    p = abs(p) - 1.0;
    p *= scale;
    p = p.zxy;
    p += vec3(sin(time * 0.1), cos(time * 0.15), sin(time * 0.13)) * 0.5;
  }
  return p;
}

void main() {
  vec2 uv = vUv;
  
  // 应用旋涡扭曲
  vec2 distortedUv = swirlDistortion(uv, uDistortionStrength);
  
  // 创建射线方向
  vec3 rayDir = normalize(vec3((distortedUv - 0.5) * 2.0, 1.0));
  vec3 rayOrigin = uCameraPosition;
  
  // 行进距离
  float t = uTime * 0.5;
  vec3 p = rayOrigin + rayDir * t;
  
  // 应用无限折叠
  p = infiniteFold(p, uTime);
  
  // 创建基于噪声的颜色
  float n1 = noise3D(p * 2.0 + uTime * 0.2);
  float n2 = noise3D(p * 4.0 - uTime * 0.3);
  float n3 = noise3D(p * 8.0 + uTime * 0.1);
  
  // 神秘的色彩
  vec3 color1 = vec3(0.1, 0.2, 0.8); // 深蓝
  vec3 color2 = vec3(0.8, 0.3, 0.9); // 紫色
  vec3 color3 = vec3(0.2, 0.8, 0.9); // 青色
  
  vec3 color = mix(color1, color2, n1 * 0.5 + 0.5);
  color = mix(color, color3, n2 * 0.5 + 0.5);
  
  // 添加能量条纹
  float stripes = sin(p.x * 20.0 + p.y * 15.0 + p.z * 25.0 - uTime * 2.0);
  stripes = pow(abs(stripes), 0.5);
  color += vec3(0.5, 0.7, 1.0) * stripes * 0.3;
  
  // 添加发光效果
  float glow = abs(n3) * 2.0;
  color += vec3(1.0, 0.8, 0.9) * glow * 0.5;
  
  // 色散效果
  float aberration = uDistortionStrength * 0.5;
  color.r *= 1.0 + aberration * sin(uTime + length(uv - 0.5));
  color.b *= 1.0 + aberration * cos(uTime * 1.3 + length(uv - 0.5));
  
  // 添加渐晕效果
  float vignette = 1.0 - length(uv - 0.5) * 0.8;
  color *= vignette;
  
  gl_FragColor = vec4(color, 1.0);
}

