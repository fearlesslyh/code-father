# 凌犀零代码平台

## 项目简介

凌犀零代码平台是一个基于 Spring Boot 3.5.7 + Vue 3 的全栈零代码开发平台，集成了 MyBatis-Flex 代码生成器和 AI 智能代码生成功能，提供用户管理和可视化开发功能。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.5.7
- **Java版本**: 21
- **ORM**: MyBatis-Flex 1.11.3
- **数据库**: MySQL 8.0
- **文档**: Knife4j (Swagger3)
- **工具类**: Hutool 5.8.38
- **AI集成**: LangChain4j 1.8.0 + 腾讯云混元大模型
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
│   │   ├── ai/                    # AI代码生成模块
│   │   │   ├── core/              # 核心处理类
│   │   │   │   ├── CodeParser.java        # 流式输出解析器
│   │   │   │   ├── StreamCodeProcessor.java # 流式代码处理器
│   │   │   │   └── AiCodeGeneratorFacade.java # AI代码生成门面
│   │   │   ├── model/             # 数据模型
│   │   │   └── service/           # AI服务接口
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
│   │   ├── application-local.yml # 本地AI配置
│   │   ├── prompt/              # AI提示词模板
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
   - 用户信息管理（支持个人资料更新）
   - 角色权限控制 (user/admin)
   - VIP会员系统

2. **零代码开发**
   - 基于MyBatis-Flex的代码生成
   - 可视化表单设计
   - 自动生成Entity、Mapper、Service、Controller
   - 集成Lombok简化代码

3. **AI智能代码生成** ⭐ **新增功能**
   - 集成腾讯云混元大模型
   - 支持自然语言描述生成HTML/CSS/JS代码
   - 流式输出解析和文件自动保存
   - 单文件和多文件代码生成模式
   - 智能文件引用修正

4. **API文档**
   - 集成Knife4j生成API文档
   - 支持在线调试

### 近期新增功能
- **AI代码生成模块**：基于LangChain4j的智能代码生成服务
- **流式输出解析器**：自动解析AI返回的JSON格式代码
- **文件引用修正**：智能修正HTML文件中的CSS和JS引用
- **个人资料更新**：登录用户可安全更新个人信息
- **测试数据生成**：支持批量生成测试用户数据

## 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Node.js 20.19.0+ 或 22.12.0+
- Maven 3.6+
- 腾讯云混元大模型API密钥（用于AI代码生成功能）

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

### AI配置（可选，如需使用AI代码生成功能）

1. 配置腾讯云混元大模型（`src/main/resources/application-local.yml`）：
```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: your_tencent_cloud_api_key
      base-url: https://api.hunyuan.cloud.tencent.com/v1
      model-name: hunyuan-lite
      strict-json-schema: true
      response-format: json
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

### 传统代码生成器

项目内置了基于MyBatis-Flex的代码生成器，位于 `src/main/java/com/lyh/codefather/generator/Codegen.java`，支持零代码快速开发。

#### 使用方法

1. 配置要生成的表名：
```java
private static final String[] TABLE_NAMES = {"user", "your_table"};
```

2. 运行生成器：
```bash
mvn compile exec:java -Dexec.mainClass="com.lyh.codefather.generator.Codegen"
```

#### 生成内容
- Entity实体类（集成Lombok）
- Mapper接口及XML文件
- Service接口及实现类
- Controller控制器

### AI智能代码生成 ⭐ 新增功能

项目集成了AI智能代码生成功能，支持通过自然语言描述生成HTML/CSS/JS代码。

#### 使用方法

1. **单文件代码生成**：
```java
AiCodeGeneratorFacade facade = new AiCodeGeneratorFacade();
File result = facade.generateAndSave("做一个任务记录网站，代码全部放在一个html文件里，样式要酷炫牛逼", CodeGenTypeEnum.HTML);
```

2. **多文件代码生成**：
```java
File result = facade.generateAndSave("做一个任务记录网站，样式要酷炫", CodeGenTypeEnum.MULTI_FILE);
```

3. **流式输出处理**：
```java
// 解析流式输出
SingleHtmlFileCodeResult singleResult = CodeParser.parseSingleFileStream(streamResponse);
MultiHtmlFileCodeResult multiResult = CodeParser.parseMultiFileStream(streamResponse);

// 处理并保存文件
File savedDir = StreamCodeProcessor.processSingleFileStream(streamResponse);
File savedDir = StreamCodeProcessor.processMultiFileStream(streamResponse);
```

#### 生成内容
- **单文件模式**：生成包含HTML、CSS、JS的单个HTML文件
- **多文件模式**：分别生成HTML、CSS、JS文件，并自动修正文件引用
- **智能修正**：自动检查并修正HTML文件中的CSS和JS引用路径

#### 测试示例
项目包含完整的测试用例，位于 `src/test/java/com/lyh/codefather/ai/AiCodeGeneratorServiceTest.java`，包含：
- 单文件代码生成测试
- 多文件代码生成测试  
- 流式输出解析测试
- 文件引用修正测试

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
- `POST /api/user/update` - 更新用户信息（管理员权限）
- `POST /api/user/update/my` - 更新个人资料（登录用户权限）⭐ 新增
- `POST /api/user/delete` - 删除用户

### AI代码生成接口 ⭐ 新增
- `POST /api/ai/generate/single` - 生成单文件代码
- `POST /api/ai/generate/multi` - 生成多文件代码
- `POST /api/ai/generate/stream` - 流式代码生成

### 健康检查
- `GET /api/health` - 服务健康状态

### 测试数据接口
- `POST /api/test/generate-users` - 生成测试用户数据（批量插入100条随机数据）

## 开发指南

### 代码规范
- 使用Lombok减少样板代码
- 统一异常处理机制
- 使用DTO进行数据传输
- 遵循RESTful API设计原则
- AI代码生成模块采用流式处理和JSON解析模式

### 扩展开发

#### 传统代码生成扩展
1. 新增数据表时，在 `Codegen.java` 中配置表名
2. 运行代码生成器生成基础CRUD代码
3. 在前端 `code-mother/src/api/` 中添加对应的API接口
4. 创建对应的Vue组件

#### AI代码生成扩展 ⭐ 新增
1. **添加新的代码生成类型**：
   - 在 `CodeGenTypeEnum` 中定义新的生成类型
   - 实现对应的解析器和处理器

2. **自定义提示词模板**：
   - 在 `src/main/resources/prompt/` 目录下添加新的提示词文件
   - 在AI服务接口中使用 `@SystemMessage` 注解引用

3. **扩展流式输出解析**：
   - 继承 `CodeParser` 类实现新的解析逻辑
   - 在 `StreamCodeProcessor` 中添加对应的处理方法

### 测试开发
项目包含完整的测试框架，支持：
- 单元测试：使用JUnit 5和Mockito
- 集成测试：Spring Boot Test
- AI代码生成功能测试：包含12个测试用例
- 流式输出解析测试：JSON格式验证和文件引用修正

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

## 已知问题与维护说明

### 已知问题
1. **AI代码生成依赖外部服务**
   - AI代码生成功能依赖腾讯云混元大模型服务
   - 网络不稳定时可能导致生成失败
   - 建议添加重试机制和降级处理

2. **流式输出解析限制**
   - 当前仅支持JSON格式的流式输出
   - 对非标准JSON格式的容错性有限
   - 建议扩展支持更多输出格式

3. **文件引用修正**
   - 自动修正功能对复杂HTML结构的处理有限
   - 建议手动验证生成的HTML文件引用

4. **测试数据生成**
   - 批量插入测试数据时可能遇到数据库连接限制
   - 建议分批插入或使用事务管理

### 维护说明

#### 数据库维护
- 定期备份用户数据和生成代码
- 监控数据库性能指标
- 清理过期的生成代码文件

#### AI服务维护
- 定期更新腾讯云API密钥
- 监控AI服务调用频率和费用
- 更新提示词模板以优化生成效果

#### 代码维护
- 保持LangChain4j依赖版本更新
- 定期运行测试用例确保功能正常
- 监控日志文件中的错误和警告信息

#### 安全维护
- 定期更新依赖库的安全补丁
- 监控用户认证和授权机制
- 保护AI服务API密钥安全

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