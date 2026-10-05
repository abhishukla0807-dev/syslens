@echo off
setlocal enabledelayedexpansion

title Building SysLens Executables...
echo =======================================================
echo          Building SysLens Windows Executable
echo =======================================================
echo.

cd /d "%~dp0"

echo [1/4] Compiling Java Fat JAR with Maven...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/4] Updating release/syslens distribution...
if not exist "release\syslens" mkdir "release\syslens"
copy /y "target\syslens.jar" "release\syslens\syslens.jar" >nul

echo.
echo [3/4] Compiling native Windows Launcher (SysLens.exe)...
set CSC_PATH=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe
if exist "%CSC_PATH%" (
    "%CSC_PATH%" /nologo /target:exe /win32icon:launcher\syslens.ico /out:SysLens.exe launcher\SysLensLauncher.cs
    copy /y "SysLens.exe" "release\syslens\SysLens.exe" >nul
    echo       Created: .\SysLens.exe
    echo       Created: .\release\syslens\SysLens.exe
) else (
    echo [WARN] CSC compiler not found at %CSC_PATH%. Skipping launcher compilation.
)

echo.
echo [4/4] Building standalone portable package (SysLens with bundled runtime)...
if exist "release\syslens\jre" (
    if not exist "target\pkg-tmp" mkdir "target\pkg-tmp"
    copy /y "target\syslens.jar" "target\pkg-tmp\syslens.jar" >nul
    
    if exist "release\standalone" rmdir /s /q "release\standalone"
    
    jpackage --type app-image ^
        --name SysLens ^
        --input "target\pkg-tmp" ^
        --main-jar syslens.jar ^
        --main-class com.aurexiris.syslens.Main ^
        --icon "launcher\syslens.ico" ^
        --runtime-image "release\syslens\jre" ^
        --dest "release\standalone" ^
        --win-console ^
        --java-options "--enable-native-access=ALL-UNNAMED" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --java-options "-Dorg.slf4j.simpleLogger.defaultLogLevel=off"
        
    rmdir /s /q "target\pkg-tmp"
    echo       Created: .\release\standalone\SysLens\SysLens.exe
) else (
    echo [INFO] Bundled JRE not detected for jpackage, skipping standalone bundle.
)

echo.
echo =======================================================
echo               Build Successful!
echo =======================================================
echo.
echo You can now run SysLens with one click:
echo   1. Direct Launcher:  Double-click .\SysLens.exe
echo   2. Bundled Release:  Double-click .\release\syslens\SysLens.exe
echo   3. Standalone App:   Double-click .\release\standalone\SysLens\SysLens.exe
echo.
pause
