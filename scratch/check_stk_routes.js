const fs = require('fs');
const code = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');
const lines = code.split('\n');
lines.forEach((l, i) => {
    if (l.includes('/api/stakeholder')) {
        console.log((i+1) + ': ' + l.trim());
    }
});
