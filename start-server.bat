@echo off
echo Starting Library Management TCP Server...
echo Server will run on port 8888
echo Press Ctrl+C to stop the server
echo.

cd /d "%~dp0"
java -cp target/library-management-system-1.0.0.jar com.dainam.library.server.LibraryServer

pause
