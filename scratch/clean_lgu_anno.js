const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
console.log('has lgu-announcements-card:', html.includes('lgu-announcements-card'));
if (html.includes('lgu-announcements-card')) {
    const clean = html.replace(/<!-- Municipal Announcements & Advisories Feed -->[\s\S]*?<\/div>\s*<\/div>/g, '');
    fs.writeFileSync('src/main/resources/web/index.html', clean, 'utf8');
    fs.writeFileSync('bin/web/index.html', clean, 'utf8');
    console.log('Removed lgu-announcements-card. Now has:', clean.includes('lgu-announcements-card'));
}
