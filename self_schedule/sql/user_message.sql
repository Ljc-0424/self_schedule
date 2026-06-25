CREATE TABLE IF NOT EXISTS `user_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `recipient_id` BIGINT NOT NULL COMMENT '接收者ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-系统通知(群发) 2-管理员私信',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_read` (`recipient_id`, `is_read`),
  KEY `idx_recipient_created` (`recipient_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';
