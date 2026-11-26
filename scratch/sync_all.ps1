$base    = "c:\Users\ASUS\OneDrive\Desktop\LAKBAY_LOKAL_BACKUP"
$webSrc  = "$base\src\main\resources\web"
$webBin  = "$base\bin\web"
$assetSrc = "$webSrc\assets"
$assetBin = "$webBin\assets"

# 1. Copy uploaded images to assets (sambawan photos already there from last session)
$uploads = @{
    # New images from this session (the ones shown in-chat)
    # rice terraces -> iyusan
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795146059849_d667e953.jpg" = "$assetSrc\iyusan_rice_terraces.jpg"
    # hot spring -> mainit
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795146103540_f4c68b1d.jpg" = "$assetSrc\mainit_hot_spring.jpg"
    # sambawan island marine sanctuary (the hilltop/coast view)
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795146831084_b405ba72.jpg" = "$assetSrc\sambawan_hilltop.jpg"
    # biliran waterfalls eco-trek
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795147252375_4b4353c9.webp" = "$assetSrc\package_biliran_waterfalls.webp"
    # higatangan sandbar
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795147278041_d49f7a6d.jpg" = "$assetSrc\package_higatangan_sandbar.jpg"
    # marapipi pottery
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795147331250_0463b3f6.jpg" = "$assetSrc\package_marapipi_pottery.jpg"
    # sambawan package image (new aerial view for carousel)
    "C:\Users\ASUS\.gemini\antigravity\brain\7986cd7b-4958-4ec4-b601-1c86e1dcd7aa\.user_uploaded\media_1795147370393_5f068a78.jpg" = "$assetSrc\package_sambawan.jpg"
}

Write-Host "=== COPYING UPLOADED IMAGES TO ASSETS ==="
foreach ($src in $uploads.Keys) {
    $dst = $uploads[$src]
    if (Test-Path $src) {
        Copy-Item -Path $src -Destination $dst -Force
        $size = (Get-Item $dst).Length
        Write-Host "  OK  $(Split-Path $dst -Leaf) ($size bytes)"
    } else {
        Write-Host "  MISSING  $src"
    }
}

# 2. Verify carousel photo files exist in src assets
Write-Host ""
Write-Host "=== VERIFYING CAROUSEL PHOTOS ==="
$carouselPhotos = @(
    "sambawan_island.jpg",
    "package_sambawan.jpg",
    "sambawan_island_hero_1791699785896.jpg",
    "sambawan_hilltop.jpg"
)
$allOk = $true
foreach ($f in $carouselPhotos) {
    $path = "$assetSrc\$f"
    if (Test-Path $path) {
        $size = (Get-Item $path).Length
        Write-Host "  FOUND  $f ($size bytes)"
    } else {
        Write-Host "  MISSING  $f"
        $allOk = $false
    }
}

# 3. Sync web files to bin/
Write-Host ""
Write-Host "=== SYNCING WEB FILES TO bin/ ==="
Copy-Item -Path "$webSrc\index.html"       -Destination "$webBin\index.html"       -Force
Copy-Item -Path "$webSrc\css\style.css"    -Destination "$webBin\css\style.css"    -Force
Copy-Item -Path "$webSrc\js\app.js"        -Destination "$webBin\js\app.js"        -Force
Write-Host "  Synced: index.html, style.css, app.js"

# Sync all assets to bin/assets
if (-not (Test-Path $assetBin)) { New-Item -ItemType Directory -Path $assetBin | Out-Null }
Get-ChildItem -Path $assetSrc -File | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "$assetBin\$($_.Name)" -Force
}
$assetCount = (Get-ChildItem $assetBin -File).Count
Write-Host "  Synced: $assetCount asset files to bin/web/assets/"

# 4. Verify initHeroCarousel in app.js
Write-Host ""
Write-Host "=== VERIFYING CAROUSEL JS IN app.js ==="
$hits = Select-String -Path "$webSrc\js\app.js" -Pattern "initHeroCarousel|heroCarouselState|nextHeroSlide|prevHeroSlide"
Write-Host "  Carousel function hits: $($hits.Count)"
foreach ($h in $hits) { Write-Host "    Line $($h.LineNumber): $($h.Line.Trim())" }

Write-Host ""
if ($allOk) { Write-Host "ALL CHECKS PASSED" } else { Write-Host "SOME CHECKS FAILED - see above" }
