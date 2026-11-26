$appjs = "c:\Users\ASUS\OneDrive\Desktop\LAKBAY_LOKAL_BACKUP\src\main\resources\web\js\app.js"
$content = Get-Content $appjs -Raw

# The carousel JS block to insert
$carouselBlock = @'

// ------------------------------------------------------------------------------
// FEATURED ECO-DESTINATION HERO CAROUSEL
// ------------------------------------------------------------------------------
const heroCarouselState = {
    photos: [
        { src: 'assets/sambawan_island.jpg',                    alt: 'Sambawan Island Aerial Panorama' },
        { src: 'assets/package_sambawan.jpg',                   alt: 'Sambawan Island Drone View' },
        { src: 'assets/sambawan_island_hero_1791699785896.jpg', alt: 'Sambawan Island Marine Sanctuary' },
        { src: 'assets/sambawan_hilltop.jpg',                   alt: 'Sambawan Hilltop Viewpoint' }
    ],
    current: 0,
    autoTimer: null
};

function initHeroCarousel() {
    const thumbsContainer = document.getElementById('hero-thumbnails-container');
    const dotsContainer   = document.getElementById('hero-dots-container');
    if (!thumbsContainer || !dotsContainer) return;

    thumbsContainer.innerHTML = '';
    dotsContainer.innerHTML   = '';

    heroCarouselState.photos.forEach(function(photo, i) {
        // Thumbnail button
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'hero-thumb-btn' + (i === 0 ? ' active' : '');
        btn.setAttribute('aria-label', 'View photo ' + (i + 1) + ': ' + photo.alt);
        (function(idx) { btn.onclick = function() { goToHeroSlide(idx); }; })(i);
        var img = document.createElement('img');
        img.src = photo.src;
        img.alt = photo.alt;
        img.onerror = (function(b) { return function() { b.style.display = 'none'; }; })(btn);
        btn.appendChild(img);
        thumbsContainer.appendChild(btn);

        // Dot indicator
        var dot = document.createElement('button');
        dot.type = 'button';
        dot.className = 'hero-dot' + (i === 0 ? ' active' : '');
        dot.setAttribute('aria-label', 'Slide ' + (i + 1));
        (function(idx) { dot.onclick = function() { goToHeroSlide(idx); }; })(i);
        dotsContainer.appendChild(dot);
    });

    updateHeroSlideUI();
    startHeroAutoplay();
}

function goToHeroSlide(index) {
    var photos = heroCarouselState.photos;
    heroCarouselState.current = (index + photos.length) % photos.length;
    updateHeroSlideUI();
    resetHeroAutoplay();
}

function nextHeroSlide() {
    goToHeroSlide(heroCarouselState.current + 1);
}

function prevHeroSlide() {
    goToHeroSlide(heroCarouselState.current - 1);
}

function updateHeroSlideUI() {
    var photos  = heroCarouselState.photos;
    var current = heroCarouselState.current;
    var img     = document.getElementById('hero-carousel-img');
    var counter = document.getElementById('hero-slide-counter');
    var thumbs  = document.querySelectorAll('.hero-thumb-btn');
    var dots    = document.querySelectorAll('.hero-dot');

    if (img) {
        img.style.opacity   = '0';
        img.style.transform = 'scale(1.03)';
        setTimeout(function() {
            img.src = photos[current].src;
            img.alt = photos[current].alt;
            img.onerror = function() { handleHeroImageFallback(img); };
            img.onload  = function() {
                img.style.opacity   = '1';
                img.style.transform = 'scale(1)';
            };
        }, 180);
    }

    if (counter) counter.textContent = (current + 1) + ' / ' + photos.length;

    thumbs.forEach(function(t, i) { t.classList.toggle('active', i === current); });
    dots.forEach(function(d, i)   { d.classList.toggle('active', i === current); });
}

function handleHeroImageFallback(img) {
    if (img) {
        img.onerror = null;
        img.style.opacity = '0';
        var bg = document.getElementById('hero-bg-layer');
        if (bg) bg.style.background = 'linear-gradient(135deg, #032219 0%, #064e3b 50%, #0d9488 100%)';
    }
}

function startHeroAutoplay() {
    clearInterval(heroCarouselState.autoTimer);
    heroCarouselState.autoTimer = setInterval(function() {
        goToHeroSlide(heroCarouselState.current + 1);
    }, 5500);
}

function resetHeroAutoplay() {
    clearInterval(heroCarouselState.autoTimer);
    startHeroAutoplay();
}

'@

# Insert after the global state block (after line 32: "const API_BASE = '/api';")
$anchor = "const API_BASE = '/api';"
if ($content.Contains($anchor)) {
    $newContent = $content.Replace($anchor, $anchor + $carouselBlock)
    Set-Content -Path $appjs -Value $newContent -NoNewline
    Write-Host "SUCCESS: Carousel JS block inserted after API_BASE declaration."
} else {
    Write-Host "ERROR: Could not find anchor string in app.js"
}
