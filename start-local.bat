@echo off
chcp 65001 >nul
title 图书管理系统 - 本地启动

echo ========================================
echo   📚 图书管理系统 - 本地启动
echo ========================================
echo.

echo [1/3] 正在启动后端 (端口 8080)...
start "Backend" cmd /c "cd /d D:\项目\图书管理系统\（Web后端版）\library && .\mvnw spring-boot:run"

echo [2/3] 正在启动前端 (端口 5173)...
echo 等待后端启动...
timeout /t 6 /nobreak >nul
start "Frontend" cmd /c "cd /d D:\项目\图书管理系统\（Web后端版）\library-frontend && npm run dev"

echo [3/3] 正在打开浏览器...
timeout /t 5 /nobreak >nul
start http://localhost:5173

echo.
echo ✅ 所有服务已启动！
echo.
echo 📋 访问地址: http://localhost:5173
echo 📋 后端接口: http://localhost:8080
echo.
echo ⚠️  关闭窗口不会停止服务，需要在任务管理器中结束 Java 进程。
echo.
pause