@echo off
echo ========================================
echo           快速提交脚本
echo ========================================
echo.

echo 当前Git状态:
git status --short
echo.

set /p commit_msg="请输入提交信息 (直接回车使用默认信息): "

if "%commit_msg%"=="" (
    set commit_msg=daily: %date% 日常开发进度
)

echo.
echo 正在提交代码...
echo 提交信息: %commit_msg%
echo.

git add .
git commit --no-verify -m "%commit_msg%"

if %errorlevel% equ 0 (
    echo.
    echo ✅ 提交成功！
    echo.
    echo 最近3次提交记录:
    git log --oneline -3
) else (
    echo.
    echo ❌ 提交失败，请检查错误信息
)

echo.
pause