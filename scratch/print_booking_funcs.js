const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

// Print openViewBookingModal in full
const start = js.indexOf('function openViewBookingModal');
const end = js.indexOf('function updateBookingStatus', start);
console.log('--- openViewBookingModal ---');
console.log(js.substring(start, end));

// Print renderScheduleTableRows
const schStart = js.indexOf('function renderScheduleTableRows');
const schEnd = js.indexOf('function loadStakeholderEarnings', schStart);
console.log('--- renderScheduleTableRows ---');
console.log(js.substring(schStart, schEnd));
