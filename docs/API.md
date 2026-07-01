# API 说明

后端基础地址：`http://localhost:8080/api`

线索列表和详情接口需要模拟登录请求头：

```http
X-User-Id: 1
```

后端会根据该用户在 `sales_user` 表中的角色强制过滤数据。销售用户只能看到自己负责的线索，主管用户可以查看全量。

## 线索

### GET /leads

查询线索列表。支持参数：

- `status`：状态，如 `NEW`、`VALID`、`MQL`、`SQL`。
- `source`：来源，如 `百度投放`。
- `ownerId`：负责人 ID。
- `keyword`：姓名、手机号、渠道关键词。

### POST /leads

新增线索。

```json
{
  "customerName": "何先生",
  "phone": "13800001001",
  "source": "百度投放",
  "channel": "百度-搜索",
  "grade": "A",
  "remark": "官网咨询"
}
```

### PATCH /leads/{id}/status

修改线索状态。关键规则：SQL 必须从 MQL 转化；MQL 前必须有跟进记录；SQL 前必须有负责人。

```json
{
  "status": "MQL",
  "reason": "完成加微和首次跟进",
  "operatorName": "张三",
  "invalidReason": ""
}
```

### PATCH /leads/{id}/assign

分配负责人。

```json
{
  "ownerId": 1,
  "operatorName": "王主管"
}
```

### POST /leads/{id}/follow-ups

新增跟进记录。

```json
{
  "operatorId": 1,
  "method": "电话",
  "content": "客户有明确兴趣，下周继续沟通",
  "nextFollowUpAt": null
}
```

### POST /leads/import

CSV 导入，`multipart/form-data`：

- `file`：CSV 文件。
- `operatorName`：操作者。

CSV 列顺序：

```text
customerName,phone,source,channel
```

## 看板

### GET /dashboard/funnel

返回总数、各状态数量、有效率、加微率、MQL 转化率、SQL 转化率和渠道 SQL 转化。

### GET /dashboard/anomalies

返回超过 48 小时未跟进的有效线索。

## 三方回调

### GET /callbacks/candidates

返回三方回调页面可选择的完整线索候选池。前端会根据外呼/企微当前回传结果，再按状态机规则过滤可操作线索。

### POST /callbacks/call-result

模拟外呼系统回调。

```json
{
  "leadId": 1,
  "connected": true,
  "valid": true,
  "invalidReason": "",
  "rawResult": "客户接通且表达兴趣"
}
```

### POST /callbacks/wechat-result

模拟企微/SCRM 加微回调。

```json
{
  "leadId": 1,
  "added": true,
  "failedReason": "",
  "externalUserId": "wm_demo_001"
}
```

## 自然语言查询

### POST /query/natural-language

```json
{
  "question": "本周哪个渠道 SQL 转化率最高？"
}
```

当前为规则匹配版本，支持：

- 渠道 SQL 转化率最高。
- 异常/超时线索数量。
- 漏斗/转化率概览。
