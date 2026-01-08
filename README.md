# 个人博客系统 (Personal Blog System)

基于 Spring Boot 3 + Thymeleaf 的个人博客系统，采用传统MVC架构，支持文章发布、评论、点赞收藏和管理后台。

## ✨ 功能特性

### 前台功能
- 📝 文章浏览、搜索、分页
- 💬 评论发表与管理
- ❤️ 文章点赞与收藏
- 👤 用户注册、登录
- 📊 个人中心（查看点赞/收藏）

### 后台功能
- 📰 文章管理（发布、编辑、删除）
- 👥 用户管理（禁言、删除）
- 📈 数据统计（访问量、用户数）
- 🎯 管理后台Dashboard

### 技术特性
- 🔐 JWT无状态认证
- 🛡️ Spring Security权限控制
- ⏰ 定时任务统计
- 📚 Swagger UI API文档
- ✅ 单元测试 + 集成测试

## 🛠️ 技术栈

- **后端框架**: Spring Boot 3.2.0
- **模板引擎**: Thymeleaf
- **ORM框架**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0
- **安全框架**: Spring Security + JWT
- **API文档**: Springdoc OpenAPI 2.3.0
- **工具库**: Hutool, Lombok

## 📦 项目结构

```
blog/
├── src/main/java/com/blog/
│   ├── config/          # 配置类（Security、OpenAPI等）
│   ├── controller/      # REST API控制器
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis数据访问层
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── security/        # JWT认证过滤器
│   ├── task/            # 定时任务
│   ├── util/            # 工具类
│   └── exception/       # 异常处理
├── src/main/resources/
│   ├── templates/       # Thymeleaf模板
│   │   ├── index.html
│   │   ├── login.html
│   │   └── admin/       # 管理后台页面
│   ├── static/          # 静态资源
│   ├── sql/             # 数据库脚本
│   └── application.yml  # 配置文件
└── src/test/            # 测试代码
```

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

```sql
# 创建数据库
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入SQL脚本
USE blog;
source src/main/resources/sql/schema.sql;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog
    username: root
    password: your_password

jwt:
  secret: your-secret-key-at-least-256-bits
```

### 4. 运行项目

```bash
# 使用Maven运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/blog-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

- **前端首页**: http://localhost:8080/
- **管理后台**: http://localhost:8080/admin
- **API文档**: http://localhost:8080/swagger-ui.html

### 6. 创建管理员账户

1. 在前端注册账号
2. 在数据库中设置管理员权限：

```sql
UPDATE user SET is_admin = 1 WHERE username = '你的用户名';
```

3. 重新登录即可访问管理后台

## 📖 API文档

项目集成了 Swagger UI，启动后访问 http://localhost:8080/swagger-ui.html

### 使用JWT认证测试API

1. 调用 `/api/auth/login` 接口获取Token
2. 点击 Swagger UI 右上角 "Authorize" 按钮
3. 输入Token（不需要"Bearer "前缀）
4. 即可测试需要认证的API

## 🎯 核心功能说明

### JWT认证流程

1. 用户登录 → 后端验证 → 生成JWT Token
2. 前端存储Token到localStorage
3. 后续请求携带 `Authorization: Bearer {token}` header
4. 后端验证Token并提取用户信息

### 角色权限

- **游客**: 浏览文章、查看评论
- **普通用户**: 发表评论、点赞收藏
- **管理员**: 发布文章、用户管理、查看统计

### 定时任务

每天凌晨1点自动执行统计聚合：
- 计算前一天的PV（页面浏览量）
- 计算UV（访客数）
- 统计新注册用户数
- 数据保存到 `site_statistics` 表

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行指定测试
mvn test -Dtest=UserServiceTest
```

测试覆盖：
- 单元测试：Service层核心逻辑
- 集成测试：Controller接口测试

## 📄 许可证

[MIT License](LICENSE)
