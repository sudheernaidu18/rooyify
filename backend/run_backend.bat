@echo off
title Rooyify Backend Server & Tunnel
echo ===================================================
echo          ROOYIFY BACKEND SERVER & TUNNEL
echo ===================================================
echo.
echo Starting Flask backend and Serveo SSH tunnel...
echo.
echo [INFO] Server is running on port 5000.
echo [INFO] Syncing RetrofitClient.kt with public URL...
echo.
echo ===================================================
echo.
py -u run_server_and_tunnel.py
pause
