const fs = require('fs');
const content = fs.readFileSync('sql/02_seed_data.sql', 'utf8');

const s = content.indexOf('INSERT INTO destinations');
console.log(content.substring(s, s + 1500));
