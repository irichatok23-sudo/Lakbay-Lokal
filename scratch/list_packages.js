const http = require('http');

http.get('http://127.0.0.1:8080/api/packages', (res) => {
    let d = '';
    res.on('data', c => d += c);
    res.on('end', () => {
        try {
            console.log(JSON.stringify(JSON.parse(d), null, 2));
        } catch (e) {
            console.log(d);
        }
    });
}).on('error', console.error);
