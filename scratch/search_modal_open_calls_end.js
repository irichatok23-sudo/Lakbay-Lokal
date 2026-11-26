const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

['openViewDestinationModal', 'openViewBookingModal', 'openUserProfileModal'].forEach(fn => {
    const idx = appJs.indexOf(`function ${fn}`);
    console.log(`\n=== End of ${fn} ===`);
    console.log(appJs.substring(idx + 600, idx + 1200));
});
