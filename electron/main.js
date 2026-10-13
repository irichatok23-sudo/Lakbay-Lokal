const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const path = require('path');
const http = require('http');
const { spawn } = require('child_process');

let mainWindow = null;
let backendProcess = null;
const SERVER_PORT = 8080;
const SERVER_URL = `http://127.0.0.1:${SERVER_PORT}/`;

/**
 * Check if the Java backend HTTP server is responding
 */
function checkServerRunning(port = SERVER_PORT) {
    return new Promise((resolve) => {
        const req = http.get(`http://127.0.0.1:${port}/api/destinations`, (res) => {
            resolve(true);
        });
        req.on('error', () => {
            resolve(false);
        });
        req.setTimeout(1200, () => {
            req.abort();
            resolve(false);
        });
    });
}

/**
 * Spawns the Java backend process if it's not already running
 */
async function ensureBackendRunning() {
    const isRunning = await checkServerRunning();
    if (isRunning) {
        console.log('[Electron] Detected active Lakbay Lokal backend on port ' + SERVER_PORT);
        return;
    }

    console.log('[Electron] Starting Java backend process...');
    const projectRoot = path.resolve(__dirname, '..');
    const binDir = path.join(projectRoot, 'bin');

    // Build classpath
    let classpath = binDir;
    const userProfile = process.env.USERPROFILE || '';
    const mysqlJarGlob = path.join(userProfile, '.m2', 'repository', 'com', 'mysql', 'mysql-connector-j');
    
    // Check if java is available
    const javaCmd = process.env.JAVA_HOME 
        ? path.join(process.env.JAVA_HOME, 'bin', 'java.exe') 
        : 'java';

    try {
        backendProcess = spawn(javaCmd, ['-cp', classpath, 'com.lakbaylokal.Main'], {
            cwd: projectRoot,
            detached: false,
            stdio: 'pipe'
        });

        backendProcess.stdout.on('data', (data) => {
            console.log(`[Java Backend] ${data.toString().trim()}`);
        });

        backendProcess.stderr.on('data', (data) => {
            console.error(`[Java Backend Err] ${data.toString().trim()}`);
        });

        backendProcess.on('close', (code) => {
            console.log(`[Java Backend] Exited with code ${code}`);
            backendProcess = null;
        });
    } catch (err) {
        console.warn('[Electron] Could not spawn backend process directly:', err.message);
    }
}

/**
 * Wait for server to become responsive, up to a timeout
 */
async function waitForServer(timeoutMs = 7000) {
    const startTime = Date.now();
    while (Date.now() - startTime < timeoutMs) {
        if (await checkServerRunning()) {
            return true;
        }
        await new Promise((r) => setTimeout(r, 400));
    }
    return false;
}

function createWindow() {
    mainWindow = new BrowserWindow({
        title: 'Lakbay Lokal: Community Tourism Information System - Biliran',
        width: 1420,
        height: 900,
        minWidth: 1024,
        minHeight: 680,
        backgroundColor: '#032219',
        icon: path.join(__dirname, 'assets', 'icon.png'),
        show: false,
        webPreferences: {
            contextIsolation: true,
            nodeIntegration: false,
            sandbox: true,
            preload: path.join(__dirname, 'preload.js')
        }
    });

    // Remove default top menu for a modern desktop look
    mainWindow.setMenuBarVisibility(false);

    // Show when ready to prevent visual flickering
    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
    });

    // Handle external links safely
    mainWindow.webContents.setWindowOpenHandler(({ url }) => {
        if (url.startsWith('http:') || url.startsWith('https:')) {
            shell.openExternal(url);
        }
        return { action: 'deny' };
    });

    mainWindow.on('closed', () => {
        mainWindow = null;
    });
}

/**
 * Application Lifecycle
 */
app.whenReady().then(async () => {
    // 1. IPC Handlers
    ipcMain.handle('get-app-version', () => app.getVersion());
    ipcMain.handle('check-backend-health', async () => {
        return await checkServerRunning();
    });
    ipcMain.on('window-minimize', () => {
        if (mainWindow) mainWindow.minimize();
    });
    ipcMain.on('window-maximize', () => {
        if (mainWindow) {
            if (mainWindow.isMaximized()) mainWindow.unmaximize();
            else mainWindow.maximize();
        }
    });
    ipcMain.on('window-close', () => {
        if (mainWindow) mainWindow.close();
    });
    ipcMain.on('open-external', (event, url) => {
        if (url && (url.startsWith('http://') || url.startsWith('https://'))) {
            shell.openExternal(url);
        }
    });

    // 2. Start or detect backend
    await ensureBackendRunning();
    const serverReady = await waitForServer(4000);

    // 3. Create window
    createWindow();

    // 4. Load page
    if (serverReady) {
        console.log('[Electron] Loading from embedded server:', SERVER_URL);
        mainWindow.loadURL(SERVER_URL);
    } else {
        console.log('[Electron] Server not responding immediately. Loading local HTML bundle...');
        const localPath = path.resolve(__dirname, '..', 'src', 'main', 'resources', 'web', 'index.html');
        mainWindow.loadFile(localPath);
    }
});

app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
        createWindow();
    }
});

app.on('before-quit', () => {
    // Gracefully terminate child backend process
    if (backendProcess) {
        console.log('[Electron] Terminating background Java process...');
        try {
            backendProcess.kill();
        } catch (e) {
            console.error('[Electron] Error killing backend process:', e);
        }
        backendProcess = null;
    }
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
