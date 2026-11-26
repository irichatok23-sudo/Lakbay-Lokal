const http = require('http');

http.get('http://127.0.0.1:8080/api/municipalities', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        const munis = JSON.parse(data);
        console.log('Municipalities count:', munis.length);
        console.log('Municipalities:', munis);
    });
}).on('error', err => {
    console.error('Error fetching municipalities:', err.message);
});
