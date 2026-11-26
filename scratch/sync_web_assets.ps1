Copy-Item -Path 'src/main/resources/web/index.html' -Destination 'bin/web/index.html' -Force
Copy-Item -Path 'src/main/resources/web/js/app.js' -Destination 'bin/web/js/app.js' -Force
Copy-Item -Path 'src/main/resources/web/css/style.css' -Destination 'bin/web/css/style.css' -Force
Write-Host "All web assets synced to bin/web successfully."
