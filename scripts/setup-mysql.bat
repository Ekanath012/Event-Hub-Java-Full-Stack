@echo off
REM One-time: creates the eventhub database and user in a local MySQL 8.
REM Reads DB_NAME, DB_USERNAME, DB_PASSWORD, MYSQL_ROOT_PASSWORD from ..\.env
setlocal EnableDelayedExpansion
set "ENVFILE=%~dp0..\.env"
if not exist "%ENVFILE%" (
  echo Missing %ENVFILE%
  exit /b 1
)
for /f "usebackq eol=# tokens=1* delims==" %%A in ("%ENVFILE%") do (
  set "%%A=%%B"
)
if "!MYSQL_PORT!"=="" set "MYSQL_PORT=3306"
mysql -h 127.0.0.1 -P !MYSQL_PORT! -u root -p"!MYSQL_ROOT_PASSWORD!" -e "CREATE DATABASE IF NOT EXISTS `!DB_NAME!` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS '!DB_USERNAME!'@'%%' IDENTIFIED BY '!DB_PASSWORD!'; CREATE USER IF NOT EXISTS '!DB_USERNAME!'@'localhost' IDENTIFIED BY '!DB_PASSWORD!'; GRANT ALL PRIVILEGES ON `!DB_NAME!`.* TO '!DB_USERNAME!'@'%%'; GRANT ALL PRIVILEGES ON `!DB_NAME!`.* TO '!DB_USERNAME!'@'localhost'; FLUSH PRIVILEGES;"
echo Done.
