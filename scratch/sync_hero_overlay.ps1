$base = "c:\Users\ASUS\OneDrive\Desktop\LAKBAY_LOKAL_BACKUP"
$webSrc = "$base\src\main\resources\web"
$webBin = "$base\bin\web"

Copy-Item -Path "$webSrc\css\style.css" -Destination "$webBin\css\style.css" -Force
Copy-Item -Path "$webSrc\js\app.js"     -Destination "$webBin\js\app.js"     -Force
Copy-Item -Path "$webSrc\index.html"   -Destination "$webBin\index.html"   -Force

Write-Host "Synced style.css, app.js, index.html to bin/web/"
