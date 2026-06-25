#!/bin/bash
BACKUP_DIR="/opt/self-schedule/backups"
MYSQL_USER="${DB_USERNAME:-schedule}"
MYSQL_PASS="${DB_PASSWORD:-请通过环境变量设置密码}"
DB_NAME="self_schedule"
RETENTION_DAYS=30

mkdir -p "$BACKUP_DIR"

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/db_backup_$DATE.sql"

echo "[$(date)] 开始备份数据库 $DB_NAME..."

mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASS" "$DB_NAME" > "$BACKUP_FILE" 2>&1

if [ $? -eq 0 ]; then
  echo "[$(date)] 备份成功: $BACKUP_FILE"

  gzip "$BACKUP_FILE"
  echo "[$(date)] 压缩完成: ${BACKUP_FILE}.gz"

  echo "[$(date)] 清理 $RETENTION_DAYS 天前的备份..."
  find "$BACKUP_DIR" -name "db_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
  echo "[$(date)] 清理完成"
else
  echo "[$(date)] 备份失败，检查日志"
  exit 1
fi

echo "[$(date)] 备份任务完成"