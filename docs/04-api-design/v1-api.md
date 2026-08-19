# API v1 接口文档

## 1. 文档说明

本文是前端与后端进行 REST JSON 联调的唯一接口契约。产品行为以 `docs/01-requirements-specification.md` 为准，模块边界以 `docs/02-architecture-design.md` 为准，数据约束以 `docs/03-database-design.md` 为准。

| 契约范围 | 设计状态 | 实现状态 |
| --- | --- | --- |
| API v1 公共规则 | 已确认 | 尚未实现 |
| 账本、成员、消费、结算接口 | 已确认 | 尚未实现 |

文中的请求和响应只用于说明契约，不是 Mock、种子数据或已实现接口。示例统一使用相对路径，因为实际主机和端口尚未完成运行验证。

## 2. 公共约定

### 2.1 请求与响应格式

- 路径前缀：`/api/v1`。
- 有请求体的接口使用 `Content-Type: application/json`，字符编码为 UTF-8。
- 客户端通过 `Accept: application/json` 声明接收 JSON。
- 首版没有登录和鉴权，不使用认证请求头。
- 创建成功返回 HTTP 201，其他成功操作返回 HTTP 200。
- 失败使用真实 HTTP 状态码，并保持统一 JSON 响应结构。

统一响应字段：

| 字段 | 类型 | 是否固定存在 | 说明 |
| --- | --- | --- | --- |
| `code` | string | 是 | 稳定结果码；成功统一为 `SUCCESS` |
| `message` | string | 是 | 可直接展示的中文提示，不作为前端分支判断依据 |
| `data` | object、array 或 null | 是 | 成功时承载接口结果；删除成功和失败时为 null |

```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": null
}
```

### 2.2 数据格式

- ID 使用仅包含十进制数字的字符串，例如 `"17"`，不得包含符号、小数或指数。
- 金额使用人民币元字符串。请求接受 `"100"`、`"100.5"`、`"100.50"`，必须大于 0、最多两位小数且不接受指数；响应统一保留两位小数。
- 业务日期使用 `YYYY-MM-DD`，例如 `"2026-08-18"`，并且必须是真实日期。
- 创建和更新时间使用无时区偏移的本地 ISO 时间，精确到秒，例如 `"2026-08-18T10:00:00"`。
- 账本名称、成员姓名和消费名称由后端去除首尾空格后校验，响应只返回规范化后的值。

### 2.3 分页

账本和消费列表支持 `page`、`size` 查询参数。`page` 从 1 开始且默认为 1；`size` 默认为 20，允许范围为 1～100。

分页结果的 `data` 字段固定包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `items` | array | 当前页数据；无数据时为空数组 |
| `page` | number | 当前页码 |
| `size` | number | 当前页容量 |
| `totalElements` | number | 记录总数 |
| `totalPages` | number | 总页数；无数据时为 0 |

### 2.4 公共错误码

| HTTP 状态 | code | 说明 |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | JSON、路径 ID、分页、文本、金额、日期或参与人集合不符合契约 |
| 400 | `INVALID_MEMBER_REFERENCE` | 消费中的付款人或参与人不存在于当前账本 |
| 404 | `LEDGER_NOT_FOUND` | 账本不存在 |
| 404 | `MEMBER_NOT_FOUND` | 成员不存在或不属于路径账本 |
| 404 | `EXPENSE_NOT_FOUND` | 消费不存在或不属于路径账本 |
| 409 | `MEMBER_NAME_CONFLICT` | 同一账本内的成员姓名重复 |
| 409 | `MEMBER_IN_USE` | 成员仍被消费引用，不能删除 |
| 500 | `INTERNAL_ERROR` | 未预期的服务端错误 |

所有接口都可能返回 `INTERNAL_ERROR`。其 `message` 不得泄露堆栈、SQL、本地路径或其他内部实现信息，后续接口小节不再重复列出该错误。

## 3. 接口索引

| 分组 | 接口 | 方法与路径 |
| --- | --- | --- |
| 账本 | 查看账本列表 | `GET /api/v1/ledgers` |
| 账本 | 创建账本 | `POST /api/v1/ledgers` |
| 账本 | 读取账本详情 | `GET /api/v1/ledgers/{ledgerId}` |
| 账本 | 修改账本名称 | `PATCH /api/v1/ledgers/{ledgerId}` |
| 账本 | 删除账本 | `DELETE /api/v1/ledgers/{ledgerId}` |
| 成员 | 查看成员列表 | `GET /api/v1/ledgers/{ledgerId}/members` |
| 成员 | 添加成员 | `POST /api/v1/ledgers/{ledgerId}/members` |
| 成员 | 修改成员姓名 | `PATCH /api/v1/ledgers/{ledgerId}/members/{memberId}` |
| 成员 | 删除成员 | `DELETE /api/v1/ledgers/{ledgerId}/members/{memberId}` |
| 消费 | 查看消费列表 | `GET /api/v1/ledgers/{ledgerId}/expenses` |
| 消费 | 新增消费 | `POST /api/v1/ledgers/{ledgerId}/expenses` |
| 消费 | 修改消费 | `PUT /api/v1/ledgers/{ledgerId}/expenses/{expenseId}` |
| 消费 | 删除消费 | `DELETE /api/v1/ledgers/{ledgerId}/expenses/{expenseId}` |
| 结算 | 查看结算结果 | `GET /api/v1/ledgers/{ledgerId}/settlement` |

## 4. 账本接口

### 4.1 查看账本列表

#### 接口说明

分页读取全部账本，固定按账本 ID 降序排列，新创建的账本优先显示。

- 请求方式：`GET`
- 请求路径：`/api/v1/ledgers`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | query | number | 否 | 页码，默认 1，必须大于等于 1 |
| `size` | query | number | 否 | 每页数量，默认 20，范围 1～100 |

#### 请求示例

```http
GET /api/v1/ledgers?page=1&size=20 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `items[].id` | string | 账本 ID |
| `items[].name` | string | 账本名称 |
| `items[].createdAt` | string | 创建时间 |
| `items[].updatedAt` | string | 最后修改时间 |
| `page`、`size` | number | 当前分页参数 |
| `totalElements`、`totalPages` | number | 总记录数和总页数 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": {
    "items": [
      {"id": "1", "name": "周末聚餐", "createdAt": "2026-08-18T10:00:00", "updatedAt": "2026-08-18T10:00:00"}
    ],
    "page": 1,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`。

```json
{
  "code": "VALIDATION_ERROR",
  "message": "page 必须大于等于 1",
  "data": null
}
```

### 4.2 创建账本

#### 接口说明

创建一个独立账本。不同账本允许使用相同名称。

- 请求方式：`POST`
- 请求路径：`/api/v1/ledgers`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `name` | body | string | 是 | 去除首尾空格后长度为 1～100 |

#### 请求示例

```http
POST /api/v1/ledgers HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "name": "周末聚餐"
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 新账本 ID |
| `name` | string | 规范化后的名称 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 最后修改时间，创建时与创建时间相同 |

#### 成功响应

HTTP 201：

```json
{
  "code": "SUCCESS",
  "message": "账本创建成功",
  "data": {
    "id": "1",
    "name": "周末聚餐",
    "createdAt": "2026-08-18T10:00:00",
    "updatedAt": "2026-08-18T10:00:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`。

```json
{
  "code": "VALIDATION_ERROR",
  "message": "账本名称不能为空",
  "data": null
}
```

### 4.3 读取账本详情

#### 接口说明

读取一个账本的基本信息，用于进入账本工作台。响应不嵌入成员、消费或结算结果。

- 请求方式：`GET`
- 请求路径：`/api/v1/ledgers/{ledgerId}`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |

#### 请求示例

```http
GET /api/v1/ledgers/1 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 账本 ID |
| `name` | string | 账本名称 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 最后修改时间 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": {
    "id": "1",
    "name": "周末聚餐",
    "createdAt": "2026-08-18T10:00:00",
    "updatedAt": "2026-08-18T10:00:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

### 4.4 修改账本名称

#### 接口说明

只修改指定账本的名称，不影响其成员、消费和结算结果。

- 请求方式：`PATCH`
- 请求路径：`/api/v1/ledgers/{ledgerId}`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `name` | body | string | 是 | 去除首尾空格后长度为 1～100 |

#### 请求示例

```http
PATCH /api/v1/ledgers/1 HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "name": "周末聚餐"
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 账本 ID |
| `name` | string | 修改后的规范化名称 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 本次修改时间 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "账本名称修改成功",
  "data": {
    "id": "1",
    "name": "周末聚餐",
    "createdAt": "2026-08-18T10:00:00",
    "updatedAt": "2026-08-18T10:10:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

### 4.5 删除账本

#### 接口说明

硬删除账本，并按数据库删除语义清理其成员、消费及参与关系。二次确认由前端完成，API 不接收确认参数。

- 请求方式：`DELETE`
- 请求路径：`/api/v1/ledgers/{ledgerId}`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |

#### 请求示例

```http
DELETE /api/v1/ledgers/1 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data | 类型 | 说明 |
| --- | --- | --- |
| `null` | null | 删除成功不返回业务对象 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "账本删除成功",
  "data": null
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

## 5. 成员接口

### 5.1 查看成员列表

#### 接口说明

读取账本中的全部成员，不分页，并按成员 ID 升序排列。

- 请求方式：`GET`
- 请求路径：`/api/v1/ledgers/{ledgerId}/members`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |

#### 请求示例

```http
GET /api/v1/ledgers/1/members HTTP/1.1
Accept: application/json
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `[].id` | string | 成员 ID |
| `[].name` | string | 成员姓名 |
| `[].createdAt` | string | 创建时间 |
| `[].updatedAt` | string | 最后修改时间 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": [
    {"id": "1", "name": "甲", "createdAt": "2026-08-18T10:05:00", "updatedAt": "2026-08-18T10:05:00"},
    {"id": "2", "name": "乙", "createdAt": "2026-08-18T10:06:00", "updatedAt": "2026-08-18T10:06:00"},
    {"id": "3", "name": "丙", "createdAt": "2026-08-18T10:07:00", "updatedAt": "2026-08-18T10:07:00"}
  ]
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

### 5.2 添加成员

#### 接口说明

向账本添加一名成员。同一账本内，去除首尾空格后的姓名区分大小写且不能重复。

- 请求方式：`POST`
- 请求路径：`/api/v1/ledgers/{ledgerId}/members`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `name` | body | string | 是 | 去除首尾空格后长度为 1～50 |

#### 请求示例

```http
POST /api/v1/ledgers/1/members HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "name": "丙"
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 新成员 ID |
| `name` | string | 规范化后的姓名 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 最后修改时间，创建时与创建时间相同 |

#### 成功响应

HTTP 201：

```json
{
  "code": "SUCCESS",
  "message": "成员添加成功",
  "data": {
    "id": "3",
    "name": "丙",
    "createdAt": "2026-08-18T10:07:00",
    "updatedAt": "2026-08-18T10:07:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`；HTTP 409 `MEMBER_NAME_CONFLICT`。

```json
{
  "code": "MEMBER_NAME_CONFLICT",
  "message": "该账本中已存在同名成员",
  "data": null
}
```

### 5.3 修改成员姓名

#### 接口说明

修改指定成员的姓名。消费响应和结算响应随后使用修改后的当前姓名。

- 请求方式：`PATCH`
- 请求路径：`/api/v1/ledgers/{ledgerId}/members/{memberId}`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `memberId` | path | string | 是 | 当前账本中的成员 ID |
| `name` | body | string | 是 | 去除首尾空格后长度为 1～50 |

#### 请求示例

```http
PATCH /api/v1/ledgers/1/members/1 HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "name": "甲"
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 成员 ID |
| `name` | string | 修改后的规范化姓名 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 本次修改时间 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "成员姓名修改成功",
  "data": {
    "id": "1",
    "name": "甲",
    "createdAt": "2026-08-18T10:05:00",
    "updatedAt": "2026-08-18T10:15:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`、`MEMBER_NOT_FOUND`；HTTP 409 `MEMBER_NAME_CONFLICT`。

```json
{
  "code": "MEMBER_NOT_FOUND",
  "message": "成员不存在",
  "data": null
}
```

### 5.4 删除成员

#### 接口说明

删除未被任何消费作为付款人或参与人引用的成员。被引用的成员必须先修改或删除相关消费。

- 请求方式：`DELETE`
- 请求路径：`/api/v1/ledgers/{ledgerId}/members/{memberId}`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `memberId` | path | string | 是 | 当前账本中的成员 ID |

#### 请求示例

```http
DELETE /api/v1/ledgers/1/members/1 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data | 类型 | 说明 |
| --- | --- | --- |
| `null` | null | 删除成功不返回业务对象 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "成员删除成功",
  "data": null
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`、`MEMBER_NOT_FOUND`；HTTP 409 `MEMBER_IN_USE`。

```json
{
  "code": "MEMBER_IN_USE",
  "message": "该成员已被消费引用，请先修改或删除相关消费",
  "data": null
}
```

## 6. 消费接口

### 6.1 查看消费列表

#### 接口说明

分页读取账本的消费明细。列表按消费日期降序、消费 ID 降序稳定排列，每项都包含编辑所需的完整数据。

- 请求方式：`GET`
- 请求路径：`/api/v1/ledgers/{ledgerId}/expenses`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `page` | query | number | 否 | 页码，默认 1，必须大于等于 1 |
| `size` | query | number | 否 | 每页数量，默认 20，范围 1～100 |

#### 请求示例

```http
GET /api/v1/ledgers/1/expenses?page=1&size=20 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `items[].id` | string | 消费 ID |
| `items[].title` | string | 消费名称 |
| `items[].amount` | string | 两位小数的人民币元金额 |
| `items[].expenseDate` | string | 消费日期 |
| `items[].payer` | object | 当前付款成员的 `id`、`name` |
| `items[].participants` | array | 参与成员的 `id`、`name`，按成员 ID 升序 |
| `items[].createdAt`、`updatedAt` | string | 创建和最后修改时间 |
| `page`、`size` | number | 当前分页参数 |
| `totalElements`、`totalPages` | number | 总记录数和总页数 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": {
    "items": [
      {"id": "2", "title": "饮料", "amount": "40.00", "expenseDate": "2026-08-18", "payer": {"id": "2", "name": "乙"}, "participants": [{"id": "2", "name": "乙"}, {"id": "3", "name": "丙"}], "createdAt": "2026-08-18T11:10:00", "updatedAt": "2026-08-18T11:10:00"},
      {"id": "1", "title": "聚餐餐费", "amount": "100.00", "expenseDate": "2026-08-18", "payer": {"id": "1", "name": "甲"}, "participants": [{"id": "1", "name": "甲"}, {"id": "2", "name": "乙"}, {"id": "3", "name": "丙"}], "createdAt": "2026-08-18T11:00:00", "updatedAt": "2026-08-18T11:00:00"}
    ],
    "page": 1, "size": 20, "totalElements": 2, "totalPages": 1
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

### 6.2 新增消费

#### 接口说明

新增一笔消费及其参与关系。付款人可以不在参与人中，但所有引用成员都必须属于当前账本。

- 请求方式：`POST`
- 请求路径：`/api/v1/ledgers/{ledgerId}/expenses`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `title` | body | string | 是 | 去除首尾空格后长度为 1～200 |
| `amount` | body | string | 是 | 大于 0、最多两位小数的人民币元金额 |
| `expenseDate` | body | string | 是 | `YYYY-MM-DD` 格式的真实日期 |
| `payerMemberId` | body | string | 是 | 当前账本中的付款成员 ID |
| `participantMemberIds` | body | array<string> | 是 | 至少一项、不得重复，成员均属于当前账本 |

#### 请求示例

```http
POST /api/v1/ledgers/1/expenses HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "title": "聚餐餐费",
  "amount": "100.00",
  "expenseDate": "2026-08-18",
  "payerMemberId": "1",
  "participantMemberIds": ["1", "2", "3"]
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 新消费 ID |
| `title`、`amount`、`expenseDate` | string | 规范化名称、两位小数金额和消费日期 |
| `payer` | object | 当前付款成员的 `id`、`name` |
| `participants` | array | 参与成员的 `id`、`name`，按成员 ID 升序 |
| `createdAt`、`updatedAt` | string | 创建和最后修改时间 |

#### 成功响应

HTTP 201：

```json
{
  "code": "SUCCESS",
  "message": "消费创建成功",
  "data": {
    "id": "1", "title": "聚餐餐费", "amount": "100.00",
    "expenseDate": "2026-08-18",
    "payer": {"id": "1", "name": "甲"},
    "participants": [{"id": "1", "name": "甲"}, {"id": "2", "name": "乙"}, {"id": "3", "name": "丙"}],
    "createdAt": "2026-08-18T11:00:00",
    "updatedAt": "2026-08-18T11:00:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`、`INVALID_MEMBER_REFERENCE`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "INVALID_MEMBER_REFERENCE",
  "message": "付款人或参与人不存在于当前账本",
  "data": null
}
```

### 6.3 修改消费

#### 接口说明

完整替换一笔消费的可编辑字段，并在同一事务中更新参与关系。请求必须提交全部字段。

- 请求方式：`PUT`
- 请求路径：`/api/v1/ledgers/{ledgerId}/expenses/{expenseId}`
- 内容类型：`application/json`

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `expenseId` | path | string | 是 | 当前账本中的消费 ID |
| `title` | body | string | 是 | 去除首尾空格后长度为 1～200 |
| `amount` | body | string | 是 | 大于 0、最多两位小数的人民币元金额 |
| `expenseDate` | body | string | 是 | `YYYY-MM-DD` 格式的真实日期 |
| `payerMemberId` | body | string | 是 | 当前账本中的付款成员 ID |
| `participantMemberIds` | body | array<string> | 是 | 至少一项、不得重复，成员均属于当前账本 |

#### 请求示例

```http
PUT /api/v1/ledgers/1/expenses/1 HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "title": "聚餐餐费",
  "amount": "100.00",
  "expenseDate": "2026-08-18",
  "payerMemberId": "1",
  "participantMemberIds": ["1", "2", "3"]
}
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 消费 ID |
| `title`、`amount`、`expenseDate` | string | 修改后的名称、金额和日期 |
| `payer` | object | 当前付款成员的 `id`、`name` |
| `participants` | array | 参与成员的 `id`、`name`，按成员 ID 升序 |
| `createdAt`、`updatedAt` | string | 创建时间和本次修改时间 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "消费修改成功",
  "data": {
    "id": "1", "title": "聚餐餐费", "amount": "100.00",
    "expenseDate": "2026-08-18",
    "payer": {"id": "1", "name": "甲"},
    "participants": [{"id": "1", "name": "甲"}, {"id": "2", "name": "乙"}, {"id": "3", "name": "丙"}],
    "createdAt": "2026-08-18T11:00:00",
    "updatedAt": "2026-08-18T11:20:00"
  }
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`、`INVALID_MEMBER_REFERENCE`；HTTP 404 `LEDGER_NOT_FOUND`、`EXPENSE_NOT_FOUND`。

```json
{
  "code": "EXPENSE_NOT_FOUND",
  "message": "消费不存在",
  "data": null
}
```

### 6.4 删除消费

#### 接口说明

硬删除指定消费，并级联删除其参与关系。删除后结算结果在下次查询时自动变化。

- 请求方式：`DELETE`
- 请求路径：`/api/v1/ledgers/{ledgerId}/expenses/{expenseId}`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |
| `expenseId` | path | string | 是 | 当前账本中的消费 ID |

#### 请求示例

```http
DELETE /api/v1/ledgers/1/expenses/1 HTTP/1.1
Accept: application/json
```

#### 响应参数

| data | 类型 | 说明 |
| --- | --- | --- |
| `null` | null | 删除成功不返回业务对象 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "消费删除成功",
  "data": null
}
```

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`、`EXPENSE_NOT_FOUND`。

```json
{
  "code": "EXPENSE_NOT_FOUND",
  "message": "消费不存在",
  "data": null
}
```

## 7. 结算接口

### 7.1 查看结算结果

#### 接口说明

根据当前成员、消费和参与关系实时计算每人的实付、应承担、净余额及转账建议，不读取或保存结算记录。

- 请求方式：`GET`
- 请求路径：`/api/v1/ledgers/{ledgerId}/settlement`
- 请求体：无

#### 请求参数

| 参数名 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ledgerId` | path | string | 是 | 仅包含十进制数字的账本 ID |

#### 请求示例

```http
GET /api/v1/ledgers/1/settlement HTTP/1.1
Accept: application/json
```

#### 响应参数

| data 字段 | 类型 | 说明 |
| --- | --- | --- |
| `balances` | array | 全部成员余额，按成员 ID 升序 |
| `balances[].member` | object | 成员的 `id`、`name` |
| `balances[].paidAmount` | string | 该成员实付金额，非负 |
| `balances[].owedAmount` | string | 该成员应承担金额，非负 |
| `balances[].netBalance` | string | 实付减应承担，允许为负 |
| `transfers` | array | 按稳定算法生成的转账建议 |
| `transfers[].fromMember` | object | 应付款成员的 `id`、`name` |
| `transfers[].toMember` | object | 应收款成员的 `id`、`name` |
| `transfers[].amount` | string | 大于 0 的转账金额 |

#### 成功响应

HTTP 200：

```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": {
    "balances": [
      {"member": {"id": "1", "name": "甲"}, "paidAmount": "100.00", "owedAmount": "33.34", "netBalance": "66.66"},
      {"member": {"id": "2", "name": "乙"}, "paidAmount": "40.00", "owedAmount": "53.33", "netBalance": "-13.33"},
      {"member": {"id": "3", "name": "丙"}, "paidAmount": "0.00", "owedAmount": "53.33", "netBalance": "-53.33"}
    ],
    "transfers": [
      {"fromMember": {"id": "3", "name": "丙"}, "toMember": {"id": "1", "name": "甲"}, "amount": "53.33"},
      {"fromMember": {"id": "2", "name": "乙"}, "toMember": {"id": "1", "name": "甲"}, "amount": "13.33"}
    ]
  }
}
```

无成员时 `balances`、`transfers` 都为空；有成员但无消费时，每人的三个金额均为 `"0.00"`，`transfers` 为空。

#### 失败响应

可能错误：HTTP 400 `VALIDATION_ERROR`；HTTP 404 `LEDGER_NOT_FOUND`。

```json
{
  "code": "LEDGER_NOT_FOUND",
  "message": "账本不存在",
  "data": null
}
```

#### 结算顺序

1. 每笔消费不能整除的尾差按参与成员 ID 升序分配。
2. 欠款与应收成员分别按剩余金额绝对值降序排列，同额时按成员 ID 升序排列。
3. 每次在两组首位成员间生成一笔较小剩余金额的转账，更新后继续排序。
4. 输出顺序即生成顺序；最终所有净余额必须精确结清到分。

该策略生成稳定且较紧凑的建议，但不承诺数学意义上的全局最少转账次数。

## 8. 契约边界

- 不提供消费详情、成员选项、手动重算、批量操作、Mock、演示或健康接口。
- 不在 API 中暴露数据库字段名、整数分存储方式或内部实体。
- 不提供币种字段；所有金额固定为人民币。
- 不提供并发版本号、软删除状态、历史版本或实际转账完成状态。
- API 只有在对应真实业务切片完成并通过验收后，才能逐项将实现状态更新为“已实现”。
