@echo off
set ELECTRON_RUN_AS_NODE=
.\node_modules\electron\dist\electron.exe scratch\test_left_panel_ui.js --no-sandbox --disable-gpu > scratch\left_panel_test.log 2>&1
type scratch\left_panel_test.log
