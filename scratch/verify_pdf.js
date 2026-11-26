const fs = require('fs');
const pdf = fs.readFileSync('scratch/sample_report.pdf');
console.log('PDF Length:', pdf.length);
console.log('Starts with %PDF:', pdf.toString('ascii', 0, 8));
console.log('Ends with %%EOF:', pdf.toString('ascii', pdf.length - 10).includes('%%EOF'));
console.log('Has xref:', pdf.includes(Buffer.from('xref')));
console.log('Has trailer:', pdf.includes(Buffer.from('trailer')));
