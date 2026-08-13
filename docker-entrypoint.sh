#!/bin/bash
set -e

echo "================================="
echo "Starting MySQL"
echo "================================="

# Start MySQL
mysqld_safe --datadir=/var/lib/mysql &

echo "Waiting for MySQL..."

until mysqladmin ping --silent; do
    sleep 2
done

echo "MySQL is ready."

# Create database
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS mini_banking;
EOF

# Set root password
mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
EOF

echo "Database is ready."

echo "================================="
echo "Starting Spring Boot"
echo "================================="

exec java -jar /app/app.jar