const fs = require('fs');
const path = require('path');

const targetDirs = [
    path.join(__dirname, '..', 'src', 'main', 'resources', 'web', 'assets'),
    path.join(__dirname, '..', 'bin', 'web', 'assets')
];

targetDirs.forEach(dir => {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
});

const packages = [
    {
        name: 'Biliran Chasing Waterfalls Eco-Trek',
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795147252375_4b4353c9.webp',
        dests: ['package_waterfalls.jpg', 'package_waterfalls.webp']
    },
    {
        name: 'Higatangan Shifting Sandbar & Heritage Cruise',
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795147278041_d49f7a6d.jpg',
        dests: ['package_higatangan.jpg']
    },
    {
        name: 'Maripipi Artisan Culture & Pottery Immersion',
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795147331250_0463b3f6.jpg',
        dests: ['package_artisan.jpg']
    },
    {
        name: 'Sambawan Island Marine Paradise Escape',
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795147370393_5f068a78.jpg',
        dests: ['package_sambawan.jpg']
    }
];

packages.forEach(pkg => {
    if (!fs.existsSync(pkg.src)) {
        console.error('Source not found:', pkg.src);
        return;
    }
    const stat = fs.statSync(pkg.src);
    console.log(`Setting photo for "${pkg.name}" (${stat.size} bytes)...`);
    pkg.dests.forEach(dest => {
        targetDirs.forEach(dir => {
            const destPath = path.join(dir, dest);
            fs.copyFileSync(pkg.src, destPath);
            console.log(`  -> ${destPath}`);
        });
    });
});

console.log('All 4 curated package images copied successfully!');
