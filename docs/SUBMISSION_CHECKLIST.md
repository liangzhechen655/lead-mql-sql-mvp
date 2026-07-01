# 提交自查清单

本文用于交付前逐项核对作业要求，方便面试官快速定位实现位置。

## 1. 必交材料

| 要求 | 完成情况 | 对应文件/位置 |
| --- | --- | --- |
| 可运行 MVP Demo | 已完成 | `backend/`、`frontend/`、`README.md` |
| 业务设计文档 | 已完成 | `docs/业务设计文档.md` |
| AI-coding 使用说明 | 已完成 | `docs/AI_CODING_USAGE.md` |
| 数据库 schema | 已完成 | `schema/schema.sql` |
| 启动说明 | 已完成 | `README.md` |
| 演示脚本 | 已完成 | `docs/DEMO_SCRIPT.md` |
| 最终压缩包 | 已完成 | `D:\lead-mql-sql-mvp.zip` |

## 2. MVP 基础功能

| 功能点 | 完成情况 | 说明 |
| --- | --- | --- |
| 新增线索 | 已完成 | `POST /api/leads`，前端“新增线索”弹窗 |
| CSV 导入线索 | 已完成 | `POST /api/leads/import`，按手机号去重 |
| 线索列表与筛选 | 已完成 | 支持状态、来源、负责人、关键词筛选 |
| 外呼清洗 | 已完成 | 状态支持 `PENDING_CALL`、`CALLED_CONNECTED`、`CALLED_NOT_CONNECTED`、`VALID`、`INVALID` |
| 加微承接 | 已完成 | 状态支持 `PENDING_WECHAT`、`WECHAT_ADDED`、`WECHAT_FAILED` |
| 销售分配 | 已完成 | `PATCH /api/leads/{id}/assign` |
| 跟进记录 | 已完成 | `POST /api/leads/{id}/follow-ups`，自动更新最近跟进时间 |
| MQL/SQL 转化 | 已完成 | `PATCH /api/leads/{id}/status`，自动记录 MQL/SQL 时间 |
| 漏斗统计 | 已完成 | `GET /api/dashboard/funnel`，含数量与转化率 |
| 详情审计 | 已完成 | 详情抽屉展示状态历史、跟进记录、操作日志 |

## 3. 加分项

| 加分项 | 完成情况 | 说明 |
| --- | --- | --- |
| 自然语言查询 | 已完成 | 规则解析常见业务问题，支持“本周哪个渠道 SQL 转化率最高” |
| 异常线索提醒 | 已完成 | 查询 48 小时未跟进的有效线索 |
| CSV 导入与去重 | 已完成 | 手机号去重，返回新增/重复/失败数量 |
| 模拟三方回调 | 已完成 | 独立候选线索池，外呼结果回调、企微/SCRM 加微结果回调 |
| 简单权限 | 已完成 | `X-User-Id` 模拟登录；主管看全部，销售只能读自己的线索 |
| 操作审计 | 已完成 | 状态变更、分配、跟进、回调均写入操作日志 |
| Docker 一键启动 | 已完成 | `docker-compose up --build` |

## 4. 质量加固

| 风险点 | 修正方式 |
| --- | --- |
| 前端角色切换是假权限 | 后端根据 `X-User-Id` 查询真实角色并强制过滤 |
| 状态跳转过宽 | 后端使用白名单状态机，只允许声明路径 |
| “本周”自然语言查询不准确 | QueryService 解析本周时间窗口，按本周创建和本周 SQL 时间计算 |
| 测试硬编码用户 ID | 测试通过用户名动态查询销售/主管 |
| CSV header 识别脆弱 | 支持 phone/mobile/tel/电话/手机/姓名 等常见表头 |
| 手动新增误写 importedAt | 仅 CSV 导入设置 `importedAt` |
| 并发状态变更风险 | `SalesLead` 增加 `@Version` 乐观锁 |
| 前端单文件过大 | 拆出 DashboardPanel、AnomalyPanel、QueryPanel 组件 |

## 5. 验证命令

```bash
cd D:\lead-mql-sql-mvp\backend
mvn test
mvn package

cd D:\lead-mql-sql-mvp\frontend
npm run build
```

## 6. 仍需人工补充

| 项目 | 说明 |
| --- | --- |
| 个人信息 | 文档中保留“姓名/学号/联系方式待补充”，提交前需要替换 |
| 演示视频 | 作业通常建议录制，仓库已提供 `docs/DEMO_SCRIPT.md`，可按脚本录 3-5 分钟视频 |
| 真实登录 | 本 MVP 按作业范围使用 `X-User-Id` 模拟登录；生产环境应替换为 session/JWT |
