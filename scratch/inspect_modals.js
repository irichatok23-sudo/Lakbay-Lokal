const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const openModalIdx = appJs.indexOf('function openModal(');
console.log('=== openModal ===');
console.log(appJs.substring(openModalIdx, openModalIdx + 600));

const closeModalIdx = appJs.indexOf('function closeModal(');
console.log('\n=== closeModal ===');
console.log(appJs.substring(closeModalIdx, closeModalIdx + 600));
