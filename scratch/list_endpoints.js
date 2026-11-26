const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');

const regex = /app\.(get|post|put|delete|patch)\(\s*"([^"]+)"/g;
let match;
const endpoints = [];
while ((match = regex.exec(content)) !== null) {
    endpoints.push(`${match[1].toUpperCase()} ${match[2]}`);
}

console.log(`Found ${endpoints.length} endpoints:`);
endpoints.forEach(e => console.log(e));
