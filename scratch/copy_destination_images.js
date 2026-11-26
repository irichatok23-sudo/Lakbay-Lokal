const fs = require('fs');
const path = require('path');

const uploads = [
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795145591158_c561a158.jpg',
        dest: 'maripipi_pottery.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795145874690_53ab3801.jpg',
        dest: 'reco_falls.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795145923882_6a6b963a.jpg',
        dest: 'sambawan_island.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795145950290_9206383b.jpg',
        dest: 'tinago_falls.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795145980422_919c5af7.jpg',
        dest: 'ulan_ulan_falls.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146009235_c059d986.jpg',
        dest: 'higatangan_sandbar.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146059849_d667e953.jpg',
        dest: 'iyusan_terraces.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146103540_f4c68b1d.jpg',
        dest: 'mainit_spring.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146009235_c059d986.jpg',
        dest: 'higatangan_sandbar.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146059849_d667e953.jpg',
        dest: 'iyusan_terraces.jpg'
    },
    {
        src: 'C:/Users/ASUS/.gemini/antigravity/brain/7986cd7b-4958-4ec4-b601-1c86e1dcd7aa/.user_uploaded/media_1795146103540_f4c68b1d.jpg',
        dest: 'mainit_spring.jpg'
    }
];

const targetDirs = [
    path.join(__dirname, '..', 'src', 'main', 'resources', 'web', 'assets'),
    path.join(__dirname, '..', 'bin', 'web', 'assets')
];

targetDirs.forEach(dir => {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
});

uploads.forEach(u => {
    if (!fs.existsSync(u.src)) {
        console.error('Source not found:', u.src);
        return;
    }
    const stat = fs.statSync(u.src);
    console.log(`Copying ${u.dest} (${stat.size} bytes)...`);
    targetDirs.forEach(dir => {
        const destPath = path.join(dir, u.dest);
        fs.copyFileSync(u.src, destPath);
        console.log(`  -> ${destPath}`);
    });
});

console.log('All 8 destination images copied successfully!');
