@echo off
echo ========================================
echo           Git Commit
echo ========================================
echo.

echo Git Stuta:
git status --short
echo.

set /p commit_msg="Please Enter The Commit Message"

if "%commit_msg%"=="" (
    set commit_msg=daily: %date% Daily Process)

echo.
echo Code Committing...
echo Commit Message: %commit_msg%
echo.

git add .
git commit --no-verify -m "%commit_msg%"

if %errorlevel% equ 0 (
    echo.
    echo  Success！！！
    echo.
    echo Recently log:
    git log --oneline -3
) else (
    echo.
    echo  Failed
)

echo.
pause