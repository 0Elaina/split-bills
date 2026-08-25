# Split Bills (分账助手)

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Vue](https://img.shields.io/badge/vue-3.x-brightgreen.svg)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-success.svg)

一个极简、优雅的全栈分账应用，专为合租、聚餐、旅行等群体活动设计。帮助团队轻松记录消费明细，并一键计算出最少笔数的转账方案，彻底告别复杂的算账烦恼。

## ✨ 核心特性

- **账本与成员管理**：支持创建多个独立账本，账本内成员自由增删改。
- **灵活的记账方式**：记录每笔开销的名称、金额、日期、付款人以及参与平摊的人员。
- **智能结算引擎**：
  - **无损分摊**：采用严格的“分”精度计算与 Penny-Dropping 尾差算法，保证 100元/3人 等情况下的账目 100% 抹平。
  - **极简转账**：基于贪心算法匹配债权与债务，生成笔数最少的建议转账方案。
- **响应式 UI**：基于 Vue 3 + TailwindCSS + Vuetify，拥有丝滑的交互体验和现代化的多端响应式设计。

## 🛠️ 技术栈

### 前端
- Vue 3 (Composition API)
- TypeScript
- Vite
- Pinia (状态管理)
- Vue Router
- TailwindCSS (原子化样式)
- Vuetify (组件库)

### 后端
- Spring Boot 3
- Java 17+
- MyBatis-Plus (ORM)
- H2 Database (内嵌数据库，克隆即运行)

## 🚀 快速启动

得益于 H2 内存数据库的配置，本项目无需在本地安装任何外部数据库服务，只需几步即可启动。

### 1. 启动后端

```bash
cd backend
# 使用 Maven Wrapper 启动
./mvnw spring-boot:run
```
后端服务将运行在 `http://localhost:8080`，首次启动会自动执行 `schema.sql` 完成建表。

### 2. 启动前端

```bash
cd frontend
# 安装依赖
pnpm install
# 启动开发服务器
pnpm run dev
```
前端服务将运行在 `http://localhost:5173`。

## 📁 目录结构

```
.
├── backend/            # Spring Boot 后端工程
├── frontend/           # Vue 3 前端工程
├── docs/               # 需求与接口文档
├── plans/              # 开发阶段规划记录
├── design/             # UI 原型设计
└── LICENSE             # MIT 协议
```

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。你可以自由地使用、修改和分发本项目的代码。
