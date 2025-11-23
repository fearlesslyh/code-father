// 连接线顶点着色器
uniform float uTime;

varying float vAlpha;

void main() {
  vAlpha = 1.0;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}

