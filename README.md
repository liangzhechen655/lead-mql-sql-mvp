# 市场线索到 MQL/SQL 的简化系统 MVP

姓名/学号/联系方式：待补充

这是一个用于 AI Coding / FDE 实习生作业的可运行 MVP。系统模拟市场线索从投放进入、外呼清洗、加微承接、销售跟进，到 MQL/SQL 转化的完整流程。

## 功能清单

- 新增线索和 CSV 批量导入。
- 按手机号去重，导入后返回新增、重复、失败数量。
- 线索列表支持按状态、来源、负责人和关键词筛选。
- 支持销售负责人分配。
- 支持状态变更，并自动记录状态历史和操作日志。
- 支持跟进记录，MQL 前必须至少有一次跟进。
- 支持 MQL / SQL 标记，SQL 必须从 MQL 转化且必须有负责人。
- 漏斗看板展示状态数量、有效率、加微率、MQL 转化率、SQL 转化率。
- 异常线索提醒：有效/加微/跟进中/MQL 线索超过 48 小时未跟进。
- 模拟外呼系统和企微/SCRM 回调。
- 规则版自然语言查询，例如“本周哪个渠道 SQL 转化率最高？”。
- 简单角色切换：主管看全部，销售只看自己的线索。
- Docker 一键启动。

## 技术栈

- 后端：Java 21、Spring Boot 3、Spring Web、Spring Data JPA、H2 文件数据库。
- 前端：Vue 3、Vite、Element Plus、Axios。
- 构建：Maven、npm。
- 部署：Docker Compose。

说明：计划中优先考虑 SQLite，但 Spring Boot/JPA 与 SQLite 的方言、自增和兼容成本较高。本项目采用 H2 文件库，仍然满足本地持久化、schema 交付和一键启动要求。

## 目录结构

```text
lead-mql-sql-mvp/
  backend/                 Spring Boot 后端
  frontend/                Vue 前端
  docs/                    业务设计、AI 使用说明、演示脚本、接口说明
  schema/schema.sql        数据库表结构说明
  data/sample_leads.csv    CSV 导入样例
  docker-compose.yml       Docker 一键启动
  start-backend.bat        Windows 后端启动脚本
  start-frontend.bat       Windows 前端启动脚本
```

## 本地启动

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

H2 控制台：`http://localhost:8080/h2-console`

H2 连接信息：

```text
JDBC URL: jdbc:h2:file:./data/lead_mql_sql
User Name: sa
Password: 留空
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

也可以直接双击根目录下：

```text
start-backend.bat
start-frontend.bat
```

## Docker 启动

```bash
docker-compose up --build
```

启动后访问：

```text
http://localhost:5173
```

Docker 中前端 Nginx 会把 `/api` 代理到后端服务。

## 模拟数据

首次启动后，系统会自动初始化：

- 销售：张三、李四。
- 主管：王主管。
- 6 条不同阶段的模拟线索，包括新线索、有效线索、已加微、MQL、SQL、无效线索。

CSV 导入样例位于：

```text
data/sample_leads.csv
```

## 推荐演示流程

1. 打开线索工作台，展示列表、筛选、角色切换。
2. 新增一条线索。
3. 导入 `data/sample_leads.csv`，说明手机号去重。
4. 给线索分配销售负责人。
5. 通过三方回调模拟外呼结果，或手动修改状态为有效。
6. 模拟企微/SCRM 加微成功。
7. 添加跟进记录。
8. 标记 MQL，再标记 SQL。
9. 打开详情抽屉，展示跟进记录、状态历史、审计日志。
10. 打开漏斗看板、异常提醒、自然语言查询。

完整话术见 `docs/DEMO_SCRIPT.md`。

## 常用 API

- `GET /api/leads`：线索列表。
- `POST /api/leads`：新增线索。
- `PATCH /api/leads/{id}/status`：修改状态。
- `PATCH /api/leads/{id}/assign`：分配负责人。
- `POST /api/leads/{id}/follow-ups`：新增跟进记录。
- `POST /api/leads/import`：CSV 导入。
- `GET /api/dashboard/funnel`：漏斗统计。
- `GET /api/dashboard/anomalies`：异常线索。
- `POST /api/callbacks/call-result`：外呼回调。
- `POST /api/callbacks/wechat-result`：企微/SCRM 回调。
- `POST /api/query/natural-language`：自然语言查询。

更详细说明见 `docs/API.md`。

## 测试与构建

后端测试：

```bash
cd backend
mvn test
```

后端打包：

```bash
cd backend
mvn package
```

前端构建：

```bash
cd frontend
npm run build
```

## 交付说明

本仓库已包含：

- 可运行 MVP Demo。
- README。
- 业务设计文档。
- 数据库 schema。
- AI-coding 使用说明。
- 演示脚本。
- CSV 样例。
- Docker Compose。

提交前请把本文档和 `docs/业务设计文档.md`、`docs/AI_CODING_USAGE.md` 中的“姓名/学号/联系方式：待补充”替换为你的真实信息。

## 本轮加固说明

- 后端不再信任前端传入的角色或 `ownerId` 来决定权限。线索列表和详情接口要求请求头 `X-User-Id`，后端会查询该用户角色；销售只能读取自己负责的线索，主管可以读取全量。
- 状态流转改为白名单式状态机，禁止 `NEW -> MQL`、`SQL -> NEW`、`WECHAT_FAILED -> MQL` 等不合理跳转。
- 自然语言查询会识别“本周”，本周渠道 SQL 转化率按本周创建线索和本周 SQL 时间计算。
- 线索实体增加 `@Version` 乐观锁字段，用于处理并发状态变更。
- 前端已拆出 `DashboardPanel`、`QueryPanel`、`AnomalyPanel`，避免全部逻辑堆在 `App.vue`。

## 提交前自查

我已按原作业要求整理逐项验收表，见 `docs/SUBMISSION_CHECKLIST.md`。提交前建议最后确认：
- 文档里的“姓名/学号/联系方式待补充”已经替换为本人信息。
- 最终提交压缩包使用 `D:\lead-mql-sql-mvp.zip`。
- 如需要录屏，按 `docs/DEMO_SCRIPT.md` 录制 3-5 分钟演示视频。
