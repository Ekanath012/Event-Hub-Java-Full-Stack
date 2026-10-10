@echo off
REM Loads ..\.env (KEY=VALUE lines) into this window, then starts the backend.
setlocal EnableDelayedExpansion
set "ENVFILE=%~dp0..\.env"
if not exist "%ENVFILE%" (
  echo Missing %ENVFILE% - create it from .env.example first.
  exit /b 1
)
for /f "usebackq eol=# tokens=1* delims==" %%A in ("%ENVFILE%") do (
  set "%%A=%%B"
)
cd /d "%~dp0..\backend"
call mvn spring-boot:run
