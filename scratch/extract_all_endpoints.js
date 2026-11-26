const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');

const regex = /"(\/api\/[^"]+)"\.equals\(path\)\s*&&\s*"([A-Z]+)"/g;
let match;
const endpoints = [];
while ((match = regex.exec(content)) !== null) {
    endpoints.push(`${match[2]} ${match[1]}`);
}

console.log(`Found ${endpoints.length} endpoints in ApiController:`);
endpoints.forEach(e => console.log(e));
