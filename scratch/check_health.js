const http = require('http');

setTimeout(() => {
    const req = http.get('http://127.0.0.1:8080/api/destinations', (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
            console.log('Backend response status:', res.statusCode);
            try {
                const parsed = JSON.parse(data);
                console.log('Destinations returned:', parsed.length);
            } catch (e) {
                console.log('Response body:', data);
            }
        });
    });
    req.on('error', (err) => {
        console.error('Connection error:', err.message);
    });
}, 2000);
