@echo off
echo DocuMind AI - Enterprise OCR Platform
echo Developed by: DEV. DIBENDU
echo ============================================================

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo Maven found. Using Maven to build and run...
    mvn clean compile
    if %ERRORLEVEL% == 0 (
        echo Build successful! Starting application...
        mvn spring-boot:run
    ) else (
        echo Build failed. Please check the error messages above.
    )
) else (
    echo Maven not found in PATH.
    echo.
    echo To install Maven:
    echo 1. Download Maven from https://maven.apache.org/download.cgi
    echo 2. Extract to a folder (e.g., C:\apache-maven-3.9.6)
    echo 3. Add C:\apache-maven-3.9.6\bin to your PATH environment variable
    echo 4. Restart command prompt and run this script again
    echo.
    echo Alternative: Use an IDE like IntelliJ IDEA or Eclipse which has Maven built-in
    echo.
    pause
)
