const fs = require('fs');

['scratch/manuscript_extracted.txt', 'scratch/manuscript_draft2.txt'].forEach(p => {
  if (!fs.existsSync(p)) return;
  const t = fs.readFileSync(p, 'utf8');
  console.log('--- Checking:', p, '---');
  ['literat', 'Samplimg', 'Barangay', 'Respondents', 'Sampling'].forEach(q => {
    let idx = 0;
    while ((idx = t.indexOf(q, idx)) !== -1) {
      console.log(`[${q}] at ${idx}: ...${t.substring(Math.max(0, idx - 40), Math.min(t.length, idx + 80))}...`);
      idx += q.length;
    }
  });
});
