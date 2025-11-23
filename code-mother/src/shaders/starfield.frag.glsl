// 星空片段着色器
uniform float uTime;
uniform float uBrightness;

varying vec3 vColor;

void main() {
  // 创建圆形星点
  vec2 center = gl_PointCoord - vec2(0.5);
  float dist = length(center);
  
  if (dist > 0.5) {
    discard;
  }
  
  // 柔和边缘
  float alpha = 1.0 - smoothstep(0.3, 0.5, dist);
  alpha = pow(alpha, 2.0);
  
  // 添加闪烁效果
  float twinkle = sin(uTime * 2.0 + gl_FragCoord.x * 0.1 + gl_FragCoord.y * 0.1) * 0.5 + 0.5;
  twinkle = pow(twinkle, 3.0);
  
  vec3 color = vColor * (0.8 + twinkle * 0.2) * uBrightness;
  
  gl_FragColor = vec4(color, alpha);
}

