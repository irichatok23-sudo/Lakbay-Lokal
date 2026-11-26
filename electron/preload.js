const { contextBridge, ipcRenderer } = require('electron');

// Expose safe, isolated desktop APIs to the renderer
contextBridge.exposeInMainWorld('desktopAPI', {
    isElectron: true,
    platform: process.platform,
    getVersion: () => ipcRenderer.invoke('get-app-version'),
    minimizeWindow: () => ipcRenderer.send('window-minimize'),
    maximizeWindow: () => ipcRenderer.send('window-maximize'),
    closeWindow: () => ipcRenderer.send('window-close'),
    openExternal: (url) => ipcRenderer.send('open-external', url),
    checkBackendHealth: () => ipcRenderer.invoke('check-backend-health'),
    restoreFocus: () => ipcRenderer.send('restore-focus')
});
