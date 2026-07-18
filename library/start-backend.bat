@echo off
chcp 65001 >nul
echo ========================================
echo   📚 图书管理系统 - 后端启动
echo ========================================
echo.
echo 正在启动后端服务 (端口 8080)...
echo 日志将输出到 backend.log
echo.

cd /d "D:\项目\图书管理系统\（Web后端版）\library"
start /b .\mvnw.cmd spring-boot:run > backend.log 2>&1

echo.
echo ✅ 后端已启动！等待服务就绪...
echo ⏳ 大约需要 10-20 秒
echo.
echo 📋 查看日志: type backend.log
echo 🛑 停止服务: 按 Ctrl+C 或关闭此窗口
echo.
pause