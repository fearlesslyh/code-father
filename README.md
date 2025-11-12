# 凌犀零代码平台

## 项目简介

凌犀零代码平台是一个基于 Spring Boot 3.5.7 + Vue 3 的全栈零代码开发平台，集成了 MyBatis-Flex 代码生成器，提供用户管理和可视化开发功能。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.5.7
- **Java版本**: 21
- **ORM**: MyBatis-Flex 1.11.3
- **数据库**: MySQL 8.0
- **文档**: Knife4j (Swagger3)
- **工具类**: Hutool 5.8.38
- **构建工具**: Maven

### 前端技术
- **框架**: Vue 3 + TypeScript
- **UI组件**: Ant Design Vue 4.2.6
- **状态管理**: Pinia 3.0.3
- **路由**: Vue Router 4.6.3
- **HTTP客户端**: Axios 1.13.2
- **构建工具**: Vite 7.1.11

## 项目结构

```
code-father/
├── src/                          # 后端代码
│   ├── main/java/com/lyh/codefather/
│   │   ├── CodeFatherApplication.java    # 启动类
│   │   ├── common/               # 通用类
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── exception/            # 异常处理
│   │   ├── generator/            # 代码生成器
│   │   ├── mapper/               # 数据访问层
│   │   ├── model/                # 数据模型
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── entity/           # 实体类
│   │   │   └── enums/            # 枚举类
│   │   └── service/              # 业务逻辑层
│   ├── main/resources/
│   │   ├── application.yml       # 应用配置
│   │   └── mapper/               # MyBatis映射文件
│   └── sql/create_sql.sql        # 数据库初始化脚本
├── code-mother/                  # 前端代码
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── components/           # 组件
│   │   ├── layouts/              # 布局
│   │   ├── pages/                # 页面
│   │   ├── router/               # 路由
│   │   ├── stores/               # 状态管理
│   │   └── views/                # 视图
│   └── package.json              # 前端依赖配置
└── pom.xml                       # Maven配置
```

## 功能特性

### 核心功能
1. **用户管理**
   - 用户注册/登录
   - 用户信息管理
   - 角色权限控制 (user/admin)
   - VIP会员系统

2. **零代码开发**
   - 基于MyBatis-Flex的代码生成
   - 可视化表单设计
   - 自动生成Entity、Mapper、Service、Controller
   - 集成Lombok简化代码

3. **API文档**
   - 集成Knife4j生成API文档
   - 支持在线调试

## 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Node.js 20.19.0+ 或 22.12.0+
- Maven 3.6+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE code_father;
```

2. 执行初始化脚本：
```sql
USE code_father;
-- 执行 src/sql/create_sql.sql 中的SQL语句
```

### 后端启动

1. 配置数据库连接信息（`src/main/resources/application.yml`）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/code_father
    username: your_username
    password: your_password
```

2. 启动应用：
```bash
# 使用Maven
mvn spring-boot:run

# 或者直接运行
java -jar target/code-father-0.0.1-SNAPSHOT.jar
```

3. 访问API文档：
   - http://localhost:8123/api/doc.html

### 前端启动

1. 进入前端目录：
```bash
cd code-mother
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 访问前端应用：
   - http://localhost:5173

## 零代码开发使用

项目内置了基于MyBatis-Flex的代码生成器，位于 `src/main/java/com/lyh/codefather/generator/Codegen.java`，支持零代码快速开发。

### 使用方法

1. 配置要生成的表名：
```java
private static final String[] TABLE_NAMES = {"user", "your_table"};
```

2. 运行生成器：
```bash
mvn compile exec:java -Dexec.mainClass="com.lyh.codefather.generator.Codegen"
```

### 生成内容
- Entity实体类（集成Lombok）
- Mapper接口及XML文件
- Service接口及实现类
- Controller控制器

## 配置说明

### 主要配置项

**后端配置** (`application.yml`):
- 服务端口：8123
- API路径前缀：/api
- Knife4j文档：启用中文界面

**前端配置** (`code-mother/package.json`):
- Vue 3 + TypeScript
- Ant Design Vue UI组件
- Vite构建工具

## API接口

### 用户相关接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录  
- `GET /api/user/current` - 获取当前用户信息
- `POST /api/user/logout` - 用户登出
- `POST /api/user/update` - 更新用户信息
- `POST /api/user/delete` - 删除用户

### 健康检查
- `GET /api/health` - 服务健康状态

## 开发指南

### 代码规范
- 使用Lombok减少样板代码
- 统一异常处理机制
- 使用DTO进行数据传输
- 遵循RESTful API设计原则

### 扩展开发
1. 新增数据表时，在 `Codegen.java` 中配置表名
2. 运行代码生成器生成基础CRUD代码
3. 在前端 `code-mother/src/api/` 中添加对应的API接口
4. 创建对应的Vue组件

## 部署说明

### 后端部署
```bash
# 打包
mvn clean package

# 运行
java -jar target/code-father-0.0.1-SNAPSHOT.jar
```

### 前端部署
```bash
cd code-mother
npm run build
# 将dist目录部署到Web服务器
```

## 许可证

本项目采用开源许可证，具体信息请查看LICENSE文件。

## 贡献指南

欢迎提交Issue和Pull Request来改进项目。

## 联系方式

- 项目维护者：梁懿豪
- GitHub: [fearlesslyh](https://github.com/fearlesslyh)

---

**注意**: 首次运行前请确保数据库已正确配置，并执行初始化SQL脚本。

---

**项目名称**: 凌犀零代码平台  
**项目定位**: 为企业提供快速、高效的零代码开发解决方案