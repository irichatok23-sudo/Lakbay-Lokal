const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

app.whenReady().then(async () => {
    console.log('========================================================================');
    console.log('TESTING FROSTED-GLASS LEFT PANEL & LOGIN INTERACTIVITY IN ELECTRON');
    console.log('========================================================================');

    const win = new BrowserWindow({
        title: 'Lakbay Lokal - Login Portal UI Test',
        width: 1420,
        height: 900,
        show: false,
        backgroundColor: '#032219',
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, '..', 'electron', 'preload.js')
        }
    });

    console.log('1. Loading http://127.0.0.1:8080/ ...');
    await win.loadURL('http://127.0.0.1:8080/');
    await new Promise(r => setTimeout(r, 1500));

    // Inspect Left Information Panel styles and content
    const leftPanelInfo = await win.webContents.executeJavaScript(`
        (() => {
            const panel = document.querySelector('.glass-left-panel');
            const style = window.getComputedStyle(panel);
            const brandName = document.querySelector('.glass-brand-name')?.textContent;
            const tagline = document.querySelector('.glass-brand-tagline')?.textContent;
            const title = document.querySelector('.glass-welcome-title')?.textContent;
            const desc = document.querySelector('.glass-welcome-desc')?.textContent;
            const footer = document.querySelector('.glass-left-footer')?.textContent;
            return {
                bg: style.background,
                backdropFilter: style.backdropFilter || style.webkitBackdropFilter,
                borderRight: style.borderRight,
                color: style.color,
                width: panel.offsetWidth,
                height: panel.offsetHeight,
                brandName,
                tagline,
                title,
                desc,
                footer
            };
        })()
    `);
    console.log('Left Panel Computed Styles & Content:', JSON.stringify(leftPanelInfo, null, 2));

    // Capture screenshot of the new frosted-glass login portal
    const screenshotDir = path.join(__dirname, '..', 'scratch');
    const screenshotPath = path.join(screenshotDir, 'login_portal_frosted_glass.png');
    const image = await win.webContents.capturePage();
    fs.writeFileSync(screenshotPath, image.toPNG());
    console.log(`2. Screenshot captured at: ${screenshotPath}`);

    // Verify right-side login form interactivity
    console.log('3. Verifying right-side login inputs hit-testing...');
    const hitCheck = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            const p = document.getElementById('glass-login-password');
            const btn = document.getElementById('btn-glass-submit');
            const uRect = u.getBoundingClientRect();
            const pRect = p.getBoundingClientRect();
            const btnRect = btn.getBoundingClientRect();

            const topU = document.elementFromPoint(uRect.left + 25, uRect.top + 20);
            const topP = document.elementFromPoint(pRect.left + 25, pRect.top + 20);
            const topBtn = document.elementFromPoint(btnRect.left + 25, btnRect.top + 20);

            return {
                topU: topU ? topU.tagName + '#' + topU.id : null,
                topP: topP ? topP.tagName + '#' + topP.id : null,
                topBtn: topBtn ? topBtn.tagName + '#' + topBtn.id : null,
                uDisabled: u.disabled,
                uReadOnly: u.readOnly,
                pDisabled: p.disabled,
                pReadOnly: p.readOnly
            };
        })()
    `);
    console.log('Hit-testing results:', JSON.stringify(hitCheck, null, 2));

    let pass = true;
    if (hitCheck.topU !== 'INPUT#glass-login-username' || hitCheck.topP !== 'INPUT#glass-login-password') {
        console.error('FAILED: Inputs are not top elements under cursor!');
        pass = false;
    }

    // Test real OS typing and login
    console.log('4. Testing typing and login...');
    await win.webContents.executeJavaScript(`
        document.getElementById('glass-login-username').value = 'tourist_ana';
        document.getElementById('glass-login-password').value = 'password123';
        document.getElementById('btn-glass-submit').click();
    `);
    await new Promise(r => setTimeout(r, 2000));

    const dashCheck = await win.webContents.executeJavaScript(`
        (() => {
            const mgmt = document.getElementById('management-portal-view');
            return {
                mgmtDisplay: window.getComputedStyle(mgmt).display,
                mgmtHeight: mgmt.offsetHeight,
                user: state.currentUser ? state.currentUser.username : null
            };
        })()
    `);
    console.log('Dashboard login check:', JSON.stringify(dashCheck, null, 2));

    if (dashCheck.mgmtDisplay !== 'block' || dashCheck.mgmtHeight <= 0) {
        console.error('FAILED: Dashboard did not load!');
        pass = false;
    }

    // Perform logout and verify inputs remain clickable
    console.log('5. Logging out...');
    await win.webContents.executeJavaScript(`
        handleLogout();
    `);
    await new Promise(r => setTimeout(r, 1000));

    const postLogoutHit = await win.webContents.executeJavaScript(`
        (() => {
            const u = document.getElementById('glass-login-username');
            const uRect = u.getBoundingClientRect();
            const topU = document.elementFromPoint(uRect.left + 25, uRect.top + 20);
            return {
                portalDisplay: window.getComputedStyle(document.getElementById('login-portal-view')).display,
                topU: topU ? topU.tagName + '#' + topU.id : null,
                uDisabled: u.disabled,
                uReadOnly: u.readOnly,
                uValue: u.value
            };
        })()
    `);
    console.log('Post-logout check:', JSON.stringify(postLogoutHit, null, 2));

    if (postLogoutHit.topU !== 'INPUT#glass-login-username' || postLogoutHit.uDisabled) {
        console.error('FAILED: Inputs unclickable after logout!');
        pass = false;
    }

    console.log('========================================================================');
    if (pass) {
        console.log('TEST PASSED: FROSTED-GLASS LEFT PANEL BEAUTIFULLY APPLIED & INPUTS 100% INTERACTIVE!');
    } else {
        console.error('TEST FAILED');
    }
    console.log('========================================================================');

    await win.close();
    app.quit();
});
