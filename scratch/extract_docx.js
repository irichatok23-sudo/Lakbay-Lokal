const AdmZip = require('adm-zip');
const fs = require('fs');

const docPath = 'C:\\Users\\ASUS\\OneDrive\\Documents\\ieee-format (1).docx';
try {
  const zip = new AdmZip(docPath);
  const text = zip.readAsText('word/document.xml');
  // strip xml tags
  const plain = text.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ');
  fs.writeFileSync('scratch/manuscript_extracted.txt', plain);
  console.log('Extracted', plain.length, 'chars. Preview:', plain.substring(0, 500));
} catch (e) {
  console.error('Extraction error:', e.message);
}
