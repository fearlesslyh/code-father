// 粒子流动场片段着色器
uniform float uTime;
uniform float uBrightness;

varying vec3 vColor;
varying float vAlpha;

void main() {
  // 创建圆形粒子
  vec2 center = gl_PointCoord - vec2(0.5);
  float dist = length(center);
  
  if (dist > 0.5) {
    discard;
  }
  
  // 柔和发光边缘
  float alpha = 1.0 - smoothstep(0.2, 0.5, dist);
  alpha = pow(alpha, 2.0);
  
  // 添加核心高亮
  float core = 1.0 - smoothstep(0.0, 0.15, dist);
  core = pow(core, 3.0);
  
  // 颜色叠加
  vec3 color = vColor;
  color += vec3(1.0) * core * 0.5; // 中心更亮
  
  // 轻微脉动
  float pulse = sin(uTime * 2.0 + gl_FragCoord.x * 0.01 + gl_FragCoord.y * 0.01) * 0.5 + 0.5;
  pulse = pow(pulse, 2.0);
  
  color *= (0.8 + pulse * 0.2);
  
  // 最终透明度
  alpha *= vAlpha * uBrightness;
  
  gl_FragColor = vec4(color, alpha);
}

