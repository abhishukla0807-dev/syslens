@echo off
title SysLens - System Information Tool
echo.
echo Starting SysLens...
echo.
.\jre\bin\java.exe --enable-native-access=ALL-UNNAMED -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar syslens.jar %%*
echo.
pause
