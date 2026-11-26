const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const permittedTabs = appJs.match(/const ROLE_PERMITTED_TABS = \{[\s\S]*?\};/)[0];
console.log('Permitted Tabs Configuration:\n', permittedTabs);

// Check button add announcement permission
console.log('\nbtnAddAnnounce condition in applyRolePermissions:');
const btnCheck = appJs.match(/btnAddAnnounce\.style\.display = [^;]+;/)[0];
console.log(btnCheck);

// Check card actions condition in renderAnnouncements
console.log('\ncanManage in renderAnnouncements:');
const canManage = appJs.match(/const canManage = [^;]+;/)[0];
console.log(canManage);
