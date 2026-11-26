@echo off
set ELECTRON_RUN_AS_NODE=
.\node_modules\electron\dist\electron.exe scratch\test_login_logout_flow.js > scratch\electron_stdout.txt 2> scratch\electron_stderr.txt
