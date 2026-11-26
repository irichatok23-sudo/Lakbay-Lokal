const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== SEARCH FOR redirectToRoleHomepage ===');
const roleHpIdx = appJs.indexOf('function redirectToRoleHomepage');
console.log(appJs.substring(roleHpIdx, roleHpIdx + 1500));

console.log('\n=== SEARCH FOR showPublicPortal ===');
const pubPortalIdx = appJs.indexOf('function showPublicPortal');
console.log(appJs.substring(pubPortalIdx, pubPortalIdx + 1000));

console.log('\n=== SEARCH FOR showManagementPortal ===');
const mgmtPortalIdx = appJs.indexOf('function showManagementPortal');
if (mgmtPortalIdx !== -1) {
    console.log(appJs.substring(mgmtPortalIdx, mgmtPortalIdx + 1000));
} else {
    console.log('No showManagementPortal function');
}
