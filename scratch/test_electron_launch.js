const { app } = require('electron');
const fs = require('fs');
fs.writeFileSync('scratch/launch.txt', 'VERSION: ' + process.versions.electron + '\n');
app.quit();
