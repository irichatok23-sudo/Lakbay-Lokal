@echo off
set ELECTRON_RUN_AS_NODE=
.\node_modules\electron\dist\electron.exe scratch\diagnose_real_click.js --no-sandbox --disable-gpu > scratch\diagnose.log 2>&1
type scratch\diagnose.log
