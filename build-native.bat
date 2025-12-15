@echo off
set "VCVARS=C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvarsall.bat"
if not exist "%VCVARS%" (
  echo vcvarsall.bat not found
  exit /b 1
)
call "%VCVARS%" x64 >nul
call "%~dp0gradlew.bat" %*
