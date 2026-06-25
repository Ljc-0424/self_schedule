# 智能任务管家 - 后端API接口文档

---

## 全局说明

### 基础信息
- **Base URL**: `https://your-domain.com/api`
- **认证方式**: JWT Bearer Token
- **请求头**: `Authorization: Bearer <token>`
- **Content-Type**: `application/json`

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 分页响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [],
    "pageNum": 1,
    "pageSize": 20,
    "total": 100,
    "pages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 错误码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/token过期 |
| 403 | 无权限/账号被封禁 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 一、认证管理 (AuthController)

### 1.1 用户注册
```
POST /api/auth
```

**请求参数**:
```json
{
  "username": "string",    // 用户名，5-50字符
  "password": "string",    // 密码，6-50字符
  "email": "string"        // 邮箱，必须有效且唯一
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "jwt_token_string",
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com",
      "avatarUrl": "/api/avatar/xxx.jpg",
      "role": 0,
      "status": 0
    }
  }
}
```

---

### 1.2 用户登录
```
POST /api/auth/login
```

**请求参数**:
```json
{
  "account": "string",    // 用户名或邮箱
  "password": "string"    // 密码
}
```

**响应**:
```json
// 成功
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "jwt_token_string",
    "user": { ... }
  }
}

// 账号被封禁
{
  "code": 403,
  "message": "账号已被封禁",
  "data": {
    "status": 1,
    "banReason": "违规操作",
    "banTime": "2024-01-15T10:00:00",
    "banEndTime": null
  }
}
```

---

### 1.3 用户注销
```
POST /api/auth/logout
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "message": "注销成功",
  "data": null
}
```

---

### 1.4 检查用户名是否存在
```
GET /api/auth/check-username?username=testuser
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "exists": true,
    "username": "testuser"
  }
}
```

---

### 1.5 检查邮箱是否存在
```
GET /api/auth/check-email?email=test@example.com
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "exists": false,
    "email": "test@example.com"
  }
}
```

---

## 二、密码管理 (PasswordResetController)

### 2.1 发送验证码
```
POST /api/auth/forgot-password
```

**请求参数**:
```json
{
  "email": "string",      // 邮箱（二选一）
  "username": "string"    // 用户名（二选一）
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码已发送到您的邮箱",
  "data": null
}
```

**说明**: 
- 验证码有效期5分钟
- 60秒冷却时间

---

### 2.2 验证验证码
```
POST /api/auth/verify-code
```

**请求参数**:
```json
{
  "email": "string",    // 邮箱
  "code": "string"      // 6位验证码
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码正确",
  "data": null
}
```

---

### 2.3 重置密码
```
POST /api/auth/reset-password
```

**请求参数**:
```json
{
  "email": "string",        // 邮箱
  "code": "string",         // 验证码
  "newPassword": "string"   // 新密码，6-50字符
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

### 2.4 修改密码
```
POST /api/auth/change-password
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "oldPassword": "string",  // 旧密码
  "newPassword": "string"   // 新密码，6-50字符
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

## 三、用户管理 (UserController)

### 3.1 获取用户信息
```
GET /api/user/info
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "avatarUrl": "/api/avatar/xxx.jpg",
    "settings": "{}",
    "isActive": 1,
    "status": 0,
    "role": 0,
    "lastLoginTime": "2024-01-15T10:00:00",
    "occupation": "学生",
    "phone": "13800138000",
    "gender": "男",
    "city": "北京",
    "hobbies": "编程,阅读",
    "bio": "热爱编程",
    "weChatWebhookUrl": "",
    "aiApiKey": "",
    "dailyFocusGoal": 120,
    "minEffectiveDuration": 5,
    "createdTime": "2024-01-01T00:00:00",
    "updatedTime": "2024-01-15T10:00:00",
    "birthday": "2000-01-01"
  }
}
```

---

### 3.2 更新用户信息
```
PUT /api/user/info
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "nickname": "string",           // 昵称
  "email": "string",              // 邮箱
  "avatarUrl": "string",          // 头像URL
  "settings": "string",           // 设置JSON
  "occupation": "string",         // 职业
  "birthday": "string",           // 生日
  "phone": "string",              // 电话
  "gender": "string",             // 性别
  "city": "string",               // 城市
  "hobbies": "string",            // 爱好
  "bio": "string",                // 简介
  "weChatWebhookUrl": "string",   // 微信Webhook
  "aiApiKey": "string",           // AI API Key
  "dailyFocusGoal": 120,          // 每日专注目标(分钟)
  "minEffectiveDuration": 5       // 最小有效时长(分钟)
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": { ... }  // 更新后的用户信息
}
```

---

## 四、头像管理 (AvatarController)

### 4.1 上传头像
```
POST /api/avatar/upload
```

**请求头**: `Authorization: Bearer <token>`

**请求类型**: `multipart/form-data`

**参数**:
- `file`: 图片文件 (jpg/jpeg/png/gif/bmp/webp，最大10MB)

**响应**:
```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatarUrl": "/api/avatar/20240115_xxx.jpg",
    "message": "上传成功"
  }
}
```

---

### 4.2 获取头像
```
GET /api/avatar/{filename}
```

**参数**: 
- `filename`: 文件名

**响应**: 图片二进制流 (带24小时缓存)

---

### 4.3 删除头像
```
DELETE /api/avatar/delete
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "message": "头像删除成功",
  "data": null
}
```

---

### 4.4 测试接口
```
GET /api/avatar/test
```

**响应**:
```json
{
  "code": 200,
  "message": "Avatar API is working",
  "data": null
}
```

---

## 五、任务管理 (TaskController)

### 5.1 获取任务分类列表
```
GET /api/tasks/categories
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": ["工作", "学习", "生活", "运动"]
}
```

---

### 5.2 查询任务列表
```
GET /api/tasks
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | String | 否 | 分类筛选 |
| statusList | List<Integer> | 否 | 状态筛选 (0待办/1进行中/2已完成/3已取消/4已存档) |
| priorityList | List<Integer> | 否 | 优先级筛选 (0低/1中/2高) |
| deadlineBefore | String | 否 | 截止时间早于 (ISO日期) |
| deadlineAfter | String | 否 | 截止时间晚于 (ISO日期) |
| keyword | String | 否 | 关键词搜索 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "data": [
      {
        "id": 1,
        "userId": 1,
        "title": "完成英语作业",
        "description": "完成第三章练习",
        "priority": 2,           // 0低/1中/2高
        "status": 0,             // 0待办/1进行中/2已完成/3已取消/4已存档
        "deadline": "2024-01-16T15:00:00",
        "remindTime": "2024-01-15T14:00:00",
        "estimatedSeconds": 3600,
        "actualSeconds": 0,
        "category": "学习",
        "tags": ["英语", "作业"],
        "reminderConfig": "{}",
        "isAiGenerated": false,
        "recurrenceRule": "",
        "recurrenceEndDate": null,
        "createdTime": "2024-01-15T10:00:00",
        "updatedTime": "2024-01-15T10:00:00",
        "completedTime": null
      }
    ],
    "pageNum": 1,
    "pageSize": 20,
    "total": 50,
    "pages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 5.3 创建任务
```
POST /api/tasks
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "title": "string",                    // 任务标题 (必填)
  "description": "string",              // 描述
  "priority": 0,                        // 优先级 (0低/1中/2高)
  "status": 0,                          // 状态 (0待办/1进行中/2已完成)
  "deadline": "2024-01-16T15:00:00",    // 截止时间
  "remindTime": "2024-01-15T14:00:00",  // 提醒时间
  "estimatedSeconds": 3600,             // 预估时长(秒)
  "category": "学习",                   // 分类
  "tags": ["英语", "作业"],              // 标签数组
  "reminderConfig": "{}",               // 提醒配置JSON
  "recurrenceRule": "FREQ=DAILY",       // 重复规则
  "recurrenceEndDate": "2024-12-31",    // 重复结束日期
  "isAiGenerated": false,               // 是否AI生成
  "subtasks": [                         // 子任务列表
    { "title": "子任务1" },
    { "title": "子任务2" }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "任务创建成功",
  "data": { ... }  // 创建的任务详情
}
```

---

### 5.4 获取任务详情
```
GET /api/tasks/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "title": "完成英语作业",
    ...
  }
}
```

---

### 5.5 更新任务
```
PUT /api/tasks/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**请求参数**:
```json
{
  "title": "string",
  "description": "string",
  "priority": 0,
  "status": 0,
  "deadline": "2024-01-16T15:00:00",
  "remindTime": "2024-01-15T14:00:00",
  "estimatedSeconds": 3600,
  "actualSeconds": 1800,
  "category": "学习",
  "tags": ["英语"],
  "reminderConfig": "{}",
  "isAiGenerated": false,
  "recurrenceRule": "",
  "recurrenceEndDate": null,
  "clearDeadline": false,      // 是否清除截止时间
  "clearRemindTime": false     // 是否清除提醒时间
}
```

**响应**:
```json
{
  "code": 200,
  "message": "任务更新成功",
  "data": { ... }
}
```

**说明**: 
- 完成任务(status=2)时自动设置完成时间
- 若为重复任务完成则自动创建下一次任务

---

### 5.6 完成任务
```
PUT /api/tasks/{id}/complete
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**响应**:
```json
{
  "code": 200,
  "message": "任务已完成",
  "data": { ... }
}
```

---

### 5.7 撤销任务状态
```
PUT /api/tasks/{id}/undo
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**响应**:
```json
{
  "code": 200,
  "message": "任务状态已撤销",
  "data": { ... }
}
```

**状态转换规则**:
- 已存档(4) → 已完成(2)
- 已取消(3) → 待办(0)
- 已完成(2) → 待办(0)

---

### 5.8 删除任务
```
DELETE /api/tasks/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**响应**:
```json
{
  "code": 200,
  "message": "任务已删除",
  "data": null
}
```

**删除规则**:
- 已完成(2) → 已存档(4)
- 待办(0)/进行中(1) → 已取消(3)
- 已取消(3)/已存档(4) → 彻底删除

---

### 5.9 彻底删除任务
```
DELETE /api/tasks/{id}/force
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 任务ID

**响应**:
```json
{
  "code": 200,
  "message": "任务已彻底删除",
  "data": null
}
```

---

## 六、子任务管理 (SubtaskController)

### 6.1 创建子任务
```
POST /api/subtasks
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `taskId`: 任务ID (必填)
- `title`: 子任务标题 (必填)

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "taskId": 1,
    "title": "子任务1",
    "completed": false,
    "createdTime": "2024-01-15T10:00:00"
  }
}
```

---

### 6.2 更新子任务
```
PUT /api/subtasks/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 子任务ID

**请求参数**:
```json
{
  "title": "新的子任务标题"
}
```

---

### 6.3 删除子任务
```
DELETE /api/subtasks/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 子任务ID

---

### 6.4 切换子任务完成状态
```
PUT /api/subtasks/{id}/complete
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 子任务ID

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "taskId": 1,
    "title": "子任务1",
    "completed": true,  // 切换后的状态
    "createdTime": "2024-01-15T10:00:00"
  }
}
```

---

### 6.5 获取任务的所有子任务
```
GET /api/subtasks/task/{taskId}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `taskId` - 任务ID

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "taskId": 1,
      "title": "子任务1",
      "completed": true
    },
    {
      "id": 2,
      "taskId": 1,
      "title": "子任务2",
      "completed": false
    }
  ]
}
```

---

### 6.6 获取子任务统计
```
GET /api/subtasks/task/{taskId}/count
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `taskId` - 任务ID

**响应**:
```json
{
  "code": 200,
  "data": {
    "total": 5,
    "completed": 3
  }
}
```

---

## 七、专注记录管理 (FocusRecordController)

### 7.1 搜索可关联任务
```
GET /api/focus-records/task-search
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `keyword`: 搜索关键词 (可选)
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认10

**响应**:
```json
{
  "code": 200,
  "data": {
    "data": [
      {
        "id": 1,
        "title": "完成英语作业",
        "priority": 2,
        "status": 0,
        "deadline": "2024-01-16T15:00:00",
        "estimatedSeconds": 3600
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "total": 20,
    "pages": 2,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**说明**: 只返回待办/进行中且有预估时间的任务

---

### 7.2 开始专注
```
POST /api/focus-records/start
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `taskId`: 关联任务ID (可选)

**响应**:
```json
{
  "code": 200,
  "message": "专注已开始",
  "data": null
}
```

**说明**: 若关联任务，会自动将任务状态改为进行中(1)

---

### 7.3 创建专注记录
```
POST /api/focus-records
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "taskId": 1,                                    // 关联任务ID (可选)
  "startTime": "2024-01-15T10:00:00+08:00",      // 开始时间
  "endTime": "2024-01-15T10:30:00+08:00",        // 结束时间
  "duration": 1800,                               // 时长(秒)
  "interruptions": 0,                             // 中断次数
  "focusScore": 95,                               // 专注分数 (可选)
  "notes": "完成了前两章",                         // 笔记 (可选)
  "status": 1                                     // 状态 (0放弃/1完成/2取消)
}
```

**响应**:
```json
{
  "code": 200,
  "message": "专注记录已保存",
  "data": {
    "id": 1,
    "userId": 1,
    "taskId": 1,
    "taskTitle": "完成英语作业",
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T10:30:00",
    "duration": 1800,
    "interruptions": 0,
    "focusScore": 95,
    "notes": "完成了前两章",
    "status": 1,
    "createdTime": "2024-01-15T10:30:00"
  }
}
```

**说明**: 
- status=1(完成)且关联任务时，自动将任务标记为完成
- 若为重复任务，自动创建下一次任务

---

### 7.4 查询专注记录
```
GET /api/focus-records
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | Long | 否 | 按任务筛选 |
| startTimeAfter | String | 否 | 开始时间晚于 |
| endTimeBefore | String | 否 | 结束时间早于 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

### 7.5 获取专注记录详情
```
GET /api/focus-records/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 记录ID

---

### 7.6 更新专注笔记
```
PUT /api/focus-records/{id}/notes
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 记录ID

**查询参数**: `notes` - 新的笔记内容

---

### 7.7 删除专注记录
```
DELETE /api/focus-records/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 记录ID

---

## 八、消息管理 (MessageController)

### 8.1 获取消息列表
```
GET /api/messages
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "系统通知",
      "content": "您的账号已通过审核",
      "type": "SYSTEM",
      "isRead": false,
      "createdTime": "2024-01-15T10:00:00"
    }
  ]
}
```

---

### 8.2 获取未读消息数量
```
GET /api/messages/unread-count
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "count": 5
  }
}
```

---

### 8.3 标记消息已读
```
PUT /api/messages/{id}/read
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 消息ID

---

### 8.4 标记所有消息已读
```
PUT /api/messages/mark-all-read
```

**请求头**: `Authorization: Bearer <token>`

---

## 九、提醒管理 (ReminderController)

### 9.1 获取待处理提醒
```
GET /api/reminders/pending
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "taskId": 1,
      "taskTitle": "完成英语作业",
      "triggerTime": "2024-01-15T14:00:00",
      "taskTime": "2024-01-16T15:00:00",
      "reminderType": "DEADLINE",
      "status": "PENDING",
      "message": "任务即将到期",
      "createdAt": "2024-01-15T10:00:00"
    }
  ]
}
```

---

### 9.2 获取所有提醒
```
GET /api/reminders/all
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `page`: 页码，默认1
- `pageSize`: 每页数量，默认10

---

### 9.3 按状态筛选提醒
```
GET /api/reminders
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `status`: 状态筛选 (ALL/READ/DISMISSED/PENDING)
- `page`: 页码，默认1
- `pageSize`: 每页数量，默认10

---

### 9.4 更新提醒状态
```
PUT /api/reminders/{id}/status
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 提醒ID

**查询参数**: `status` - 新状态 (READ/DISMISSED)

---

### 9.5 批量标记已读
```
PUT /api/reminders/read-all
```

**请求头**: `Authorization: Bearer <token>`

---

### 9.6 删除提醒
```
DELETE /api/reminders/{id}
```

**请求头**: `Authorization: Bearer <token>`

**路径参数**: `id` - 提醒ID

---

### 9.7 手动触发提醒扫描
```
POST /api/reminders/scan
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "message": "扫描完成",
    "timestamp": "2024-01-15T10:00:00"
  }
}
```

---

### 9.8 获取提醒统计
```
GET /api/reminders/stats
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "pending": 5,
    "read": 80,
    "dismissed": 10,
    "expired": 5
  }
}
```

---

### 9.9 清理已完成提醒
```
DELETE /api/reminders/cleanup
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "message": "清理完成",
    "deletedCount": 50
  }
}
```

---

### 9.10 标记超时提醒为已过期
```
PUT /api/reminders/expire
```

**请求头**: `Authorization: Bearer <token>`

---

## 十、用户统计 (UserStatsController)

### 10.1 获取用户统计数据
```
GET /api/user/stats
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalFocusSeconds": 36000,        // 总专注时长(秒)
    "monthlyFocusSeconds": 7200,       // 本月专注时长
    "todayFocusSeconds": 1800,         // 今日专注时长
    "todayGoalRate": 0.75,             // 今日目标完成率
    "maxDailyFocusSeconds": 14400,     // 最高单日专注时长
    "dailyFocusGoalSeconds": 7200,     // 每日目标时长
    "totalCompletedTasks": 50,         // 总完成任务数
    "todayCompletedTasks": 3,          // 今日完成任务数
    "weekCompletedTasks": 15,          // 本周完成任务数
    "monthCompletedTasks": 45,         // 本月完成任务数
    "todayTaskRate": 0.6,              // 今日任务完成率
    "weekTaskRate": 0.75,              // 本周任务完成率
    "monthTaskRate": 0.8,              // 本月任务完成率
    "currentStreak": 7,                // 当前连续打卡天数
    "maxStreak": 30,                   // 最长连续打卡天数
    "totalCheckInDays": 100,           // 总打卡天数
    "minEffectiveDurationSeconds": 300, // 最小有效时长
    "badges": [                        // 勋章列表
      {
        "id": 1,
        "name": "专注新手",
        "icon": "🎯",
        "description": "完成首次专注",
        "unlocked": true,
        "progress": 1,
        "target": 1
      }
    ]
  }
}
```

---

### 10.2 按分类统计专注时间
```
GET /api/user/stats/focus-by-category
```

**请求头**: `Authorization: Bearer <token>`

**查询参数**:
- `range`: 时间范围 (day/week/month/custom)，默认week
- `startDate`: 开始日期 (yyyy-MM-dd，range=custom时必填)
- `endDate`: 结束日期 (yyyy-MM-dd，range=custom时必填)

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "category": "学习",
      "duration": 3600
    },
    {
      "category": "工作",
      "duration": 7200
    },
    {
      "category": "运动",
      "duration": 1800
    }
  ]
}
```

---

## 十一、反馈管理 (FeedbackController)

### 11.1 提交反馈
```
POST /api/feedback
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "category": "BUG",           // 分类 (BUG/SUGGESTION/OTHER)
  "title": "string",           // 标题
  "content": "string",         // 内容
  "contact": "string"          // 联系方式 (可选)
}
```

---

### 11.2 获取我的反馈
```
GET /api/feedback/my
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "category": "BUG",
      "title": "页面显示异常",
      "content": "xxx页面在xxx情况下显示异常",
      "contact": "test@example.com",
      "status": "PROCESSED",
      "adminReply": "已修复，请更新版本",
      "userRead": false,
      "createdTime": "2024-01-15T10:00:00",
      "updatedTime": "2024-01-16T10:00:00"
    }
  ]
}
```

---

### 11.3 获取未读反馈回复数量
```
GET /api/feedback/unread-count
```

**请求头**: `Authorization: Bearer <token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "count": 2
  }
}
```

---

### 11.4 标记所有反馈回复已读
```
PUT /api/feedback/mark-read
```

**请求头**: `Authorization: Bearer <token>`

---

## 十二、申诉管理 (AppealController)

### 12.1 获取封禁信息
```
GET /api/appeal/ban-info?username=testuser
```

**说明**: 无需登录

**响应**:
```json
{
  "code": 200,
  "data": {
    "status": 1,
    "banReason": "违规操作",
    "banTime": "2024-01-15T10:00:00",
    "banEndTime": null,
    "appealStatus": 0,
    "auditNote": "",
    "username": "testuser"
  }
}
```

---

### 12.2 提交申诉
```
POST /api/appeal/submit
```

**说明**: 无需登录

**请求参数**:
```json
{
  "username": "string",    // 用户名
  "content": "string"      // 申诉内容，不超过2000字
}
```

---

### 12.3 管理员获取申诉列表
```
GET /api/appeal/admin/list
```

**请求头**: `Authorization: Bearer <admin_token>`

**查询参数**:
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20
- `status`: 状态筛选 (0待审核/1通过/2驳回)

---

### 12.4 管理员审核申诉
```
PUT /api/appeal/admin/audit/{id}
```

**请求头**: `Authorization: Bearer <admin_token>`

**路径参数**: `id` - 申诉ID

**请求参数**:
```json
{
  "status": 1,              // 1通过/2驳回
  "auditNote": "string"     // 审核备注
}
```

**说明**: 通过申诉会自动解封账号

---

### 12.5 获取待审核申诉数量
```
GET /api/appeal/admin/pending-count
```

**请求头**: `Authorization: Bearer <admin_token>`

---

## 十三、AI任务解析 (AiController)

### 13.1 同步解析任务
```
POST /api/ai/parse-task
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**:
```json
{
  "prompt": "明天下午3点前完成英语作业，提前一天提醒我",
  "userId": "1"    // 可选
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "message": "已为您创建任务",
    "taskCreated": true,
    "taskCount": 1,
    "isAiGenerated": true
  }
}
```

**说明**: 
- 支持三个路径: `/api/ai/parse-task`, `/api/ai/parse`, `/api/ai/task/parse`
- AI会自动解析自然语言并创建任务

---

### 13.2 异步解析任务
```
POST /api/ai/parse-task/async
```

**请求头**: `Authorization: Bearer <token>`

**请求参数**: 同13.1

**响应**:
```json
{
  "code": 200,
  "data": {
    "taskId": "uuid-string",
    "message": "任务已提交，正在处理"
  }
}
```

---

### 13.3 查询异步任务状态
```
GET /api/ai/task/status/{taskId}
```

**路径参数**: `taskId` - 任务ID (UUID)

**响应**:
```json
{
  "code": 200,
  "data": {
    "taskId": "uuid-string",
    "status": "COMPLETED",
    "statusDescription": "已完成"
  }
}
```

---

### 13.4 获取异步任务结果
```
GET /api/ai/task/result/{taskId}
```

**路径参数**: `taskId` - 任务ID (UUID)

**响应**:
```json
{
  "code": 200,
  "data": {
    "taskId": "uuid-string",
    "message": "已为您创建任务",
    "taskCreated": true,
    "taskCount": 1,
    "isAiGenerated": true
  }
}
```

---

### 13.5 删除异步任务记录
```
DELETE /api/ai/task/{taskId}
```

**路径参数**: `taskId` - 任务ID (UUID)

---

## 十四、SSE实时推送 (SseController)

### 14.1 建立SSE连接
```
GET /api/sse/connect?token=<jwt_token>
```

**说明**: 
- 通过query参数传递token（非Header）
- 返回 `text/event-stream` 格式
- 用于实时接收消息推送

**事件类型**:
- `message`: 新消息通知
- `reminder`: 提醒通知
- `feedback`: 反馈回复通知

---

## 十五、管理员后台 (AdminController)

### 15.1 获取反馈列表
```
GET /api/admin/feedbacks
```

**请求头**: `Authorization: Bearer <admin_token>`

**查询参数**:
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20
- `status`: 状态筛选 (PENDING/PROCESSED/CLOSED)

---

### 15.2 回复反馈
```
PUT /api/admin/feedbacks/{id}/reply
```

**请求头**: `Authorization: Bearer <admin_token>`

**路径参数**: `id` - 反馈ID

**请求参数**:
```json
{
  "status": "PROCESSED",      // 新状态
  "adminReply": "string"      // 回复内容
}
```

---

### 15.3 获取反馈统计
```
GET /api/admin/feedbacks/stats
```

**请求头**: `Authorization: Bearer <admin_token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "pending": 5,
    "processed": 80,
    "closed": 15
  }
}
```

---

### 15.4 获取待处理反馈数量
```
GET /api/admin/feedbacks/pending-count
```

**请求头**: `Authorization: Bearer <admin_token>`

---

### 15.5 查看用户在线状态
```
GET /api/admin/users/online
```

**请求头**: `Authorization: Bearer <admin_token>`

**响应**:
```json
{
  "code": 200,
  "data": {
    "onlineCount": 10,
    "todayActiveCount": 50,
    "totalActive": 200,
    "totalUsers": 500,
    "onlineThresholdMinutes": 15,
    "users": [...]
  }
}
```

---

### 15.6 查询用户列表
```
GET /api/admin/users
```

**请求头**: `Authorization: Bearer <admin_token>`

**查询参数**:
- `keyword`: 搜索关键词 (用户名/昵称)
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20

---

### 15.7 封禁用户
```
POST /api/admin/users/{id}/ban
```

**请求头**: `Authorization: Bearer <admin_token>`

**路径参数**: `id` - 用户ID

**请求参数**:
```json
{
  "banType": 1,                           // 1永久封禁/2临时封禁
  "banReason": "string",                  // 封禁原因
  "banEndTime": "2024-02-15T10:00:00"     // 到期时间 (临时封禁必填)
}
```

**说明**: 不能封禁管理员账号

---

### 15.8 解封用户
```
PUT /api/admin/users/{id}/unban
```

**请求头**: `Authorization: Bearer <admin_token>`

**路径参数**: `id` - 用户ID

**说明**: 解封后会发送系统消息通知用户

---

### 15.9 获取申诉列表
```
GET /api/admin/appeals
```

**请求头**: `Authorization: Bearer <admin_token>`

**查询参数**:
- `pageNum`: 页码，默认1
- `pageSize`: 每页数量，默认20
- `status`: 状态筛选 (0待审核/1通过/2驳回)

---

### 15.10 审核申诉
```
PUT /api/admin/appeals/{id}/audit
```

**请求头**: `Authorization: Bearer <admin_token>`

**路径参数**: `id` - 申诉ID

**请求参数**:
```json
{
  "status": 1,              // 1通过/2驳回
  "auditNote": "string"     // 审核备注
}
```

---

### 15.11 获取待审核申诉数量
```
GET /api/admin/appeals/pending-count
```

**请求头**: `Authorization: Bearer <admin_token>`

---

### 15.12 私发消息
```
POST /api/admin/messages/send
```

**请求头**: `Authorization: Bearer <admin_token>`

**请求参数**:
```json
{
  "recipientId": 1,         // 接收用户ID
  "title": "string",        // 消息标题
  "content": "string"       // 消息内容
}
```

---

### 15.13 群发消息
```
POST /api/admin/messages/broadcast
```

**请求头**: `Authorization: Bearer <admin_token>`

**请求参数**:
```json
{
  "title": "string",        // 消息标题
  "content": "string"       // 消息内容
}
```

---

## 接口汇总

| 模块 | 接口数量 | 需要登录 | 需要管理员 |
|------|---------|---------|-----------|
| 认证管理 | 5 | 1 | 0 |
| 密码管理 | 4 | 1 | 0 |
| 用户管理 | 2 | 2 | 0 |
| 头像管理 | 4 | 2 | 0 |
| 任务管理 | 9 | 9 | 0 |
| 子任务管理 | 6 | 6 | 0 |
| 专注记录 | 7 | 7 | 0 |
| 消息管理 | 4 | 4 | 0 |
| 提醒管理 | 10 | 10 | 0 |
| 申诉管理 | 5 | 2 | 3 |
| 反馈管理 | 4 | 3 | 0 |
| 用户统计 | 2 | 2 | 0 |
| 管理员后台 | 13 | 0 | 13 |
| AI任务 | 5 | 3 | 0 |
| SSE推送 | 1 | 0 | 0 |
| **合计** | **81** | **52** | **16** |

---

## 数据模型

### UserVO
```typescript
{
  id: number
  username: string
  nickname: string
  email: string
  avatarUrl: string
  settings: string
  isActive: number
  status: number          // 0正常/1永久封禁/2临时封禁
  role: number            // 0普通用户/1管理员
  lastLoginTime: string
  occupation: string
  phone: string
  gender: string
  city: string
  hobbies: string
  bio: string
  weChatWebhookUrl: string
  aiApiKey: string
  dailyFocusGoal: number  // 每日专注目标(分钟)
  minEffectiveDuration: number
  createdTime: string
  updatedTime: string
  birthday: string
}
```

### TaskVO
```typescript
{
  id: number
  userId: number
  title: string
  description: string
  priority: number        // 0低/1中/2高
  status: number          // 0待办/1进行中/2已完成/3已取消/4已存档
  deadline: string
  remindTime: string
  estimatedSeconds: number
  actualSeconds: number
  category: string
  tags: string[]
  reminderConfig: string
  isAiGenerated: boolean
  recurrenceRule: string
  recurrenceEndDate: string
  createdTime: string
  updatedTime: string
  completedTime: string
}
```

### FocusRecordVO
```typescript
{
  id: number
  userId: number
  taskId: number
  taskTitle: string
  startTime: string
  endTime: string
  duration: number        // 时长(秒)
  interruptions: number   // 中断次数
  focusScore: number
  notes: string
  status: number          // 0放弃/1完成/2取消
  createdTime: string
  task: TaskVO
}
```

### ReminderRecordVO
```typescript
{
  id: number
  taskId: number
  taskTitle: string
  triggerTime: string
  taskTime: string
  reminderType: string
  status: string          // PENDING/READ/DISMISSED/EXPIRED
  message: string
  createdAt: string
}
```

### FeedbackVO
```typescript
{
  id: number
  userId: number
  username: string
  nickname: string
  category: string        // BUG/SUGGESTION/OTHER
  title: string
  content: string
  contact: string
  status: string          // PENDING/PROCESSED/CLOSED
  adminReply: string
  userRead: boolean
  createdTime: string
  updatedTime: string
}
```

### BanInfoVO
```typescript
{
  status: number
  banReason: string
  banTime: string
  banEndTime: string
  appealStatus: number
  auditNote: string
  username: string
}
```

### AppealVO
```typescript
{
  id: number
  userId: number
  username: string
  nickname: string
  content: string
  status: number          // 0待审核/1通过/2驳回
  auditAdminId: number
  auditAdminName: string
  auditTime: string
  auditNote: string
  createdTime: string
  banReason: string
  userStatus: number
  banEndTime: string
  banTime: string
}
```

### UserStatsVO
```typescript
{
  totalFocusSeconds: number
  monthlyFocusSeconds: number
  todayFocusSeconds: number
  todayGoalRate: number
  maxDailyFocusSeconds: number
  dailyFocusGoalSeconds: number
  totalCompletedTasks: number
  todayCompletedTasks: number
  weekCompletedTasks: number
  monthCompletedTasks: number
  todayTaskRate: number
  weekTaskRate: number
  monthTaskRate: number
  currentStreak: number
  maxStreak: number
  totalCheckInDays: number
  minEffectiveDurationSeconds: number
  badges: BadgeVO[]
}
```

### BadgeVO
```typescript
{
  id: number
  name: string
  icon: string
  description: string
  unlocked: boolean
  progress: number
  target: number
}
```

---

*文档版本: v1.0*
*更新时间: 2026-05-18*
*接口总数: 81个*
