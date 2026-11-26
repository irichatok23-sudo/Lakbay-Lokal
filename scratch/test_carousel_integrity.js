// Test JS syntax and carousel execution simulation
const fs = require('fs');
const path = require('path');

const appJsPath = path.join(__dirname, '../src/main/resources/web/js/app.js');
const appJsContent = fs.readFileSync(appJsPath, 'utf8');

console.log("Checking app.js syntax...");
try {
    // Basic syntax test by evaluating carousel functions in isolation or parsing
    const vm = require('vm');
    const script = new vm.Script(appJsContent, { filename: 'app.js' });
    console.log("[PASS] app.js syntax is valid - no parse errors!");
} catch (e) {
    console.error("[FAIL] app.js syntax error:", e.message);
    process.exit(1);
}

// Check index.html carousel DOM elements
const htmlPath = path.join(__dirname, '../src/main/resources/web/index.html');
const htmlContent = fs.readFileSync(htmlPath, 'utf8');

const requiredElements = [
    'hero-showcase-card',
    'hero-carousel-img',
    'hero-carousel-controls',
    'hero-thumbnails-container',
    'hero-dots-container',
    'hero-slide-counter'
];

let allElementsFound = true;
requiredElements.forEach(id => {
    if (htmlContent.includes(id)) {
        console.log(`[PASS] Found DOM element: ${id}`);
    } else {
        console.error(`[FAIL] Missing DOM element: ${id}`);
        allElementsFound = false;
    }
});

// Check css classes
const cssPath = path.join(__dirname, '../src/main/resources/web/css/style.css');
const cssContent = fs.readFileSync(cssPath, 'utf8');

const requiredClasses = [
    '.hero-showcase-card',
    '.hero-bg-layer',
    '.hero-carousel-img',
    '.hero-overlay',
    '.hero-carousel-controls',
    '.hero-thumb-btn',
    '.hero-dot'
];

let allClassesFound = true;
requiredClasses.forEach(cls => {
    if (cssContent.includes(cls)) {
        console.log(`[PASS] Found CSS class: ${cls}`);
    } else {
        console.error(`[FAIL] Missing CSS class: ${cls}`);
        allClassesFound = false;
    }
});

if (allElementsFound && allClassesFound) {
    console.log("\nALL STATIC CAROUSEL ASSETS & DEFINITIONS VERIFIED SUCCESSFULLY!");
} else {
    process.exit(1);
}
