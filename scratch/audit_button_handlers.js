const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== Checking all button onclick handlers and elements ===');

// Check every onclick in index.html
const onclickRegex = /onclick=["']([^"']+)["']/gi;
let m;
const onclicks = new Set();
while ((m = onclickRegex.exec(html)) !== null) {
    onclicks.add(m[1].trim());
}

console.log(`Total unique onclick attributes in index.html: ${onclicks.size}`);
const brokenStatic = [];
onclicks.forEach(oc => {
    const fnName = oc.split('(')[0].trim();
    if (!js.includes(`function ${fnName}`) && !js.includes(`${fnName} =`) && !js.includes(`${fnName}(`)) {
        brokenStatic.push(oc);
    }
});

console.log('Broken static onclicks:', brokenStatic.length > 0 ? brokenStatic : 'NONE');

// Check dynamic buttons inside app.js
const dynamicOnclickRegex = /onclick=\\?["']([a-zA-Z0-9_]+)\(/g;
const dynamicOnclicks = new Set();
while ((m = dynamicOnclickRegex.exec(js)) !== null) {
    dynamicOnclicks.add(m[1]);
}
console.log(`\nDynamic onclick functions called in app.js: ${dynamicOnclicks.size}`);
const brokenDynamic = [];
dynamicOnclicks.forEach(fn => {
    const fnDef = new RegExp(`function\\s+${fn}\\b|window\\.${fn}\\s*=|const\\s+${fn}\\s*=|let\\s+${fn}\\s*=`);
    if (!fnDef.test(js)) {
        brokenDynamic.push(fn);
    }
});
console.log('Broken dynamic onclicks:', brokenDynamic.length > 0 ? brokenDynamic : 'NONE');

// Check all modal ids referenced vs exist in html
const openModalRegex = /openModal\(['"]([^'"]+)['"]\)/g;
const modalCalls = new Set();
while ((m = openModalRegex.exec(js)) !== null) {
    modalCalls.add(m[1]);
}
console.log(`\nModals targeted by openModal() in app.js: ${modalCalls.size}`);
const missingModals = [];
modalCalls.forEach(modId => {
    if (!html.includes(`id="${modId}"`) && !html.includes(`id='${modId}'`)) {
        missingModals.push(modId);
    }
});
console.log('Missing Modal elements:', missingModals.length > 0 ? missingModals : 'NONE');

// Check closeModal calls
const closeModalRegex = /closeModal\(['"]([^'"]+)['"]\)/g;
const closeModalCalls = new Set();
while ((m = closeModalRegex.exec(js)) !== null) {
    closeModalCalls.add(m[1]);
}
while ((m = closeModalRegex.exec(html)) !== null) {
    closeModalCalls.add(m[1]);
}
console.log(`\nModals targeted by closeModal(): ${closeModalCalls.size}`);
const missingCloseModals = [];
closeModalCalls.forEach(modId => {
    if (!html.includes(`id="${modId}"`) && !html.includes(`id='${modId}'`)) {
        missingCloseModals.push(modId);
    }
});
console.log('Missing Close Modal targets:', missingCloseModals.length > 0 ? missingCloseModals : 'NONE');
