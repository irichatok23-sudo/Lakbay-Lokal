const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const path = require('path');
const fs = require('fs');
const net = require('net');
const http = require('http');
const { spawn, execSync } = require('child_process');

let mainWindow = null;
let backendProcess = null;
const SERVER_PORT = 8080;
const SERVER_URL = `http://127.0.0.1:${SERVER_PORT}/`;

const LOG_FILE = path.join(__dirname, '..', 'lakbay_lokal_electron.log');

function logInfo(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a) : a)).join(' ');
    const line = `[${new Date().toISOString()}] [INFO] ${msg}`;
    console.log(line);
    try { fs.appendFileSync(LOG_FILE, line + '\n', 'utf8'); } catch (_) {}
}

function logError(...args) {
    const msg = args.map(a => (typeof a === 'object' ? (a.stack || JSON.stringify(a)) : a)).join(' ');
    const line = `[${new Date().toISOString()}] [ERROR] ${msg}`;
    console.error(line);
    try { fs.appendFileSync(LOG_FILE, line + '\n', 'utf8'); } catch (_) {}
}

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
 * Check if a local TCP port (e.g. MySQL 3306) is listening
 */
function checkPortListening(port, host = '127.0.0.1') {
    return new Promise((resolve) => {
        const socket = new net.Socket();
        socket.setTimeout(1000);
        socket.once('connect', () => {
            socket.destroy();
            resolve(true);
        });
        socket.once('timeout', () => {
            socket.destroy();
            resolve(false);
        });
        socket.once('error', () => {
            socket.destroy();
            resolve(false);
        });
        socket.connect(port, host);
    });
}

/**
 * Ensure local MySQL server is running if WAMP/XAMPP is installed
 */
async function ensureMySQLRunning() {
    if (await checkPortListening(3306)) {
        return true;
    }
    const candidates = [
        'C:\\wamp64\\bin\\mysql\\mysql8.4.7\\bin\\mysqld.exe',
        'C:\\xampp\\mysql\\bin\\mysqld.exe'
    ];
    for (const mysqld of candidates) {
        if (fs.existsSync(mysqld)) {
            logInfo('[Electron] Attempting to start local MySQL server:', mysqld);
            try {
                const child = spawn(mysqld, [], {
                    cwd: path.dirname(mysqld),
                    detached: true,
                    stdio: 'ignore',
                    windowsHide: true
                });
                child.on('error', (err) => {
                    logInfo('[Electron] MySQL child process notice:', err.message);
                });
                child.unref();
                for (let i = 0; i < 10; i++) {
                    await new Promise((r) => setTimeout(r, 400));
                    if (await checkPortListening(3306)) return true;
                }
            } catch (e) {
                logInfo('[Electron] Could not auto-start MySQL:', e.message);
            }
        }
    }
    return await checkPortListening(3306);
}

/**
 * Clean and normalize file path, stripping surrounding quotes and whitespace
 */
function sanitizePath(rawPath) {
    if (!rawPath || typeof rawPath !== 'string') return '';
    const unquoted = rawPath.replace(/[";']/g, '').trim();
    if (!unquoted) return '';
    return path.normalize(unquoted);
}

/**
 * Locate java.exe across:
 * 1. JAVA_HOME environment variable
 * 2. System PATH (via where.exe or PATH directories)
 * 3. Standard JDK installation directories
 * 4. Verified Eclipse Adoptium installation fallback
 * 5. Global 'java' command
 */
function resolveJavaExecutable() {
    const isWindows = process.platform === 'win32';
    const javaExeName = isWindows ? 'java.exe' : 'java';

    // 1. Check JAVA_HOME
    if (process.env.JAVA_HOME) {
        const cleanHome = sanitizePath(process.env.JAVA_HOME);
        const candidate = path.join(cleanHome, 'bin', javaExeName);
        if (fs.existsSync(candidate)) {
            logInfo('[Java Detect] Resolved from JAVA_HOME:', candidate);
            return candidate;
        }
    }

    // 2. Check system PATH via where.exe (Windows) or which (Unix)
    try {
        const cmd = isWindows ? 'where.exe java' : 'which java';
        const out = execSync(cmd, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
        const firstLine = out.split(/\r?\n/)[0];
        const cleanPath = sanitizePath(firstLine);
        if (cleanPath && fs.existsSync(cleanPath)) {
            logInfo('[Java Detect] Resolved from system PATH (' + cmd + '):', cleanPath);
            return cleanPath;
        }
    } catch (_) {
        // where.exe failed or not in PATH, proceed to directory scan
    }

    // Also check directories directly listed in process.env.PATH
    if (process.env.PATH) {
        const pathDirs = process.env.PATH.split(path.delimiter);
        for (const dir of pathDirs) {
            const cleanDir = sanitizePath(dir);
            if (cleanDir && fs.existsSync(cleanDir)) {
                const candidate = path.join(cleanDir, javaExeName);
                if (fs.existsSync(candidate)) {
                    logInfo('[Java Detect] Resolved from PATH directory:', candidate);
                    return candidate;
                }
            }
        }
    }

    // 3. Search common JDK installation base directories
    const localAppData = sanitizePath(process.env.LOCALAPPDATA) || '';
    const programFiles = sanitizePath(process.env.ProgramFiles) || 'C:\\Program Files';
    const programFilesX86 = sanitizePath(process.env['ProgramFiles(x86)']) || 'C:\\Program Files (x86)';

    const searchBases = [
        path.join(localAppData, 'Programs', 'Eclipse Adoptium'),
        path.join(programFiles, 'Eclipse Adoptium'),
        path.join(programFiles, 'Java'),
        path.join(programFiles, 'Microsoft'),
        path.join(programFiles, 'Amazon Corretto'),
        path.join(programFiles, 'Zulu'),
        path.join(programFiles, 'BellSoft'),
        path.join(programFilesX86, 'Java'),
        path.join(localAppData, 'Programs')
    ];

    for (const base of searchBases) {
        if (base && fs.existsSync(base)) {
            try {
                const entries = fs.readdirSync(base, { withFileTypes: true });
                const jdkDirs = entries
                    .filter((e) => e.isDirectory())
                    .map((e) => e.name)
                    .sort()
                    .reverse();

                for (const d of jdkDirs) {
                    const candidate = path.join(base, d, 'bin', javaExeName);
                    if (fs.existsSync(candidate)) {
                        logInfo('[Java Detect] Resolved from search directory:', candidate);
                        return candidate;
                    }
                }
            } catch (_) {}
        }
    }

    // 4. Verified Java installation fallback (Eclipse Adoptium OpenJDK 25)
    const verifiedPaths = [
        path.join(localAppData, 'Programs', 'Eclipse Adoptium', 'jdk-25.0.4.101-hotspot', 'bin', 'java.exe'),
        'C:\\Users\\ASUS\\AppData\\Local\\Programs\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot\\bin\\java.exe'
    ];
    for (const vp of verifiedPaths) {
        if (fs.existsSync(vp)) {
            logInfo('[Java Detect] Resolved from verified fallback:', vp);
            return vp;
        }
    }

    // 5. Fallback to generic binary name
    logInfo('[Java Detect] No explicit java binary found on disk; falling back to "java"');
    return 'java';
}

/**
 * Attaches stdout, stderr, and close listeners to the child backend process
 */
function attachBackendHandlers(proc) {
    if (!proc) return;

    proc.stdout.on('data', (data) => {
        logInfo(`[Java Backend] ${data.toString().trim()}`);
    });

    proc.stderr.on('data', (data) => {
        logError(`[Java Backend Err] ${data.toString().trim()}`);
    });

    proc.on('close', (code) => {
        logInfo(`[Java Backend] Exited with code ${code}`);
        backendProcess = null;
    });
}

function showJavaStartupError(attemptedCmd, cwd, err) {
    const errorDetails = err ? (err.message || err.code || String(err)) : 'Unknown error';
    logError('[Electron] Showing Java startup error dialog:', errorDetails);
    const msg = `Could not start the Java backend process.\n\n` +
        `Attempted Executable: ${attemptedCmd}\n` +
        `Working Directory: ${cwd}\n` +
        `Error: ${errorDetails}\n\n` +
        `Please verify that Java (OpenJDK/Adoptium) is installed and that MySQL is running.`;
    dialog.showErrorBox('Lakbay Lokal - Java Startup Notice', msg);
}

/**
 * Spawns the Java backend process if it's not already running
 */
async function ensureBackendRunning() {
    await ensureMySQLRunning();

    const isRunning = await checkServerRunning();
    if (isRunning) {
        logInfo('[Electron] Detected active Lakbay Lokal backend on port ' + SERVER_PORT);
        return;
    }

    logInfo('[Electron] Starting Java backend process in headless mode...');

    // 1. Resolve project root
    const candidateRoots = [
        path.resolve(__dirname, '..'),
        process.resourcesPath ? path.join(process.resourcesPath, 'app') : null,
        process.cwd()
    ].filter(Boolean);

    let projectRoot = candidateRoots[0];
    for (const root of candidateRoots) {
        if (fs.existsSync(path.join(root, 'bin', 'com', 'lakbaylokal', 'Main.class')) ||
            fs.existsSync(path.join(root, 'target', 'classes', 'com', 'lakbaylokal', 'Main.class'))) {
            projectRoot = root;
            break;
        }
    }

    if (!fs.existsSync(projectRoot)) {
        projectRoot = process.cwd();
    }

    const binDir = path.join(projectRoot, 'bin');
    const libWildcard = path.join(projectRoot, 'lib', '*');
    const targetClasses = path.join(projectRoot, 'target', 'classes');

    // Build classpath entries
    const cpEntries = [binDir, libWildcard];
    if (fs.existsSync(targetClasses)) {
        cpEntries.push(targetClasses);
    }
    const classpath = cpEntries.join(path.delimiter);

    // 2. Resolve Java command path
    const javaCmd = resolveJavaExecutable();
    logInfo('[Electron] Spawning Java backend with executable:', javaCmd);
    logInfo('[Electron] Project root (CWD):', projectRoot);
    logInfo('[Electron] Classpath:', classpath);

    const spawnArgs = ['-cp', classpath, 'com.lakbaylokal.Main', '--headless'];

    // 3. Spawn child process with error handling
    try {
        backendProcess = spawn(javaCmd, spawnArgs, {
            cwd: projectRoot,
            detached: false,
            windowsHide: true,
            stdio: 'pipe'
        });

        backendProcess.on('error', (err) => {
            logError('[Electron] Backend process spawn error:', err);
            backendProcess = null;

            // If full path failed with ENOENT, try fallback to system PATH "java"
            if (err.code === 'ENOENT' && javaCmd !== 'java') {
                logInfo('[Electron] Attempting fallback spawn with "java" from PATH...');
                try {
                    backendProcess = spawn('java', spawnArgs, {
                        cwd: projectRoot,
                        detached: false,
                        windowsHide: true,
                        stdio: 'pipe'
                    });
                    backendProcess.on('error', (fbErr) => {
                        logError('[Electron] Fallback spawn with "java" failed:', fbErr);
                        backendProcess = null;
                        showJavaStartupError(javaCmd, projectRoot, fbErr);
                    });
                    attachBackendHandlers(backendProcess);
                    return;
                } catch (fallbackErr) {
                    logError('[Electron] Fallback spawn threw exception:', fallbackErr);
                }
            }

            showJavaStartupError(javaCmd, projectRoot, err);
        });

        attachBackendHandlers(backendProcess);
    } catch (err) {
        logError('[Electron] Synchronous spawn exception:', err);
        showJavaStartupError(javaCmd, projectRoot, err);
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
        logInfo('[Electron] Loading from embedded server:', SERVER_URL);
        mainWindow.loadURL(SERVER_URL);
    } else {
        logInfo('[Electron] Server not responding immediately. Loading local HTML bundle...');
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
        logInfo('[Electron] Terminating background Java process...');
        try {
            backendProcess.kill();
        } catch (e) {
            logError('[Electron] Error killing backend process:', e);
        }
        backendProcess = null;
    }
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
