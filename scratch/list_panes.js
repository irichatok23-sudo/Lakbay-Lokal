const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

const regex = /<div class="main-content-pane[^"]*" id="([^"]+)">/g;
let match;
console.log('Main panes in index.html:');
while ((match = regex.exec(content)) !== null) {
    console.log(`- ${match[1]}`);
}
