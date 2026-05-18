#!/bin/bash
#===========================================
# AgentEdu 智能助教系统 - Rocky Linux 一键部署
# 在服务器终端粘贴整段运行
#===========================================

set -e

# 配置
GITHUB_REPO="https://github.com/yilan-2004/bs.git"
DB_NAME="agentedu"
DB_USER="root"
DB_PASS="Wz.@5210"
APP_DIR="/www/agentedu"

echo "============================================"
echo "  AgentEdu 智能助教系统 - 一键部署"
echo "============================================"
echo ""

# 0. 安装基础工具
echo "[0/10] 安装基础工具..."
dnf install -y git curl wget 2>/dev/null || yum install -y git curl wget

# 1. 安装 MySQL 8
echo "[1/10] 安装 MySQL 8.0..."
dnf module disable mysql -y 2>/dev/null || true
cat > /etc/yum.repos.d/mysql.repo << 'MYSQLREPO'
[mysql-8.0-community]
name=MySQL 8.0 Community Server
baseurl=https://repo.mysql.com/yum/mysql-8.0-community/el/8/x86_64/
enabled=1
gpgcheck=0
MYSQLREPO
dnf install -y mysql-community-server 2>/dev/null || yum install -y mysql-community-server
systemctl start mysqld
systemctl enable mysqld

# 获取 MySQL 临时密码
MYSQL_TMP_PASS=$(grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}' | tail -1)
echo "  MySQL 临时密码: $MYSQL_TMP_PASS"

# 设置 MySQL root 密码
mysql -u root -p"$MYSQL_TMP_PASS" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASS';" 2>/dev/null || \
mysql -u root -p"$DB_PASS" -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASS';" 2>/dev/null || true

# 创建数据库
mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true
echo "  MySQL 安装完成"

# 2. 安装 Java 17
echo "[2/10] 安装 OpenJDK 17..."
dnf install -y java-17-openjdk java-17-openjdk-devel 2>/dev/null || yum install -y java-17-openjdk java-17-openjdk-devel
java -version 2>&1 | head -1
echo "  Java 安装完成"

# 3. 安装 Node.js 18
echo "[3/10] 安装 Node.js 18..."
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash - 2>/dev/null
dnf install -y nodejs 2>/dev/null || yum install -y nodejs
node -v
echo "  Node.js 安装完成"

# 4. 安装 Maven + Nginx
echo "[4/10] 安装 Maven + Nginx..."
dnf install -y maven nginx 2>/dev/null || yum install -y maven nginx
mvn -version | head -1
nginx -v
echo "  依赖安装完成"

# 5. 拉取代码
echo "[5/10] 拉取代码..."
mkdir -p $APP_DIR
cd $APP_DIR
git clone $GITHUB_REPO . 2>/dev/null || (git fetch origin && git reset --hard origin/main)
echo "  代码拉取完成"

# 6. 导入数据库
echo "[6/10] 导入数据库..."
if [ -f src/main/resources/sql/demo-data.sql ]; then
    mysql -u root -p"$DB_PASS" $DB_NAME < src/main/resources/sql/demo-data.sql 2>/dev/null && echo "  数据库导入完成" || echo "  数据库导入失败，跳过"
else
    echo "  警告: 未找到数据库脚本 demo-data.sql"
fi

# 7. 修改后端配置
echo "[7/10] 配置后端..."
cd $APP_DIR

# 修改数据库密码
sed -i "s/password: 123456/password: $DB_PASS/g" src/main/resources/application.yml 2>/dev/null || true
sed -i "s/username: root/username: root/g" src/main/resources/application.yml 2>/dev/null || true

# 修改服务端口
sed -i 's/server.port: 8080/server.port: 8080/g' src/main/resources/application.yml 2>/dev/null || true

echo "  后端配置完成"

# 8. 构建后端
echo "[8/10] 构建后端 (约5-10分钟)..."
cd $APP_DIR
mvn clean package -DskipTests -q 2>&1 | tail -5
JAR_FILE=$(ls target/*.jar 2>/dev/null | grep -v original | head -1)
echo "  后端构建完成: $(basename $JAR_FILE)"

# 9. 构建前端
echo "[9/10] 构建前端 (约3-5分钟)..."
cd $APP_DIR/frontend
npm install --legacy-peer-deps --silent 2>/dev/null || npm install --legacy-peer-deps
npm run build 2>&1 | tail -3
echo "  前端构建完成"

# 10. 配置 Nginx
echo "[10/10] 配置 Nginx..."
mkdir -p /www/agentedu/frontend/dist

cat > /etc/nginx/conf.d/agentedu.conf << 'NGINXEOF'
server {
    listen 80;
    server_name _;

    # 前端静态文件
    location / {
        root /www/agentedu/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket
    location /ws {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
NGINXEOF

nginx -t && systemctl enable nginx && systemctl restart nginx
echo "  Nginx 配置完成"

# 开放端口
echo ""
echo "[防火墙] 开放端口..."
firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true
firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true
firewall-cmd --reload 2>/dev/null || true
setsebool -P httpd_can_network_connect 1 2>/dev/null || true

# 创建启动脚本
cat > $APP_DIR/start.sh << STARTEOF
#!/bin/bash
JAR_FILE="$JAR_FILE"
APP_DIR="$APP_DIR"

echo "停止旧进程..."
pkill -f "agentedu-backend" 2>/dev/null || true
sleep 2

echo "启动 AgentEdu..."
cd \$APP_DIR
nohup java -jar \$JAR_FILE --server.port=8080 > \$APP_DIR/app.log 2>&1 &
echo "AgentEdu 已启动, PID: \$!"
echo "查看日志: tail -f \$APP_DIR/app.log"
STARTEOF
chmod +x $APP_DIR/start.sh

# 启动
echo ""
echo "[启动] 启动后端服务..."
bash $APP_DIR/start.sh

echo ""
echo "============================================"
echo "  部署完成！"
echo "============================================"
echo ""
echo "访问地址: http://你的服务器公网IP"
echo ""
echo "初始账号:"
echo "  教师: teacher001 / 123456"
echo "  学生: student001 / 123456"
echo ""
echo "常用命令:"
echo "  启动: bash $APP_DIR/start.sh"
echo "  查看日志: tail -f $APP_DIR/app.log"
echo "  重启nginx: systemctl restart nginx"
echo "============================================"