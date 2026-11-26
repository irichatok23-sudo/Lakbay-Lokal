const http = require('http');

http.get('http://127.0.0.1:8080/api/destinations', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        const dests = JSON.parse(data);
        console.log('Sample destination:', dests[0]);
        console.log('Total destinations:', dests.length);
    });
}).on('error', err => {
    console.error('Error fetching destinations:', err.message);
});
