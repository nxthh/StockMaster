@echo off
call mvn clean package || exit /b 1
if exist dist rmdir /s /q dist
jpackage --type app-image --name StockMaster --icon packaging\StockMaster.ico --input target --main-jar inventory.jar --main-class com.inventory.Launcher --dest dist
echo.
echo Done: dist\StockMaster\StockMaster.exe