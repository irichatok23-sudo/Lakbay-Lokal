const fs = require('fs');

const test1 = fs.readFileSync('scratch/test_role_destinations.js', 'utf8');
const test2 = fs.readFileSync('scratch/test_stakeholder_verification.js', 'utf8');

console.log("test_role_destinations mentions of overview or ROLE_PERMITTED_TABS:");
test1.split('\n').forEach((l, i) => {
    if (l.includes('overview') || l.includes('ROLE_PERMITTED_TABS')) console.log(`  Line ${i+1}: ${l.trim()}`);
});

console.log("test_stakeholder_verification mentions of overview or ROLE_PERMITTED_TABS:");
test2.split('\n').forEach((l, i) => {
    if (l.includes('overview') || l.includes('ROLE_PERMITTED_TABS')) console.log(`  Line ${i+1}: ${l.trim()}`);
});
