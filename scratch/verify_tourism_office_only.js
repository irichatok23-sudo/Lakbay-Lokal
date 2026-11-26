const fs = require('fs');
const code = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');
const lines = code.split('\n');
const matches = [];
lines.forEach((l, i) => {
    if (l.includes('isTourismOfficeOnly')) {
        matches.push(`${i + 1}: ${l.trim()}`);
    }
});
console.log(`Found ${matches.length} occurrences:`);
matches.forEach(m => console.log(m));
