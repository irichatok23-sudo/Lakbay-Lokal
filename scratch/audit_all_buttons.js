const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const apiCode = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');

console.log('=== 1. Static Buttons in index.html ===');
const buttonRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
let match;
const staticButtons = [];
while ((match = buttonRegex.exec(html)) !== null) {
    const attrs = match[1];
    const text = match[2].replace(/<[^>]+>/g, '').trim().replace(/\s+/g, ' ');
    const idMatch = attrs.match(/id=["']([^"']+)["']/i);
    const onclickMatch = attrs.match(/onclick=["']([^"']+)["']/i);
    const typeMatch = attrs.match(/type=["']([^"']+)["']/i);
    const classMatch = attrs.match(/class=["']([^"']+)["']/i);

    staticButtons.push({
        id: idMatch ? idMatch[1] : '(no-id)',
        type: typeMatch ? typeMatch[1] : 'button',
        class: classMatch ? classMatch[1] : '',
        onclick: onclickMatch ? onclickMatch[1] : '(no-onclick)',
        text: text || '(icon/html)'
    });
}
console.log(`Found ${staticButtons.length} static buttons in index.html`);

console.log('\n=== 2. Forms in index.html ===');
const formRegex = /<form\b([^>]*)>/gi;
const forms = [];
while ((match = formRegex.exec(html)) !== null) {
    const attrs = match[1];
    const idMatch = attrs.match(/id=["']([^"']+)["']/i);
    const onsubmitMatch = attrs.match(/onsubmit=["']([^"']+)["']/i);
    forms.push({
        id: idMatch ? idMatch[1] : '(no-id)',
        onsubmit: onsubmitMatch ? onsubmitMatch[1] : '(no-onsubmit)'
    });
}
console.log(`Found ${forms.length} forms in index.html:`);
forms.forEach(f => console.log(`  Form: ${f.id} -> onsubmit: ${f.onsubmit}`));

console.log('\n=== 3. Dynamic Buttons in app.js ===');
const dynamicBtnRegex = /<button\b([^>]*)>([\s\S]*?)<\/button>/gi;
const dynamicButtons = [];
while ((match = dynamicBtnRegex.exec(js)) !== null) {
    const attrs = match[1];
    const text = match[2].replace(/<[^>]+>/g, '').trim().replace(/\s+/g, ' ');
    const onclickMatch = attrs.match(/onclick=["']([^"']+)["']/i) || attrs.match(/onclick=\\?["']([^"'\\]+)/i);
    dynamicButtons.push({
        onclick: onclickMatch ? onclickMatch[1] : '(no-onclick)',
        text: text.substring(0, 40)
    });
}
console.log(`Found ${dynamicButtons.length} dynamic buttons generated in app.js`);

console.log('\n=== 4. Checking Onclick Handlers in app.js ===');
const allOnclicks = new Set();
staticButtons.forEach(b => {
    if (b.onclick !== '(no-onclick)') {
        const fnName = b.onclick.split('(')[0].trim();
        allOnclicks.add(fnName);
    }
});
forms.forEach(f => {
    if (f.onsubmit !== '(no-onsubmit)') {
        const fnName = f.onsubmit.split('(')[0].trim();
        allOnclicks.add(fnName);
    }
});
dynamicButtons.forEach(b => {
    if (b.onclick !== '(no-onclick)') {
        const fnName = b.onclick.split('(')[0].trim();
        allOnclicks.add(fnName);
    }
});

const missingFns = [];
const existingFns = [];
allOnclicks.forEach(fn => {
    // Check if function defined in app.js
    const fnRegex = new RegExp(`function\\s+${fn}\\b|window\\.${fn}\\s*=|const\\s+${fn}\\s*=|let\\s+${fn}\\s*=`, 'i');
    if (fnRegex.test(js)) {
        existingFns.push(fn);
    } else {
        missingFns.push(fn);
    }
});

console.log(`Existing JS functions (${existingFns.length}):`, existingFns.join(', '));
console.log(`MISSING JS functions (${missingFns.length}):`, missingFns.join(', ') || 'NONE');
