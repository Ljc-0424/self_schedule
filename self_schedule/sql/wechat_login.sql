-- 微信登录支持：添加 open_id 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `open_id` VARCHAR(128) DEFAULT NULL COMMENT '微信OpenID' AFTER `min_effective_duration`;
ALTER TABLE `user` ADD UNIQUE INDEX IF NOT EXISTS `idx_open_id` (`open_id`);

-- 确保其他微信登录相关字段存在
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `daily_focus_goal` INT DEFAULT 120 COMMENT '每日专注目标(分钟)' AFTER `ai_api_key`;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `min_effective_duration` INT DEFAULT 5 COMMENT '最小有效专注时长(分钟)' AFTER `daily_focus_goal`;
