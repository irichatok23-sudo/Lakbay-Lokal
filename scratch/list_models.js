const fs = require('fs');
const modelDir = 'src/main/java/com/lakbaylokal/model';
const files = fs.readdirSync(modelDir);
console.log('Model classes:', files.join(', '));
