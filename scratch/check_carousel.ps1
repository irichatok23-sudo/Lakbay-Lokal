$appjs = "c:\Users\ASUS\OneDrive\Desktop\LAKBAY_LOKAL_BACKUP\src\main\resources\web\js\app.js"
$hits = Select-String -Path $appjs -Pattern "initHeroCarousel|heroCarouselState|nextHeroSlide|prevHeroSlide"
Write-Host "Carousel hits: $($hits.Count)"
$dom = Select-String -Path $appjs -Pattern "DOMContentLoaded"
foreach ($h in $dom) { Write-Host "DOMContentLoaded at line $($h.LineNumber)" }
$refresh = Select-String -Path $appjs -Pattern "function refreshDashboardData"
foreach ($h in $refresh) { Write-Host "refreshDashboardData at line $($h.LineNumber)" }
