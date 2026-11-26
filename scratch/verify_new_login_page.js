const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');

console.log("=== VERIFY LOGIN PAGE REQUIREMENTS ===");

// 1. Background photograph
if (css.includes("assets/sambawan_island_hero_1791699785896.jpg")) {
    console.log("  [PASS] Sambawan Island photograph used as background");
} else {
    console.error("  [FAIL] Missing Sambawan Island background");
    process.exit(1);
}

// 2. Subtle white translucent overlay
if (css.includes("background: rgba(255, 255, 255, 0.52);")) {
    console.log("  [PASS] Subtle white translucent overlay configured");
} else {
    console.error("  [FAIL] Missing subtle white translucent overlay");
    process.exit(1);
}

// 3. Left Section elements
const leftTexts = [
    'LAKBAY LOKAL',
    'Biliran Community Tourism System',
    'Mabuhay &amp; Welcome!',
    'A centralized platform empowering local tour guides, boat operators, habal-habal drivers, homestay cooks, and artisans across Biliran Province, Philippines.'
];
leftTexts.forEach(t => {
    if (html.includes(t)) {
        console.log(`  [PASS] Left section text present: ${t.substring(0, 30)}...`);
    } else {
        console.error(`  [FAIL] Left section missing: ${t}`);
        process.exit(1);
    }
});

// 4. Right Section elements
const rightElements = [
    'Sign In to Lakbay Lokal',
    'glass-login-username',
    'glass-login-password',
    'btn-toggle-pwd',
    'glass-remember-me',
    'openForgotPasswordModal()',
    'btn-glass-submit',
    "openRegisterModal('tourist')"
];
rightElements.forEach(el => {
    if (html.includes(el)) {
        console.log(`  [PASS] Right section element present: ${el}`);
    } else {
        console.error(`  [FAIL] Right section missing: ${el}`);
        process.exit(1);
    }
});

console.log("\nALL LOGIN PAGE REQUIREMENTS VERIFIED!");
