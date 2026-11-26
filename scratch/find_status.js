const fs = require('fs');
const lines = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('handleUpdateUserStatus')) {
        console.log(`Line ${i + 1}: ${l}`);
        console.log(lines.slice(i, i + 25).join('\n'));
    }
});
