@echo off
title SysLens - System Information Tool
cd /d "%~dp0"
echo.
echo Starting SysLens...
echo.
jre\bin\java.exe --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar syslens.jar %*
