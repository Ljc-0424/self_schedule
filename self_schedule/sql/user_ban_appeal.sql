ALTER TABLE `user` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态：0=正常 1=永久禁用 2=临时禁用' AFTER `is_active`;
ALTER TABLE `user` ADD COLUMN `ban_reason` VARCHAR(255) DEFAULT NULL COMMENT '禁用原因（用户可见）' AFTER `status`;
ALTER TABLE `user` ADD COLUMN `ban_end_time` DATETIME DEFAULT NULL COMMENT '临时禁用到期时间（永久禁用为NULL）' AFTER `ban_reason`;
ALTER TABLE `user` ADD COLUMN `ban_operator` BIGINT DEFAULT NULL COMMENT '操作管理员ID' AFTER `ban_end_time`;
ALTER TABLE `user` ADD COLUMN `ban_time` DATETIME DEFAULT NULL COMMENT '禁用时间' AFTER `ban_operator`;

CREATE TABLE IF NOT EXISTS `user_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '申诉用户ID',
  `content` TEXT NOT NULL COMMENT '用户填写的申诉理由',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '申诉状态：0=待审核 1=通过（解封） 2=驳回',
  `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_note` VARCHAR(255) DEFAULT NULL COMMENT '管理员审核备注（用户可见，驳回必填）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申诉表';
