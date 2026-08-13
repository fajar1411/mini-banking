#!/bin/bash
set -e

echo "Starting MySQL..."

service mysql start

echo "Waiting for MySQL..."

until mysqladmin ping --silent; do
    sleep 2
done

echo "MySQL is ready."

mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
CREATE DATABASE IF NOT EXISTS mini_banking;
EOF

echo "Database mini_banking is ready."

echo "Starting Spring Boot..."

exec java -jar /app/app.jar