@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-Worklyn-Person1.ps1"
set EXITCODE=%ERRORLEVEL%
echo.
if not "%EXITCODE%"=="0" echo Startup failed with exit code %EXITCODE%. Read the error above.
pause
exit /b %EXITCODE%
