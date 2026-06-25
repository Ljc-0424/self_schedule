#!/bin/bash
set -e

APP_NAME="self-schedule"
APP_JAR="self_schedule-0.0.1-SNAPSHOT.jar"
APP_DIR="/opt/$APP_NAME"
LOG_DIR="/var/log/$APP_NAME"
PID_FILE="$APP_DIR/app.pid"

echo "========================================="
echo "  自律日程 - 部署脚本"
echo "========================================="

mkdir -p $APP_DIR
mkdir -p $LOG_DIR

if [ ! -f "$APP_DIR/$APP_JAR" ]; then
    echo "错误: 未找到 $APP_DIR/$APP_JAR"
    echo "请先将 JAR 文件上传到 $APP_DIR/"
    echo "  scp $APP_JAR root@你的服务器IP:$APP_DIR/"
    exit 1
fi

echo ""
echo "[1/4] 停止旧服务..."
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat $PID_FILE)
    if kill -0 $OLD_PID 2>/dev/null; then
        kill $OLD_PID
        echo "已发送停止信号到进程 $OLD_PID"
        sleep 5
        if kill -0 $OLD_PID 2>/dev/null; then
            kill -9 $OLD_PID
            echo "强制停止进程 $OLD_PID"
        fi
    fi
    rm -f $PID_FILE
fi

echo ""
echo "[2/4] 启动应用..."
nohup java -jar $APP_DIR/$APP_JAR \
    --spring.datasource.url="jdbc:mysql://127.0.0.1:3306/self_schedule?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
    --spring.datasource.username="${DB_USERNAME:-schedule}" \
    --spring.datasource.password="${DB_PASSWORD:-请修改这里}" \
    --spring.data.redis.host=127.0.0.1 \
    --spring.data.redis.port=6379 \
    --jwt.secret-key="${JWT_SECRET_KEY:-请修改这里为至少32位随机字符串}" \
    --encryption.secret-key="${AES_SECRET_KEY:-请修改这里为至少16位随机字符串}" \
    --debug=false \
    --logging.level.root=WARN \
    --logging.level.com.cjl.self_schedule=INFO \
    > $LOG_DIR/app.log 2>&1 &

echo $! > $PID_FILE
echo "应用已启动, PID: $(cat $PID_FILE)"

echo ""
echo "[3/4] 等待启动..."
sleep 10

if kill -0 $(cat $PID_FILE) 2>/dev/null; then
    echo "应用运行中 ✓"
else
    echo "应用启动失败！查看日志:"
    tail -50 $LOG_DIR/app.log
    exit 1
fi

echo ""
echo "[4/4] 健康检查..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "健康检查通过 ✓ (HTTP $HTTP_CODE)"
else
    echo "健康检查返回 HTTP $HTTP_CODE，可能还在启动中，稍等片刻再试"
fi

echo ""
echo "========================================="
echo "  部署完成！"
echo "========================================="
echo ""
echo "访问地址: http://你的服务器IP:8080"
echo "查看日志: tail -f $LOG_DIR/app.log"
echo "停止服务: kill \$(cat $PID_FILE)"
echo "重启服务: bash deploy.sh"
