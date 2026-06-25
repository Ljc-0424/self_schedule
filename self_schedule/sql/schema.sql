-- ============================================
-- self_schedule 数据库初始化脚本
-- 根据实体类自动生成
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `settings` TEXT DEFAULT NULL COMMENT '用户设置(JSON)',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否激活：0-否 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `ban_reason` VARCHAR(500) DEFAULT NULL COMMENT '封禁原因',
  `ban_end_time` DATETIME DEFAULT NULL COMMENT '封禁结束时间',
  `ban_operator` BIGINT DEFAULT NULL COMMENT '封禁操作人ID',
  `ban_time` DATETIME DEFAULT NULL COMMENT '封禁时间',
  `role` TINYINT DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `occupation` VARCHAR(100) DEFAULT NULL COMMENT '职业',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `hobbies` VARCHAR(500) DEFAULT NULL COMMENT '爱好',
  `bio` VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
  `wechat_webhook_url` VARCHAR(500) DEFAULT NULL COMMENT '微信Webhook URL',
  `ai_api_key` VARCHAR(500) DEFAULT NULL COMMENT 'AI API Key',
  `daily_focus_goal` INT DEFAULT 0 COMMENT '每日专注目标(分钟)',
  `min_effective_duration` INT DEFAULT 0 COMMENT '最小有效专注时长(分钟)',
  `open_id` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_open_id` (`open_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 任务表
CREATE TABLE IF NOT EXISTS `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `description` TEXT DEFAULT NULL COMMENT '任务描述',
  `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低 2-中 3-高',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待办 1-进行中 2-已完成 3-已取消',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
  `remind_time` DATETIME DEFAULT NULL COMMENT '提醒时间',
  `estimated_seconds` INT DEFAULT NULL COMMENT '预估耗时(秒)',
  `actual_seconds` INT DEFAULT NULL COMMENT '实际耗时(秒)',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `reminder_config` TEXT DEFAULT NULL COMMENT '提醒配置(JSON)',
  `is_ai_generated` TINYINT DEFAULT 0 COMMENT '是否AI生成：0-否 1-是',
  `recurrence_rule` VARCHAR(100) DEFAULT NULL COMMENT '重复规则',
  `recurrence_end_date` DATETIME DEFAULT NULL COMMENT '重复结束日期',
  `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deadline` (`deadline`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 子任务表
CREATE TABLE IF NOT EXISTS `subtask` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '父任务ID',
  `title` VARCHAR(200) NOT NULL COMMENT '子任务标题',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待办 1-已完成',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子任务表';

-- 专注记录表
CREATE TABLE IF NOT EXISTS `focus_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '任务ID',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration` INT DEFAULT 0 COMMENT '专注时长(秒)',
  `interruptions` INT DEFAULT 0 COMMENT '中断次数',
  `focus_score` INT DEFAULT 0 COMMENT '专注评分',
  `notes` VARCHAR(1000) DEFAULT NULL COMMENT '备注',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-中断 1-完成',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专注记录表';

-- 提醒记录表
CREATE TABLE IF NOT EXISTS `reminder_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '任务ID',
  `task_title` VARCHAR(200) DEFAULT NULL COMMENT '任务标题',
  `trigger_time` DATETIME NOT NULL COMMENT '触发时间',
  `task_time` DATETIME DEFAULT NULL COMMENT '任务时间',
  `reminder_type` VARCHAR(50) DEFAULT NULL COMMENT '提醒类型',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  `message` VARCHAR(500) DEFAULT NULL COMMENT '提醒消息',
  `channel` VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '提醒渠道',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `send_status` INT DEFAULT 0 COMMENT '发送状态：0-待发送 1-已发送 2-发送失败',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_trigger_time` (`trigger_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒记录表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '反馈分类',
  `title` VARCHAR(200) NOT NULL COMMENT '反馈标题',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已解决',
  `admin_reply` TEXT DEFAULT NULL COMMENT '管理员回复',
  `user_read` TINYINT DEFAULT 0 COMMENT '用户是否已读：0-未读 1-已读',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 用户申诉表
CREATE TABLE IF NOT EXISTS `user_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '申诉内容',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-通过 2-拒绝',
  `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_note` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申诉表';

-- 用户消息表
CREATE TABLE IF NOT EXISTS `user_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `recipient_id` BIGINT NOT NULL COMMENT '接收者ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-系统通知(群发) 2-管理员私信',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_read` (`recipient_id`, `is_read`),
  KEY `idx_recipient_created` (`recipient_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';
