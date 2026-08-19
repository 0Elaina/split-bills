# 工程运行指南

## 1. 文档职责

本文记录已经由工程配置和实际运行结果确认的本地环境、启动方式、访问入口、持久化位置与质量门禁。业务范围、模块边界、数据库结构和 REST 契约分别以 `docs/01-requirements-specification.md`、`docs/02-architecture-design.md`、`docs/03-database-design.md` 和 `docs/04-api-design/v1-api.md` 为准。

## 2. 已验证环境

| 范围 | 已验证版本或要求 |
| --- | --- |
| Java | Java 17 |
| Maven Wrapper | 3.9.16 |
| Spring Boot | 3.5.16 |
| MyBatis-Plus | 3.5.17 |
| Node.js | 24.12.0；工程要求 `^22.18.0 || >=24.12.0` |
| pnpm | 11.19.0，由 `frontend/package.json` 固定 |
| Vue | 3.5.41 |
| Vue Router | 5.2.0 |
| Pinia | 4.0.3 |
| Axios | 1.19.0 |
| Vite | 8.2.1 |
| TypeScript | 6.0.3 |
| Vuetify | 4.1.9 |

前端依赖的精确解析结果以 `frontend/pnpm-lock.yaml` 为准，后端依赖由 Maven 与 Spring Boot 依赖管理确定。仓库不要求全局安装 Maven。

## 3. 首次准备

Node.js 通过 nvm 或其他方式安装后，使用 Corepack 启用项目约定的 pnpm：

```bash
corepack enable pnpm
corepack install --global pnpm@11.19.0
pnpm --version
```

首次取得前端工程后，在 `frontend/` 安装锁文件声明的依赖：

```bash
pnpm install --frozen-lockfile
```

Maven Wrapper 会在首次执行时下载项目指定的 Maven 及后端依赖，不需要执行额外安装命令。

## 4. 本地启动

前后端使用两个终端运行。建议先启动后端，避免前端首次请求时代理目标尚未就绪。

### 4.1 后端

从 `backend/` 目录运行：

```bash
./mvnw spring-boot:run
```

后端固定监听 `127.0.0.1:8080`。必须从 `backend/` 启动，因为 H2 文件路径相对于进程工作目录解析。

### 4.2 前端

从 `frontend/` 目录运行：

```bash
pnpm dev
```

Vite 默认使用本机地址和 5173 端口；端口被占用时会自动选择下一可用端口，实际浏览器地址以终端输出为准。

## 5. 请求链路

```mermaid
flowchart LR
    Browser[浏览器] --> Vite[Vite 开发服务器]
    Vite -->|/api 原样代理| Backend[127.0.0.1:8080]
    Backend --> H2[(H2 文件数据库)]
```

前端 Axios 客户端使用 `/api/v1` 作为基础路径，Vite 将 `/api` 请求代理到 `http://127.0.0.1:8080`，且不重写路径。因此本地开发不需要额外配置 CORS，也不得在业务模块中散落后端主机地址。

## 6. H2 连接与持久化

| 项目 | 本地开发值 |
| --- | --- |
| 控制台入口 | `http://127.0.0.1:8080/h2-console` |
| JDBC URL | `jdbc:h2:file:./data/split-bills` |
| Driver Class | `org.h2.Driver` |
| 用户名 | `sa` |
| 密码 | 空 |
| 仓库相对数据文件 | `backend/data/split-bills.mv.db` |

H2 控制台和空密码仅用于本机开发。`backend/data/` 已被 Git 忽略，数据库运行数据不得提交到仓库。

### 6.1 重置本地数据库

> **破坏性操作：** 以下操作会永久删除全部本地账本、成员和消费数据，无法恢复。执行前必须停止后端，并确认不需要保留当前数据。

从仓库根目录删除明确命名的 H2 数据文件：

```bash
rm -f backend/data/split-bills.mv.db backend/data/split-bills.trace.db
```

再次启动后端时，Spring Boot 会执行 `schema.sql` 重新创建已确认的四张空表。不得通过删除整个 `backend/`、仓库根目录或其他宽泛路径来重置数据库。

## 7. 质量门禁

### 7.1 后端

从 `backend/` 运行：

```bash
./mvnw clean package
```

### 7.2 前端

从 `frontend/` 依次运行：

```bash
pnpm lint
pnpm type-check
pnpm build
```

`lint` 只检查而不修改文件；需要主动修复可安全处理的问题时才使用 `pnpm lint:fix`。格式化使用 `pnpm format`，应在质量门禁之前执行。

## 8. 常见问题

| 现象 | 检查与处理 |
| --- | --- |
| 找不到 `pnpm` | 确认当前 Node 环境包含 Corepack，再执行第 3 节的启用命令；不要使用 `sudo` 向 nvm 目录安装包。 |
| Maven 下载曾超时并缓存失败 | 网络恢复后在 `backend/` 执行 `./mvnw -U clean package`，强制重新检查依赖。 |
| 后端提示 8080 被占用 | 停止占用端口的旧后端进程；后端端口是固定契约，不自动切换。 |
| 前端未使用 5173 | Vite 会在端口占用时自动顺延，以终端打印的地址为准。 |
| 前端请求代理失败 | 先确认后端正在监听 `127.0.0.1:8080`，再核对请求是否以 `/api` 开头。 |
| H2 控制台无法连接 | 确认后端已启动、JDBC URL 完整一致，并从 `backend/` 目录启动了进程。 |

## 9. 当前工程边界

- 只维护本地开发配置，不提供生产 profile、容器、部署或自动化运维方案。
- 不创建专用健康、演示或 Mock 接口；工程可用性通过构建、启动和真实业务链路验证。
- H2 控制台不属于正式业务入口，不得暴露到非本机网络。
- 仓库不保存密码、token、临时业务数据或完整运行日志。
