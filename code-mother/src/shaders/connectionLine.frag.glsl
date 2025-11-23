// 连接线片段着色器
uniform float uTime;
uniform vec3 uLineColor;

varying float vAlpha;

void main() {
  // 柔和脉动效果
  float pulse = sin(uTime * 3.0) * 0.5 + 0.5;
  pulse = pow(pulse, 2.0);
  
  vec3 color = uLineColor * (0.6 + pulse * 0.4);
  float alpha = 0.2 + pulse * 0.3;
  
  gl_FragColor = vec4(color, alpha * vAlpha);
}

