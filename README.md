# SelfSchedule 智能任务管家

<div align="center">

一款基于 AI 驱动的个人效率管理 Web 应用，集任务管理、番茄专注计时、数据统计于一体，辅以游戏化激励与多渠道通知系统。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-green)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3%20%2B%20TypeScript-blue)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-lightgrey)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6%2B-red)](https://redis.io/)

**在线演示：** https://cjlkybsa.top

</div>

## 核心亮点

- **AI 智能解析**：输入自然语言描述，AI 自动解析为结构化任务（含优先级、截止时间、提醒规则）
- **游戏化专注森林**：番茄钟 + 心跳防作弊 + 贡献热力图 + 连续打卡激励
- **12 种主题切换**：Forest / Dark / Eyecare / Sage / Slate / Blue 等，支持暗色模式
- **多渠道通知**：SSE 实时推送 + 站内消息 + 企业微信机器人 Webhook，提醒不遗漏
- **成就勋章体系**：24 个成就覆盖 4 大分类，驱动用户持续活跃

## 功能特性

| 模块 | 功能 |
|:---|:---|
| **任务管理** | CRUD、子任务拆分、重复任务自动生成、优先级、分类标签、归档、CSV 导出 |
| **专注计时** | 番茄钟 / 自定义时长、关联任务、心跳防作弊、4 种场景音效、专注评分 |
| **数据统计** | 专注时长趋势图、分类统计、打卡日历、周复盘、连续打卡、成就勋章 |
| **AI 助手** | 自然语言创建任务、聊天式交互 |
| **提醒系统** | 定时扫描触发、SSE 实时推送、企业微信机器人 Webhook 推送 |
| **用户体系** | 注册 / 登录、邮箱验证码、密码重置、微信登录、头像上传、账号封禁与申诉 |
| **社交功能** | 好友申请 / 管理、消息通知中心 |
| **管理后台** | 用户管理、反馈审核、申诉处理、在线用户监控、群发消息 |

## 技术栈

### 后端

| 技术 | 用途 |
|:---|:---|
| Spring Boot 3.1 + Java 17 | 应用框架 |
| MyBatis-Plus | ORM |
| Spring Security + JWT | 认证授权 |
| Redis | 缓存 + 会话管理 |
| MySQL 8 | 持久化 |
| SSE | 实时推送 |

### 前端

| 技术 | 用途 |
|:---|:---|
| Vue 3 + TypeScript | 框架 |
| Vite | 构建工具 |
| Vuex + Composables | 状态管理 |
| Chart.js | 数据可视化 |
| 自定义设计系统 | 12 种主题 + 动画 |

### 部署

Ubuntu 24.04 + Nginx (HTTPS)，前端构建产物打包进 Spring Boot JAR，单文件部署。

## 项目结构

```
├── self_schedule/                  # 后端 (Spring Boot)
│   ├── src/main/java/com/cjl/self_schedule/
│   │   ├── auth/                   # 认证模块
│   │   ├── task/                   # 任务管理
│   │   ├── focus/                  # 专注记录
│   │   ├── reminder/               # 提醒管理
│   │   ├── ai/                     # AI 任务解析
│   │   ├── admin/                  # 管理后台
│   │   ├── achievement/            # 成就系统
│   │   ├── friend/                 # 好友系统
│   │   ├── appeal/                 # 申诉模块
│   │   ├── feedback/               # 反馈模块
│   │   ├── message/                # 消息模块
│   │   └── common/                 # 公共配置
│   └── src/main/resources/
│       ├── application.yml
│       ├── mapper/                 # MyBatis XML
│       └── static/                 # 前端构建产物
│
├── project-self-schedule-vue-ts/   # 前端 (Vue 3 + TypeScript)
│   ├── src/
│   │   ├── views/                  # 页面组件
│   │   ├── components/             # 公共组件
│   │   ├── composables/            # 组合式函数
│   │   ├── store/                  # 状态管理
│   │   ├── api/                    # 接口封装
│   │   ├── router/                 # 路由配置
│   │   ├── styles/                 # 样式与设计系统
│   │   └── types/                  # 类型定义
│   └── vite.config.ts
│
├── screenshot/                     # 项目截图
├── deploy/                         # 部署脚本
└── self_schedule.sql               # 数据库初始化脚本
```

## 快速启动

### 环境要求

JDK 17+、MySQL 8.0+、Redis 6.0+、Node.js 18+、Maven 3.8+

### 后端启动

```bash
cd self_schedule
# 配置环境变量
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET_KEY=your_jwt_secret_key_at_least_32_chars
export AES_SECRET_KEY=your_aes_secret_key_16_chars

# 导入数据库
mysql -u root -p self_schedule < self_schedule.sql

# 启动
mvn spring-boot:run
```

### 前端开发

```bash
cd project-self-schedule-vue-ts
npm install
npm run dev
```

### 生产部署

```bash
# 构建前端（自动输出到后端 static 目录）
cd project-self-schedule-vue-ts && npm run build

# 打包后端 JAR
cd ../self_schedule && mvn clean package -DskipTests

# 上传到服务器并启动
scp target/self_schedule-0.0.1-SNAPSHOT.jar root@your-server:/opt/self-schedule/
```

## API 接口

共 **81 个** RESTful API，覆盖认证管理、用户管理、任务管理、子任务、专注记录、成就系统、好友系统、消息通知、提醒系统、反馈系统、申诉系统、管理员后台、AI 任务解析、SSE 实时推送。

## 截图

### 认证页面
| 登录页 | 注册页 |
|:---:|:---:|
| ![登录](screenshot/login.png) | ![注册](screenshot/register.png) |

### 首页与任务管理
| 首页 Dashboard | 任务管理 |
|:---:|:---:|
| ![首页](screenshot/home.png) | ![任务](screenshot/task.png) |

### 专注与提醒
| 专注计时 | 提醒管理 |
|:---:|:---:|
| ![专注](screenshot/focus.png) | ![提醒](screenshot/reminder.png) |

### 个人中心
| 用户中心 |
|:---:|
| ![用户](screenshot/user.png) |

### 社交功能
| 好友 | 消息中心 |
|:---:|:---:|
| ![好友](screenshot/friend.png) | ![消息](screenshot/message.png) |

### 帮助与反馈
| 帮助中心 | 反馈建议 |
|:---:|:---:|
| ![帮助](screenshot/help.png) | ![反馈](screenshot/feedback.png) |

## License

MIT
