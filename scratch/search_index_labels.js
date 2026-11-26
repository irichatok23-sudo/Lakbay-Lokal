const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
const pats = [
    /page-title/,
    /Stakeholders Directory|Stakeholder Verification|Stakeholder Accreditation/,
    /pane-stakeholders/,
    /Local Stakeholder/,
    /LGU Officer/,
    /MODULE.*STAKEHOLDER/,
    /id="pane-stakeholders"/
];
lines.forEach((l, i) => {
    if (pats.some(p => p.test(l))) console.log((i+1) + ': ' + l.trim());
});
