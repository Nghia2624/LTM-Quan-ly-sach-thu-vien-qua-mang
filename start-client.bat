@echo off
echo Starting Library Management Client (TCP Mode)...
echo Client will auto-detect TCP server on localhost:8888
echo If no server found, will fall back to Local mode
echo.

cd /d "%~dp0"
java -cp target/library-management-system-1.0.0.jar com.dainam.library.LibraryManagementSystem

pause
