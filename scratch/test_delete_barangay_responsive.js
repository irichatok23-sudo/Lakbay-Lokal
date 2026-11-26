const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

const logFile = path.join(__dirname, 'test_delete_barangay_responsive.log');
fs.writeFileSync(logFile, 'Starting Delete Barangay Responsive Test...\n', 'utf8');

function log(...args) {
    const msg = args.map(a => (typeof a === 'object' ? JSON.stringify(a, null, 2) : String(a))).join(' ');
    console.log(msg);
    fs.appendFileSync(logFile, msg + '\n', 'utf8');
}

process.on('uncaughtException', err => {
    log('Uncaught Exception:', err.stack || err.message);
});

process.on('unhandledRejection', err => {
    log('Unhandled Rejection:', err && (err.stack || err.message));
});

log('Electron app module loaded, waiting for ready...');

app.whenReady().then(async () => {
    log('app.whenReady resolved! Creating window...');
    try {
        const win = new BrowserWindow({
            width: 1024,
            height: 768,
            show: true,
            webPreferences: {
                contextIsolation: false,
                nodeIntegration: true
            }
        });

        win.webContents.on('console-message', (event, level, message) => {
            log(`[Renderer Console] ${message}`);
        });

        log('Loading http://127.0.0.1:8080/ ...');
        await win.loadURL('http://127.0.0.1:8080/');
        await new Promise(r => setTimeout(r, 2000));

        // 2. Login as admin
        log('Logging in as admin...');
        await win.webContents.executeJavaScript(`
            (async () => {
                document.getElementById('glass-login-username').value = 'admin';
                document.getElementById('glass-login-password').value = 'password123';
                await handleGlassLoginSubmit();
            })()
        `);
        await new Promise(r => setTimeout(r, 2500));

        // 3. Switch to barangay-records tab
        log('Switching to pane-barangay-records tab...');
        await win.webContents.executeJavaScript(`
            (async () => {
                switchTab('barangay-records');
                await loadBarangayRecordsTable();
            })()
        `);
        await new Promise(r => setTimeout(r, 2000));

        // 4. Find the test barangay ID dynamically
        const targetBarangay = await win.webContents.executeJavaScript(`
            (() => {
                const rec = (state.barangayRecords || []).find(b => (b.barangay_name || b.name || '').includes('Brgy Test Automated'))
                    || (state.barangayRecords || [])[0];
                return {
                    id: rec ? (rec.barangay_id || rec.barangayId) : null,
                    name: rec ? (rec.barangay_name || rec.name) : null,
                    totalCount: (state.barangayRecords || []).length
                };
            })()
        `);
        log('Target Barangay Found:', targetBarangay);

        const targetId = targetBarangay.id;

        // 5. Open Edit Barangay modal for target barangay
        log('Opening edit modal for barangay ID ' + targetId + '...');
        await win.webContents.executeJavaScript(`
            (() => {
                openEditBarangayModal(${targetId});
            })()
        `);
        await new Promise(r => setTimeout(r, 1000));

        // 6. Test Desktop (>= 640px) Modal & Button Layout
        log('Testing Desktop (1024px) layout...');
        const desktopLayout = await win.webContents.executeJavaScript(`
            (() => {
                const modal = document.getElementById('modal-edit-barangay');
                const dialog = modal.querySelector('.modal-dialog');
                const actions = modal.querySelector('.modal-actions-responsive');
                const btnDelete = document.getElementById('btn-delete-barangay');
                const btnSave = document.getElementById('btn-save-barangay');
                const modalBody = modal.querySelector('.modal-body');

                const dialogStyle = window.getComputedStyle(dialog);
                const actionsStyle = window.getComputedStyle(actions);
                const deleteStyle = window.getComputedStyle(btnDelete);
                const saveStyle = window.getComputedStyle(btnSave);
                const bodyStyle = window.getComputedStyle(modalBody);

                const deleteRect = btnDelete.getBoundingClientRect();
                const saveRect = btnSave.getBoundingClientRect();

                return {
                    modalOpen: modal.classList.contains('open') && modal.style.display === 'flex',
                    dialogWidth: dialogStyle.width,
                    dialogMaxWidth: dialogStyle.maxWidth,
                    bodyOverflowY: bodyStyle.overflowY,
                    flexDirection: actionsStyle.flexDirection,
                    deleteBtnColor: deleteStyle.color,
                    deleteBtnBg: deleteStyle.backgroundColor,
                    deleteBtnBorderColor: deleteStyle.borderColor,
                    saveBtnBg: saveStyle.backgroundColor,
                    saveBtnColor: saveStyle.color,
                    isSideBySide: Math.abs(deleteRect.top - saveRect.top) < 10 && deleteRect.left < saveRect.left,
                    deleteRect: { top: deleteRect.top, left: deleteRect.left, width: deleteRect.width, height: deleteRect.height },
                    saveRect: { top: saveRect.top, left: saveRect.left, width: saveRect.width, height: saveRect.height }
                };
            })()
        `);
        log('Desktop Layout Check:', desktopLayout);

        // 7. Test Mobile (< 640px) Modal & Button Layout
        log('Resizing window to Mobile (375 x 667)...');
        win.setSize(375, 667);
        await new Promise(r => setTimeout(r, 1500));

        const mobileLayout = await win.webContents.executeJavaScript(`
            (() => {
                const modal = document.getElementById('modal-edit-barangay');
                const dialog = modal.querySelector('.modal-dialog');
                const actions = modal.querySelector('.modal-actions-responsive');
                const btnDelete = document.getElementById('btn-delete-barangay');
                const btnSave = document.getElementById('btn-save-barangay');
                const modalBody = modal.querySelector('.modal-body');

                const dialogStyle = window.getComputedStyle(dialog);
                const actionsStyle = window.getComputedStyle(actions);
                const deleteStyle = window.getComputedStyle(btnDelete);
                const saveStyle = window.getComputedStyle(btnSave);
                const bodyStyle = window.getComputedStyle(modalBody);

                const deleteRect = btnDelete.getBoundingClientRect();
                const saveRect = btnSave.getBoundingClientRect();

                return {
                    dialogWidth: dialogStyle.width,
                    dialogMaxWidth: dialogStyle.maxWidth,
                    bodyOverflowY: bodyStyle.overflowY,
                    flexDirection: actionsStyle.flexDirection,
                    // In column-reverse: Save is on top, Delete is underneath
                    isSaveOnTop: saveRect.bottom <= deleteRect.top + 5,
                    isBothFullWidth: Math.abs(deleteRect.width - actions.getBoundingClientRect().width) < 5
                                  && Math.abs(saveRect.width - actions.getBoundingClientRect().width) < 5,
                    deleteRect: { top: deleteRect.top, left: deleteRect.left, width: deleteRect.width, height: deleteRect.height },
                    saveRect: { top: saveRect.top, left: saveRect.left, width: saveRect.width, height: saveRect.height }
                };
            })()
        `);
        log('Mobile Layout Check:', mobileLayout);

        // 8. Click "Delete Barangay" button to trigger confirmation modal
        log('Triggering confirmDeleteBarangay()...');
        await win.webContents.executeJavaScript(`
            (() => {
                document.getElementById('btn-delete-barangay').click();
            })()
        `);
        await new Promise(r => setTimeout(r, 1000));

        // 9. Inspect Confirmation Modal
        const confirmModalCheck = await win.webContents.executeJavaScript(`
            (() => {
                const confirmModal = document.getElementById('modal-confirm-delete-barangay');
                const title = confirmModal.querySelector('.modal-title');
                const nameEl = document.getElementById('confirm-delete-barangay-name');
                const desc = confirmModal.querySelector('p');
                const dialog = confirmModal.querySelector('.modal-dialog');
                const dStyle = window.getComputedStyle(dialog);

                return {
                    isOpen: confirmModal.classList.contains('open') && confirmModal.style.display === 'flex',
                    dialogWidth: dStyle.width,
                    dialogMaxWidth: dStyle.maxWidth,
                    titleText: title ? title.innerText.trim() : '',
                    barangayName: nameEl ? nameEl.innerText.trim() : '',
                    promptText: desc ? desc.innerText.trim() : ''
                };
            })()
        `);
        log('Confirmation Modal Check:', confirmModalCheck);

        // 10. Click "Yes, Delete" and test dynamic deletion
        log('Executing deletion...');
        await win.webContents.executeJavaScript(`
            (() => {
                document.getElementById('btn-confirm-delete-barangay-btn').click();
            })()
        `);
        await new Promise(r => setTimeout(r, 2000));

        // 11. Verify row dynamic removal and modal closure
        const postDeleteCheck = await win.webContents.executeJavaScript(`
            (() => {
                const editModal = document.getElementById('modal-edit-barangay');
                const confirmModal = document.getElementById('modal-confirm-delete-barangay');
                const deletedRow = document.getElementById('row-barangay-${targetId}');
                const remainingRows = Array.from(document.querySelectorAll('#barangay-full-body tr'));

                const toast = document.querySelector('.toast, #toast-container');
                const toastText = toast ? toast.innerText : '';

                return {
                    editModalClosed: !editModal.classList.contains('open') || editModal.style.display === 'none',
                    confirmModalClosed: !confirmModal.classList.contains('open') || confirmModal.style.display === 'none',
                    rowDynamicallyRemoved: deletedRow === null,
                    remainingRowsCount: remainingRows.length,
                    toastText: toastText
                };
            })()
        `);
        log('Post-Deletion Check:', postDeleteCheck);

        // Check assertions
        const assertions = [
            { name: 'Edit modal opened', pass: desktopLayout.modalOpen },
            { name: 'Desktop dialog responsive max-width (28rem = 448px)', pass: desktopLayout.dialogMaxWidth === '448px' || desktopLayout.dialogMaxWidth === '28rem' },
            { name: 'Desktop buttons side-by-side in a row', pass: desktopLayout.flexDirection === 'row' && desktopLayout.isSideBySide },
            { name: 'Mobile buttons flex-direction column-reverse', pass: mobileLayout.flexDirection === 'column-reverse' },
            { name: 'Mobile Save button is on top', pass: mobileLayout.isSaveOnTop },
            { name: 'Mobile buttons are full width', pass: mobileLayout.isBothFullWidth },
            { name: 'Confirmation modal opened', pass: confirmModalCheck.isOpen },
            { name: 'Confirmation exact prompt text', pass: confirmModalCheck.promptText === 'Are you sure you want to delete this barangay record? This action cannot be undone.' },
            { name: 'Modals closed after deletion', pass: postDeleteCheck.editModalClosed && postDeleteCheck.confirmModalClosed },
            { name: 'Row dynamically removed from DOM table without reload', pass: postDeleteCheck.rowDynamicallyRemoved }
        ];

        log('==================================================');
        log('SUMMARY ASSERTIONS:');
        assertions.forEach(a => log(`[${a.pass ? 'PASS' : 'FAIL'}] ${a.name}`));
        log('==================================================');

        const allPass = assertions.every(a => a.pass);
        if (allPass) {
            log('>>> ALL VERIFICATION TESTS PASSED SUCCESSFULLY! <<<');
        } else {
            log('>>> SOME TESTS FAILED! <<<');
        }

    } catch (err) {
        log('Test execution error:', err.stack || err.message);
    } finally {
        setTimeout(() => {
            app.quit();
        }, 1000);
    }
});
