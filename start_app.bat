@echo off
echo 正在启动课程管理系统...
echo.

REM 检查是否已存在占用8080端口的进程
echo 检查8080端口占用情况...
netstat -ano | findstr :8080
if errorlevel 1 (
    echo 8080端口当前未被占用
) else (
    echo 发现8080端口被占用，正在查找占用进程...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
        echo 终止PID为 %%a 的进程...
        taskkill /pid %%a /f 2>nul
    )
)
echo.

echo 正在启动应用程序...
cd /d "C:\Users\29757\Desktop\Course-system"
call mvnw.cmd spring-boot:run

pause