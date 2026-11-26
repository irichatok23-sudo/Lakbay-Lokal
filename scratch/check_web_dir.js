const fs = require('fs');
console.log('web files:', fs.readdirSync('src/main/resources/web'));
if (fs.existsSync('src/main/resources/web/js')) {
    console.log('js files:', fs.readdirSync('src/main/resources/web/js'));
}
