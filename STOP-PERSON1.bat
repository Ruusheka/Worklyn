@echo off
cd /d "%~dp0"
docker compose down
echo.
echo Containers stopped. MongoDB and Redis data volumes are preserved.
pause
