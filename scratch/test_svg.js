// Generate inline SVG for the Lakbay Lokal Logo
const logoSvg = `
<svg width="120" height="85" viewBox="0 0 140 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="login-logo-svg">
    <!-- Mountain Peaks -->
    <!-- Left Mountain Peak -->
    <path d="M25 72 L55 28 L85 72 Z" fill="#2d6a4f" />
    <path d="M55 28 L43 45 L50 43 L55 47 L62 42 L67 45 Z" fill="#ffffff" />
    
    <!-- Right Mountain Peak -->
    <path d="M60 72 L88 24 L115 72 Z" fill="#1b4332" />
    <path d="M88 24 L76 43 L83 40 L88 44 L95 39 L100 43 Z" fill="#ffffff" />

    <!-- Center Top Location Pin -->
    <g transform="translate(48, 2)">
        <path d="M22 0C9.85 0 0 9.85 0 22C0 35.5 18 51.5 22 55C26 51.5 44 35.5 44 22C44 9.85 34.15 0 22 0Z" fill="#1e633d" />
        <circle cx="22" cy="20" r="8" fill="#ffffff" />
    </g>

    <!-- Smooth Rolling Terraces / Curved Sweeps -->
    <!-- Upper green sweep -->
    <path d="M12 72 Q45 58 75 66 Q105 74 128 60 L128 72 L12 72 Z" fill="#40916c" />
    <!-- Middle green sweep -->
    <path d="M10 74 Q50 63 80 71 Q110 79 130 65 C125 74 110 82 80 80 C50 78 25 84 10 74 Z" fill="#2d6a4f" />
    <!-- Lower vibrant green sweep -->
    <path d="M15 82 Q55 70 85 77 Q115 84 132 72 C125 83 105 91 80 89 C55 87 30 93 15 82 Z" fill="#1e633d" />
</svg>
`;

// Bottom Left Palm & Hills Illustration
const palmsHillsSvg = `
<svg width="150" height="95" viewBox="0 0 160 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="login-corner-illustration">
    <!-- Soft rolling hills in background -->
    <path d="M0 100 L0 75 Q40 50 85 75 Q125 95 160 85 L160 100 Z" fill="#b7e4c7" opacity="0.6" />
    <path d="M0 100 L0 82 Q35 62 70 80 Q105 95 140 88 L140 100 Z" fill="#74c69d" opacity="0.75" />
    <path d="M0 100 L0 88 Q30 74 60 88 Q90 100 120 95 L120 100 Z" fill="#52b788" opacity="0.9" />

    <!-- Palm Tree 1 (Tall) -->
    <g transform="translate(15, 30)">
        <!-- Trunk -->
        <path d="M16 65 Q18 40 12 18 Q11 12 10 5" stroke="#40916c" stroke-width="3.5" stroke-linecap="round" fill="none" />
        <!-- Palm Fronds -->
        <path d="M10 5 Q4 -4 -5 3" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M10 5 Q12 -6 20 -3" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M10 5 Q22 2 26 12" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M10 5 Q1 -1 -7 12" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M10 5 Q10 -8 8 -12" stroke="#1b4332" stroke-width="2.5" stroke-linecap="round" fill="none" />
    </g>

    <!-- Palm Tree 2 (Smaller) -->
    <g transform="translate(32, 45) scale(0.75)">
        <!-- Trunk -->
        <path d="M14 60 Q18 35 15 15 Q14 10 12 4" stroke="#40916c" stroke-width="3.5" stroke-linecap="round" fill="none" />
        <!-- Palm Fronds -->
        <path d="M12 4 Q6 -5 -3 2" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M12 4 Q15 -7 22 -4" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M12 4 Q24 2 27 11" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
        <path d="M12 4 Q3 -2 -5 10" stroke="#2d6a4f" stroke-width="2.5" stroke-linecap="round" fill="none" />
    </g>
</svg>
`;

console.log("SVGs generated successfully");
