const fs = require('fs');

function grep(file, pattern) {
    const lines = fs.readFileSync(file, 'utf8').split('\n');
    const re = new RegExp(pattern, 'i');
    lines.forEach((line, i) => {
        if (re.test(line)) console.log(`${i+1}: ${line.trimEnd()}`);
    });
}

console.log('=== app.js: formatCategory ===');
grep('src/main/resources/web/js/app.js', 'formatCategory');

console.log('\n=== app.js: roleName display ===');
grep('src/main/resources/web/js/app.js', 'roleName|roleCode.*format|format.*roleCode');

console.log('\n=== index.html: Stakeholder (nav labels) ===');
grep('src/main/resources/web/index.html', 'Stakeholders Directory|Stakeholder Verification|LGU.*Tourism|Community Worker|Municipal Tourism');

console.log('\n=== index.html: role-select/register labels ===');
grep('src/main/resources/web/index.html', 'option.*STAKEHOLDER|option.*LGU_OFFICER|Stakeholder.*option|LGU.*option');
