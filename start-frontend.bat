@echo off
cd /d %~dp0frontend
if not exist node_modules (
  echo Installing frontend dependencies...
  npm install
  if errorlevel 1 (
    echo Frontend dependency installation failed.
    pause
    exit /b 1
  )
)
npm run dev
if errorlevel 1 (
  echo Frontend startup failed.
  pause
)
