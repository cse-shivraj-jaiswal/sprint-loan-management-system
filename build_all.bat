@echo off
echo ============================================
echo 🚀 FINFLOW LOCAL BUILD SCRIPT (8GB RAM OPTIMIZED)
echo ============================================

set SERVICES=config-server discovery-server auth-service application-service document-service admin-service api-gateway

FOR %%s IN (%SERVICES%) DO (
    echo.
    echo 🛠️  Building %%s...
    pushd %%s
    call mvnw.cmd clean package -DskipTests
    if errorlevel 1 (
        echo ❌ Build failed for %%s
        popd
        exit /b 1
    )
    popd
)

echo.
echo ✅ ALL SERVICES BUILT SUCCESSFULLY!
echo Now you can run: docker-compose up -d --build
