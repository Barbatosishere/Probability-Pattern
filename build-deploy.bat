@echo off
chcp 65001 >nul
echo ========================================
echo   Probability Pattern 编译部署脚本
echo ========================================
echo.

cd /d "%~dp0"

:: 设置项目内 Gradle 缓存目录
set GRADLE_USER_HOME=%~dp0.gradle-home

:: Gradle 路径
set GRADLE_BIN=%~dp0.gradle-home\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\bin\gradle.bat

:: 游戏 mods 目录
set MODS_DIR=D:\minecraft modify pcl\.minecraft\versions\1.21.1-NeoForge_21.1.236\mods
set MOD_JAR=build\libs\probabilitypattern-0.1.0.jar

echo [0/3] 清理旧构建产物...
if exist "%MOD_JAR%" del /F /Q "%MOD_JAR%"
if exist "%MODS_DIR%\probabilitypattern-0.1.0.jar" del /F /Q "%MODS_DIR%\probabilitypattern-0.1.0.jar"
echo     已清理旧 jar 文件
echo.

echo [1/3] 开始编译模组...
echo.

call "%GRADLE_BIN%" build --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 编译失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 部署模组到游戏目录...
echo     目标: %MODS_DIR%
echo.

copy /Y "%MOD_JAR%" "%MODS_DIR%\"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 部署失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo   编译部署完成！
echo   模组文件: probabilitypattern-0.1.0.jar
echo ========================================
echo.
if "%1"=="" pause
