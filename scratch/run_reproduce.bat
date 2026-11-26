@echo off
set "ELECTRON_RUN_AS_NODE="
".\node_modules\electron\dist\electron.exe" --no-sandbox --disable-gpu "%~dp0electron_reproduce_bug.js"
