const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const regex = /function\s+([a-zA-Z0-9_]+)\s*\(/g;
let match;
const funcs = [];
while ((match = regex.exec(content)) !== null) {
    funcs.push(match[1]);
}
console.log(`Total functions in app.js: ${funcs.length}`);

// Group by topic
const groups = {
    destinations: funcs.filter(f => /dest/i.test(f)),
    stakeholders: funcs.filter(f => /stakeholder|worker/i.test(f)),
    announcements: funcs.filter(f => /announce|event/i.test(f)),
    reviews: funcs.filter(f => /review|rate|rating/i.test(f)),
    reports: funcs.filter(f => /report|kpi|stat|analytic/i.test(f)),
    bookings: funcs.filter(f => /book/i.test(f)),
    auth: funcs.filter(f => /login|auth|user|logout/i.test(f))
};

for (const [k, v] of Object.entries(groups)) {
    console.log(`\n-- ${k} (${v.length}) --\n` + v.join(', '));
}
