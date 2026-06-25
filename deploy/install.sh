#!/bin/bash
set -e

echo "========================================="
echo "  自律日程 - 服务器环境初始化脚本"
echo "========================================="

echo ""
echo "[1/6] 更新系统包..."
apt update && apt upgrade -y

echo ""
echo "[2/6] 安装基础工具..."
apt install -y curl wget git unzip software-properties-common

echo ""
echo "[3/6] 安装 Java 21..."
apt install -y openjdk-21-jre-headless
java -version

echo ""
echo "[4/6] 安装 MySQL 8..."
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

echo ""
echo "[5/6] 安装 Redis..."
apt install -y redis-server
systemctl start redis-server
systemctl enable redis-server

echo ""
echo "[6/6] 配置防火墙..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 8080/tcp
ufw --force enable

echo ""
echo "========================================="
echo "  环境安装完成！"
echo "========================================="
echo ""
echo "接下来请执行："
echo "  1. 配置 MySQL:   mysql_secure_installation"
echo "  2. 创建数据库:   mysql -u root -e \"CREATE DATABASE self_schedule DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""
echo "  3. 创建用户:     mysql -u root -e \"CREATE USER 'schedule'@'localhost' IDENTIFIED BY '你的密码'; GRANT ALL ON self_schedule.* TO 'schedule'@'localhost'; FLUSH PRIVILEGES;\""
echo "  4. 上传 deploy.sh 和 JAR 文件到服务器"
echo "  5. 运行 deploy.sh 部署应用"
