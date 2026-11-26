/**
 * Full Audit Script — reads app.js and index.html to identify
 * all buttons, their handlers, and gaps
 */
const fs = require('fs');
const path = require('path');

const BASE = 'c:/Users/ASUS/OneDrive/Desktop/LAKBAY_LOKAL_BACKUP';
const appJs = fs.readFileSync(path.join(BASE, 'src/main/resources/web/js/app.js'), 'utf8');
const indexHtml = fs.readFileSync(path.join(BASE, 'src/main/resources/web/index.html'), 'utf8');

// Extract all defined function names from app.js
const funcDefs = new Set();
const funcDefRegex = /(?:^|\n)\s*(?:async\s+)?function\s+(\w+)\s*\(/g;
let m;
while ((m = funcDefRegex.exec(appJs)) !== null) {
    funcDefs.add(m[1]);
}
console.log(`\n=== FUNCTIONS DEFINED (${funcDefs.size}) ===`);
[...funcDefs].sort().forEach(f => console.log('  ' + f));

// Extract all onclick/onsubmit/oninput handler references in HTML
const handlerRefs = new Set();
const onAttrRegex = /on(?:click|submit|change|input|keyup|keydown|mouseover|focus|blur)="([^"]+)"/g;
while ((m = onAttrRegex.exec(indexHtml)) !== null) {
    // Extract function names from handler strings
    const fnNames = m[1].match(/([a-zA-Z_$][a-zA-Z0-9_$]*)\s*\(/g);
    if (fnNames) fnNames.forEach(fn => handlerRefs.add(fn.replace('(', '')));
}

// Also find inline onclick/onsubmit in app.js (dynamic)
const jsHandlerRegex = /on(?:click|submit|change|input)\s*=\s*[`'"]([^`'"]+)[`'"]/g;
while ((m = jsHandlerRegex.exec(appJs)) !== null) {
    const fnNames = m[1].match(/([a-zA-Z_$][a-zA-Z0-9_$]*)\s*\(/g);
    if (fnNames) fnNames.forEach(fn => handlerRefs.add(fn.replace('(', '')));
}

console.log(`\n=== HANDLER REFERENCES FOUND (${handlerRefs.size}) ===`);
// Find missing functions
const missing = [...handlerRefs].filter(ref => !funcDefs.has(ref) && ref.length > 2);
console.log(`\n=== MISSING FUNCTIONS (${missing.length}) ===`);
missing.sort().forEach(f => console.log('  MISSING: ' + f));

// Find functions defined but never called
const calls = new Set();
const callRegex = /([a-zA-Z_$][a-zA-Z0-9_$]*)\s*\(/g;
while ((m = callRegex.exec(appJs + indexHtml)) !== null) {
    calls.add(m[1]);
}
const neverCalled = [...funcDefs].filter(f => !calls.has(f));
console.log(`\n=== DEFINED BUT POSSIBLY ORPHANED (${neverCalled.length}) ===`);
neverCalled.forEach(f => console.log('  ORPHAN: ' + f));

// Check specific key gaps from summary
const keyFunctions = [
    'openViewDestinationModal',
    'openViewBookingModal', 
    'openUserProfileModal',
    'openLoginModal',
    'handleGlassLoginSubmit',
    'confirmLogout',
    'logout',
    'openEditServiceModal',
    'openEditAnnouncementModal',
    'deleteAnnouncement',
    'openEditDestinationModal',
    'deleteDestination',
    'openEditPackageModal',
    'deletePackage',
    'deleteServiceOffering',
    'exportReportCSV',
    'printReport',
    'triggerDbBackup',
    'openEditUserModal',
    'openAdminResetPasswordModal',
    'toggleUserStatus',
    'updateBookingStatus',
    'updateBookingItemStatus',
    'openVerificationModal',
    'openEditStakeholderModal',
    'openMyStakeholderProfileModal',
    'handleLogout',
    'filterDestinations',
    'handlePublicSearch',
    'filterPublicCategory',
    'openReviewModalForBooking',
    'openPackageBookingModal',
    'openServiceBookingModal',
    'openRegisterStakeholderModal',
    'openStakeholderHistoryModal',
    'openStakeholderResubmitModal',
    'loadAuditLogs',
    'saveSystemSettings',
    'openCreateUserModal',
    'openAddDestinationModal',
    'openAddServiceModal',
    'openAddAnnouncementModal',
    'openAddPackageModal',
    'openEditBarangayModal',
    'openArrivalEntryModal',
    'validateDestination',
    'submitCustomBooking',
];

console.log('\n=== KEY FUNCTION STATUS ===');
keyFunctions.forEach(fn => {
    const defined = funcDefs.has(fn);
    const called = calls.has(fn);
    console.log(`  ${defined ? 'OK  ' : 'MISS'} | ${called ? 'called' : 'unused'} | ${fn}`);
});

// Scan for openLoginModal usage
console.log('\n=== openLoginModal USAGE ===');
const loginModalLines = (appJs + indexHtml).split('\n').filter(l => l.includes('openLoginModal'));
loginModalLines.forEach(l => console.log('  ' + l.trim()));

// Check for view-destination and view-booking modals in HTML
console.log('\n=== MODAL IDs IN HTML ===');
const modalIds = [...indexHtml.matchAll(/id="(modal-[^"]+)"/g)].map(m => m[1]);
modalIds.forEach(id => console.log('  ' + id));

// Check for profile modal
console.log('\n=== PROFILE-RELATED ELEMENTS ===');
const profileLines = indexHtml.split('\n').filter(l => l.includes('profile') || l.includes('Profile'));
profileLines.slice(0, 20).forEach(l => console.log('  ' + l.trim().substring(0, 100)));

console.log('\n\n=== AUDIT COMPLETE ===');
