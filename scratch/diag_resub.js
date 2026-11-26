const http = require('http');

function req(path, token) {
  return new Promise((resolve) => {
    http.get({ hostname: '127.0.0.1', port: 8080, path, headers: { 'X-Auth-Token': token } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => {
        try { resolve(JSON.parse(b)); } catch (e) { resolve(b); }
      });
    });
  });
}

async function run() {
  const loginRes = await new Promise((resolve) => {
    const r = http.request({ hostname: '127.0.0.1', port: 8080, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json' } }, res => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => resolve(JSON.parse(b)));
    });
    r.write(JSON.stringify({ username: 'guide_resub_43297', password: 'password123' }));
    r.end();
  });
  const stkToken = loginRes.token;
  const verif = await req('/api/stakeholders/my-verification', stkToken);
  console.log('--- STAKEHOLDER MY-VERIFICATION ---');
  console.log(JSON.stringify(verif, null, 2));
}
run();
