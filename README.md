# 高校编程教育多智能体智能助教系统

> AgentEdu — 基于 AI 大模型的高校编程教育智能助教平台

![](https://img.shields.io/badge/Java-17-brightgreen) ![](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen) ![](https://img.shields.io/badge/Vue-3.5-blue) ![](https://img.shields.io/badge/Element%20Plus-2.9-blue) ![](https://img.shields.io/badge/AI-DeepSeek-blue)

---

## 📚 项目简介

本系统是一个面向高校编程教育的**多智能体 AI 助教平台**，集成智能问答、代码评测、知识库管理、题库管理、学习数据分析等功能。教师端提供完整的题库与知识库管理能力，学生端提供智能答疑、编程练习与学习数据可视化。

> **核心亮点**：内置 Live2D 数字人形象作为 AI 助教形象，配合 DeepSeek 大模型提供拟人化答疑体验。

---

## 🏗 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.3.5 | 核心框架 |
| Java 17 | 编程语言 |
| MyBatis-Plus 3.5.7 | ORM 持久层 |
| Sa-Token 1.39.0 | 身份认证与权限 |
| MySQL 8.0 | 关系数据库 |
| DeepSeek API | AI 对话与反馈生成 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.5 | 渐进式前端框架 |
| Vite 6.0 | 构建工具 |
| Element Plus 2.9 | UI 组件库 |
| ECharts 5.5 | 数据可视化 |
| Monaco Editor | 在线代码编辑器 |
| Live2D / Pixi.js | 数字人形象渲染 |
| Pinia | 状态管理 |
| Axios | HTTP 客户端 |

---

## 🎯 功能模块

### 教师端

- 📋 **题库管理** — 题目增删改查、题库分类、测试用例管理
- 📚 **知识库管理** — 文档上传、知识点分块、RAG 检索支持
- 📊 **数据分析** — 学生提交记录、正确率统计、薄弱点分析
- 👥 **科目管理** — 按课程/班级划分题目与知识

### 学生端

- 🤖 **AI 智能问答** — 多智能体协作，支持代码分析与错误反馈
- 💻 **在线代码评测** — 实时编译运行、测试用例判定
- 📈 **学习仪表盘** — 可视化个人学习进度与能力图谱
- 📖 **知识库检索** — 快速查阅课程相关知识

---

## 🗂 项目结构

```
bs/
├── src/main/java/com/agentedu/     # 后端源码
│   ├── controller/                  # REST API 控制器
│   │   ├── AgentController          # AI 智能体对话
│   │   ├── AiFeedbackController     # AI 反馈生成
│   │   ├── AuthController           # 登录注册认证
│   │   ├── KnowledgeBaseController  # 知识库管理
│   │   ├── ProblemBankController    # 题库管理
│   │   ├── ProblemController        # 题目管理
│   │   ├── SubmissionController     # 提交记录
│   │   ├── StudentDashboardController # 学生数据看板
│   │   └── ...
│   ├── service/                     # 业务逻辑层
│   ├── mapper/                      # 数据访问层
│   ├── entity/                      # 实体类
│   ├── dto/                         # 数据传输对象
│   ├── vo/                          # 视图对象
│   ├── config/                      # 配置类
│   └── utils/                       # 工具类
│
├── frontend/src/                    # 前端源码 (Vue 3)
│   ├── views/
│   │   ├── login/                   # 登录注册页
│   │   ├── teacher/                 # 教师端视图
│   │   └── student/                 # 学生端视图
│   ├── components/                  # 公共组件
│   ├── layout/                      # 布局组件
│   ├── router/                      # 路由配置
│   ├── store/                       # Pinia 状态管理
│   └── utils/                       # 工具函数
│
├── src/main/resources/
│   ├── sql/                         # 数据库脚本
│   └── application.yml              # 后端配置
│
├── pom.xml                          # Maven 依赖配置
└── package.json                     # 前端依赖配置
```

---

## 🚀 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 1. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE agentedu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入初始化脚本（sql/ 目录下）
mysql -u root -p agentedu < sql/demo-data.sql
```

### 2. 配置后端

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agentedu?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

ai:
  base-url: https://api.deepseek.com   # AI 接口地址
  model: deepseek-chat                 # AI 模型
  # api-key: sk-xxxxx                  # 在 AiModelClientConfig 中配置
```

### 3. 启动后端

```bash
# 使用 Maven 启动
cd bs
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/agentedu-backend-0.0.1-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 5. 访问系统

- 学生端：`http://localhost:5173/student`
- 教师端：`http://localhost:5173/teacher`

---

## 🔐 认证说明

本系统使用 **Sa-Token** 进行身份认证：

- 登录后 Token 保存于 `satoken` 请求头
- Token 默认有效期 24 小时
- 角色分为 `teacher` 和 `student`

---

## 📝 API 概览

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/login`, `/api/auth/register` | 登录注册 |
| 题目 | `/api/problem/**` | 题目 CRUD |
| 题库 | `/api/problemBank/**` | 题库管理 |
| 知识库 | `/api/knowledgeBase/**` | 知识库管理 |
| 提交 | `/api/submission/**` | 代码提交与评测 |
| AI 助手 | `/api/agent/ask` | 智能问答 |
| 仪表盘 | `/api/studentDashboard/**` | 学习数据 |

---

## ⚙️ AI 代码评测流程

```
学生提交代码
    ↓
后端接收 → 写入临时文件
    ↓
调用 python评判器 (JudgeProperties)
    ↓
编译运行 → 比对测试用例
    ↓
返回 结果/错误信息
```

评测超时默认 3 秒，代码最大 51200 字符。

---

## 🌐 部署建议

### 前端部署 (Nginx)

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /var/www/agentedu/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
    }
}
```

### 后端部署

```bash
# 打包
./mvnw clean package -DskipTests

# 后台运行
nohup java -jar agentedu-backend-0.0.1-SNAPSHOT.jar --server.port=8080 &
```

---

## 📄 许可证

本项目仅供学习与研究使用。
