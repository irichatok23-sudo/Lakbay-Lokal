@echo off
set ELECTRON_RUN_AS_NODE=
.\node_modules\electron\dist\electron.exe scratch\test_all_six_roles_lifecycle.js --no-sandbox --disable-gpu > scratch\all_six_roles.log 2>&1
type scratch\all_six_roles.log
