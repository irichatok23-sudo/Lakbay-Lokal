const fs = require('fs');

const appJsPath = 'src/main/resources/web/js/app.js';
const indexHtmlPath = 'src/main/resources/web/index.html';

let appJs = fs.readFileSync(appJsPath, 'utf8');
let indexHtml = fs.readFileSync(indexHtmlPath, 'utf8');

console.log('--- Checking Priority 1 in app.js ---');
console.log('TOURIST has announcements:', appJs.includes("'destinations', 'announcements', 'services', 'packages', 'bookings', 'reviews'"));
console.log('LGU_OFFICER has destinations:', appJs.includes("'destinations', 'announcements', 'stakeholders', 'bookings'"));
console.log('LGU_OFFICER destinations unblocked:', !appJs.includes("if (role === 'LGU_OFFICER') {\n            paneDest.classList.remove('active');\n            paneDest.style.display = 'none';"));
