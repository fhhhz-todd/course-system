@echo off
echo 正在释放8080端口...
net stop http 2>nul
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
echo 8080端口已释放
pause