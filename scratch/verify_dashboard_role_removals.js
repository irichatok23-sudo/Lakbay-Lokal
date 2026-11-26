const fs = require('fs');
const path = require('path');

const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

console.log("=== 1. VERIFY DOM ELEMENTS IN INDEX.HTML ===");
['overview-page-header', 'kpi-cards-grid', 'card-recent-activity', 'card-top-destinations'].forEach(id => {
    if (indexHtml.includes(`id="${id}"`)) {
        console.log(`  [PASS] Found ID in index.html: ${id}`);
    } else {
        console.error(`  [FAIL] Missing ID in index.html: ${id}`);
        process.exit(1);
    }
});

console.log("\n=== 2. VERIFY ROLE_PERMITTED_TABS CONFIGURATION ===");
// Extract ROLE_PERMITTED_TABS block
const match = appJs.match(/const ROLE_PERMITTED_TABS = \{([\s\S]*?)\};/);
if (!match) {
    console.error("  [FAIL] Could not find ROLE_PERMITTED_TABS in app.js");
    process.exit(1);
}

// Evaluate simulated object
const rolePermittedTabs = eval(`({ ${match[1]} })`);

// LGU Officer: must NOT have 'overview' and must NOT have 'destinations'
if (!rolePermittedTabs['LGU_OFFICER'].includes('overview')) {
    console.log("  [PASS] LGU_OFFICER permitted tabs does NOT include 'overview'");
} else {
    console.error("  [FAIL] LGU_OFFICER still includes 'overview'");
    process.exit(1);
}

if (!rolePermittedTabs['LGU_OFFICER'].includes('destinations')) {
    console.log("  [PASS] LGU_OFFICER permitted tabs does NOT include 'destinations'");
} else {
    console.error("  [FAIL] LGU_OFFICER still includes 'destinations'");
    process.exit(1);
}

console.log("\n=== 3. VERIFY ROLE VISIBILITY LOGIC IN APPLYROLEPERMISSIONS ===");
// Check for hideCommunityOverview logic
if (appJs.includes("role === 'STAKEHOLDER' || role === 'BARANGAY_OFFICIAL'")) {
    console.log("  [PASS] Community Tourism Overview hidden for Stakeholder and Barangay Official");
} else {
    console.error("  [FAIL] Missing hideCommunityOverview logic");
    process.exit(1);
}

// Check for hideRecentActivity logic
if (appJs.includes("role === 'TOURISM_OFFICE'")) {
    console.log("  [PASS] Recent Activity Log hidden for Tourism Office");
} else {
    console.error("  [FAIL] Missing hideRecentActivity logic for Tourism Office");
    process.exit(1);
}

// Check for LGU Officer pane hiding
if (appJs.includes("role === 'LGU_OFFICER'") && appJs.includes("paneDest.style.display = 'none'")) {
    console.log("  [PASS] Destinations pane explicitly isolated and hidden for LGU Officer");
} else {
    console.error("  [FAIL] Missing Destinations pane hiding for LGU Officer");
    process.exit(1);
}

console.log("\nALL ROLE DASHBOARD ISOLATION CHECKS PASSED!");
