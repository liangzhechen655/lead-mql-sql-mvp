@echo off
cd /d %~dp0backend
mvn spring-boot:run
if errorlevel 1 (
  echo Backend startup failed.
  pause
)
