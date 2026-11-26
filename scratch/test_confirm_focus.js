const { app, BrowserWindow, dialog } = require('electron');
const path = require('path');
const fs = require('fs');

const logFile = path.join(__dirname, 'confirm_focus.log');
fs.writeFileSync(logFile, 'Starting Confirm Focus Test...\n', 'utf8');

function log(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a, null, 2) : String(a))).join(' ');
    console.log(msg);
    fs.appendFileSync(logFile, msg + '\n', 'utf8');
}

app.whenReady().then(async () => {
    const win = new BrowserWindow({
        width: 1420,
        height: 900,
        show: true,
        webPreferences: {
            contextIsolation: true,
            nodeIntegration: false,
            sandbox: true,
            preload: path.join(__dirname, '..', 'electron', 'preload.js')
        }
    });

    win.webContents.on('console-message', (event, level, message) => {
        log(`[Console] ${message}`);
    });

    try {
        await win.loadURL('http://127.0.0.1:8080/');
        await new Promise(r => setTimeout(r, 2000));

        // Log in as tourist_ana
        log('Logging in as tourist_ana...');
        await win.webContents.executeJavaScript(`
            (async () => {
                document.getElementById('glass-login-username').value = 'tourist_ana';
                document.getElementById('glass-login-password').value = 'password123';
                await handleGlassLoginSubmit();
            })()
        `);
        await new Promise(r => setTimeout(r, 3000));

        // Now test handleLogout with a mock or real dialog
        log('Simulating logout...');
        await win.webContents.executeJavaScript(`
            (() => {
                logout();
            })()
        `);
        await new Promise(r => setTimeout(r, 1000));

        // Inspect window and input state
        const state = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                const p = document.getElementById('glass-login-password');
                const active = document.activeElement;
                return {
                    windowHasFocus: document.hasFocus(),
                    activeElementTag: active?.tagName,
                    activeElementId: active?.id,
                    uDisabled: u?.disabled,
                    uReadOnly: u?.readOnly,
                    pDisabled: p?.disabled,
                    pReadOnly: p?.readOnly
                };
            })()
        `);
        log('State after logout:', state);

        // Try focusing username and typing
        log('Focusing username input...');
        const focusResult = await win.webContents.executeJavaScript(`
            (() => {
                const u = document.getElementById('glass-login-username');
                u.focus();
                return {
                    activeElementId: document.activeElement?.id,
                    windowHasFocus: document.hasFocus()
                };
            })()
        `);
        log('Focus result:', focusResult);

    } catch (e) {
        log('Error:', e.stack || e);
    } finally {
        app.quit();
    }
});
