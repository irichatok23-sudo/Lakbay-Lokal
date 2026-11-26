const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const http = require('http');

const logFile = path.join(__dirname, 'test_four_enhancements.log');
fs.writeFileSync(logFile, 'Starting Four Enhancements Automated Test...\n', 'utf8');

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

function httpPost(urlPath, data, authToken) {
    return new Promise((resolve, reject) => {
        const bodyStr = JSON.stringify(data);
        const req = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: urlPath,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(bodyStr),
                'X-Auth-Token': authToken || ''
            }
        }, (res) => {
            let resBody = '';
            res.on('data', chunk => { resBody += chunk; });
            res.on('end', () => {
                let parsed = null;
                try { parsed = JSON.parse(resBody); } catch(e) { parsed = resBody; }
                resolve({ statusCode: res.statusCode, body: parsed });
            });
        });
        req.on('error', reject);
        req.write(bodyStr);
        req.end();
    });
}

app.whenReady().then(async () => {
    log('app.whenReady resolved! Creating window...');
    try {
        const win = new BrowserWindow({
            width: 1200,
            height: 800,
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

        // -------------------------------------------------------------
        // TEST 1: Live Notice Banner & Workspace Banner Visibility
        // -------------------------------------------------------------
        log('\n--- TEST 1: Live Notice Banner & Workspace Banner Visibility ---');

        // Log in as Tourism Office which has Overview, Destinations, Bookings, Barangay Records, etc.
        await win.webContents.executeJavaScript(`
            (async () => {
                document.getElementById('glass-login-username').value = 'tourismoffice';
                document.getElementById('glass-login-password').value = 'password123';
                await handleGlassLoginSubmit();
            })()
        `);
        await new Promise(r => setTimeout(r, 2000));

        // Check Overview tab: Notice bar MUST be visible
        const overviewBannerCheck = await win.webContents.executeJavaScript(`
            (() => {
                switchTab('overview');
                const nb = document.getElementById('live-notice-bar');
                const wb = document.getElementById('role-workspace-banner');
                const nbStyle = window.getComputedStyle(nb);
                const wbStyle = window.getComputedStyle(wb);
                return {
                    noticeBarDisplay: nbStyle.display,
                    noticeBarHidden: nb.classList.contains('banner-hidden'),
                    workspaceBannerDisplay: wbStyle.display,
                    workspaceBannerHidden: wb.classList.contains('banner-hidden')
                };
            })()
        `);
        log('Tourism Office Overview Tab Banner Check:', overviewBannerCheck);

        // Switch to secondary tabs (bookings, barangay-records, verifications, reports): Notice bar MUST be hidden
        const secondaryTabsCheck = await win.webContents.executeJavaScript(`
            (() => {
                const results = {};
                const tabs = ['bookings', 'barangay-records', 'verifications', 'reports'];
                tabs.forEach(t => {
                    switchTab(t);
                    const nb = document.getElementById('live-notice-bar');
                    const wb = document.getElementById('role-workspace-banner');
                    const nbStyle = window.getComputedStyle(nb);
                    const wbStyle = window.getComputedStyle(wb);
                    results[t] = {
                        noticeBarDisplay: nbStyle.display,
                        noticeBarHidden: nb.classList.contains('banner-hidden'),
                        noticeBarMargin: nbStyle.marginBottom,
                        workspaceBannerDisplay: wbStyle.display,
                        workspaceBannerHidden: wb.classList.contains('banner-hidden')
                    };
                });
                return results;
            })()
        `);
        log('Tourism Office Secondary Tabs Banner Check:', secondaryTabsCheck);

        // Test Tourist role: on destinations tab, banner MUST be visible; on bookings, MUST be hidden
        const touristBannerCheck = await win.webContents.executeJavaScript(`
            (() => {
                state.activeRole = 'TOURIST';
                switchTab('destinations');
                const nb = document.getElementById('live-notice-bar');
                const wb = document.getElementById('role-workspace-banner');
                const destNbDisplay = window.getComputedStyle(nb).display;
                const destNbHidden = nb.classList.contains('banner-hidden');
                const destWbDisplay = window.getComputedStyle(wb).display;

                switchTab('bookings');
                const bookNbDisplay = window.getComputedStyle(nb).display;
                const bookNbHidden = nb.classList.contains('banner-hidden');

                return {
                    destNoticeBarDisplay: destNbDisplay,
                    destNoticeBarHidden: destNbHidden,
                    destWorkspaceBannerDisplay: destWbDisplay,
                    bookNoticeBarDisplay: bookNbDisplay,
                    bookNoticeBarHidden: bookNbHidden
                };
            })()
        `);
        log('Tourist Tabs Banner Check:', touristBannerCheck);

        // -------------------------------------------------------------
        // TEST 2: Booking Table Actions Column with "View Details" Button
        // -------------------------------------------------------------
        log('\n--- TEST 2: Booking Table ACTIONS Column & View Details Button ---');

        const bookingTableCheck = await win.webContents.executeJavaScript(`
            (async () => {
                state.activeRole = 'TOURIST';
                // Mock test bookings: one pending, one completed
                state.bookings = [
                    {
                        bookingId: 101,
                        bookingReference: 'LL-2026-7020',
                        touristName: 'Maria Santos',
                        packageName: 'Sambawan Marine Sanctuary',
                        travelDateStart: '2026-11-20',
                        travelDateEnd: '2026-11-22',
                        numberOfPax: 2,
                        totalAmount: 4200.00,
                        status: 'PENDING'
                    },
                    {
                        bookingId: 102,
                        bookingReference: 'LL-2026-7021',
                        touristName: 'Juan Dela Cruz',
                        packageName: 'Tinago Falls Eco Trek',
                        travelDateStart: '2026-11-15',
                        travelDateEnd: '2026-11-16',
                        numberOfPax: 4,
                        totalAmount: 5800.00,
                        status: 'COMPLETED'
                    }
                ];

                switchTab('bookings');
                renderBookingsTable();

                const ths = Array.from(document.querySelectorAll('#bookings-table thead th')).map(th => th.textContent.trim());
                const rows = document.querySelectorAll('#bookings-table-body tr.clickable-booking-row');

                let pendingRowOk = false;
                let completedRowOk = false;
                let isWhitespaceNowrap = false;
                let btnTitle = '';
                let hasEyeIcon = false;

                if (rows.length >= 2) {
                    const row1Actions = rows[0].querySelector('td:last-child');
                    const viewBtn1 = row1Actions.querySelector('.btn-view-details');
                    const cancelBtn1 = row1Actions.querySelector('.btn-cancel-booking');

                    if (viewBtn1) {
                        btnTitle = viewBtn1.getAttribute('title') || '';
                        hasEyeIcon = viewBtn1.querySelector('i.fa-eye') !== null;
                        const style = window.getComputedStyle(viewBtn1);
                        isWhitespaceNowrap = (style.whiteSpace === 'nowrap');
                        // In pending row for Tourist, both View Details and Cancel are present
                        pendingRowOk = viewBtn1.textContent.includes('View Details') && cancelBtn1 !== null;
                    }

                    const row2Actions = rows[1].querySelector('td:last-child');
                    const viewBtn2 = row2Actions.querySelector('.btn-view-details');
                    // In completed row, View Details is present and NO stray dash '—'
                    completedRowOk = viewBtn2 !== null && viewBtn2.textContent.includes('View Details') && !row2Actions.textContent.includes('—');
                }

                return {
                    tableHeaders: ths,
                    hasActionsHeader: ths.includes('Actions'),
                    rowsCount: rows.length,
                    btnTitle: btnTitle,
                    hasEyeIcon: hasEyeIcon,
                    isWhitespaceNowrap: isWhitespaceNowrap,
                    pendingRowOk: pendingRowOk,
                    completedRowOk: completedRowOk
                };
            })()
        `);
        log('Booking Table Check:', bookingTableCheck);

        // -------------------------------------------------------------
        // TEST 3: Tourism Office Booking Restrictions (Frontend & Backend)
        // -------------------------------------------------------------
        log('\n--- TEST 3: Tourism Office Booking Restrictions ---');

        // Check Frontend UI when role is TOURISM_OFFICE
        const tourismOfficeUiCheck = await win.webContents.executeJavaScript(`
            (async () => {
                state.activeRole = 'TOURISM_OFFICE';
                renderBookingsTable();

                const rows = document.querySelectorAll('#bookings-table-body tr.clickable-booking-row');
                let foundConfirmOrCancelInTable = false;
                rows.forEach(r => {
                    const actionsCell = r.querySelector('td:last-child');
                    if (actionsCell) {
                        if (actionsCell.querySelector('.btn-cancel-booking') || actionsCell.textContent.includes('Confirm') || actionsCell.textContent.includes('Cancel')) {
                            foundConfirmOrCancelInTable = true;
                        }
                    }
                });

                // Test modal footer for first booking
                const pendingBooking = state.bookings[0];
                let modalHasConfirmOrCancel = false;
                if (pendingBooking) {
                    await openViewBookingModal(pendingBooking.bookingId);
                    const modalFooter = document.getElementById('view-booking-footer-actions');
                    if (modalFooter) {
                        const txt = modalFooter.textContent;
                        if (txt.includes('Confirm Booking') || txt.includes('Cancel Booking')) {
                            modalHasConfirmOrCancel = true;
                        }
                    }
                    closeModal('modal-view-booking');
                }

                return {
                    foundConfirmOrCancelInTable: foundConfirmOrCancelInTable,
                    modalHasConfirmOrCancel: modalHasConfirmOrCancel
                };
            })()
        `);
        log('Tourism Office Frontend UI Restriction Check:', tourismOfficeUiCheck);

        // Check Backend Role Enforcement: Login as Tourism Office and try to POST /api/bookings/status
        log('Testing Backend Role Enforcement: Tourism Office POST /api/bookings/status...');
        const toLoginRes = await httpPost('/api/auth/login', { username: 'tourismoffice', password: 'password123' });
        const toToken = toLoginRes.body && toLoginRes.body.token ? toLoginRes.body.token : '';

        let backendToBlockPassed = false;
        if (toToken) {
            const statusUpdateRes = await httpPost('/api/bookings/status', { bookingId: 1, status: 'CONFIRMED' }, toToken);
            log('Backend Tourism Office /api/bookings/status response:', statusUpdateRes);
            backendToBlockPassed = (statusUpdateRes.statusCode === 403) && (statusUpdateRes.body && statusUpdateRes.body.success === false);
        } else {
            log('Warning: could not authenticate as tourismoffice');
        }

        // Test Stakeholder permissions: Stakeholder modal preserves Confirm Booking & Cancel Booking
        const stakeholderUiCheck = await win.webContents.executeJavaScript(`
            (async () => {
                state.activeRole = 'STAKEHOLDER';
                const pendingBooking = state.bookings[0];
                let modalHasConfirmAndCancel = false;
                if (pendingBooking) {
                    await openViewBookingModal(pendingBooking.bookingId);
                    const modalFooter = document.getElementById('view-booking-footer-actions');
                    if (modalFooter) {
                        const txt = modalFooter.textContent;
                        if (txt.includes('Confirm Booking') && txt.includes('Cancel Booking')) {
                            modalHasConfirmAndCancel = true;
                        }
                    }
                    closeModal('modal-view-booking');
                }
                return { modalHasConfirmAndCancel: modalHasConfirmAndCancel };
            })()
        `);
        log('Stakeholder Modal Permission Check:', stakeholderUiCheck);

        // -------------------------------------------------------------
        // TEST 4: Destination Photo Upload, Previews & Persistence
        // -------------------------------------------------------------
        log('\n--- TEST 4: Add New Destination Modal Photo Upload & Previews ---');

        const destModalPhotoUiCheck = await win.webContents.executeJavaScript(`
            (() => {
                state.activeRole = 'TOURISM_OFFICE';
                openAddDestinationModal();

                const dropzone = document.getElementById('dest-upload-dropzone');
                const fileInput = document.getElementById('dest-photo-file-input');
                const previewsGrid = document.getElementById('dest-photo-previews-grid');
                const modalDialog = document.querySelector('#modal-add-destination .modal-dialog');
                const modalBody = document.querySelector('#modal-add-destination .modal-body');

                const dropzoneExists = dropzone !== null;
                const fileInputAccepts = fileInput ? fileInput.getAttribute('accept') : '';
                const hasHelperText = dropzone ? dropzone.textContent.includes('Max 5MB') : false;
                const hasUploadIcon = dropzone ? dropzone.querySelector('i.fa-cloud-arrow-up') !== null : false;

                const dialogStyle = window.getComputedStyle(modalDialog);
                const bodyStyle = window.getComputedStyle(modalBody);

                // Simulate adding a photo
                destinationPhotos = [{
                    dataUrl: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
                    name: 'test_destination.png',
                    isExisting: false
                }];
                renderDestinationPhotoPreviews();

                const previewCard = previewsGrid.querySelector('.dest-preview-card');
                const previewImg = previewCard ? previewCard.querySelector('img') : null;
                const removeBtn = previewCard ? previewCard.querySelector('.btn-remove-photo') : null;
                const previewBadge = previewCard ? previewCard.querySelector('.dest-preview-badge') : null;

                // Test removal
                removeDestinationPhoto(0);
                const previewCountAfterRemove = destinationPhotos.length;

                closeModal('modal-add-destination');

                return {
                    dropzoneExists: dropzoneExists,
                    fileInputAccepts: fileInputAccepts,
                    hasHelperText: hasHelperText,
                    hasUploadIcon: hasUploadIcon,
                    modalInternalScrolling: bodyStyle.overflowY === 'auto',
                    previewCardRendered: previewCard !== null,
                    previewImgSrcValid: previewImg && previewImg.src.startsWith('data:image/'),
                    hasRemoveBtn: removeBtn !== null,
                    badgeText: previewBadge ? previewBadge.textContent : '',
                    removalWorks: previewCountAfterRemove === 0
                };
            })()
        `);
        log('Destination Modal Photo UI Check:', destModalPhotoUiCheck);

        // Test Photo Upload API Endpoint: POST /api/destinations/upload
        log('Testing Backend API: POST /api/destinations/upload...');
        const adminLoginRes = await httpPost('/api/auth/login', { username: 'admin', password: 'password123' });
        const adminToken = adminLoginRes.body && adminLoginRes.body.token ? adminLoginRes.body.token : '';

        const sampleBase64 = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
        const uploadRes = await httpPost('/api/destinations/upload', {
            fileName: 'sample_island.png',
            data: sampleBase64
        }, adminToken);
        log('Upload API Response:', uploadRes);

        const uploadSuccess = (uploadRes.statusCode === 200) && uploadRes.body && uploadRes.body.success === true && uploadRes.body.url.startsWith('uploads/destinations/');

        // Verify Static Serving of Uploaded Photo
        let photoStaticServingPass = false;
        if (uploadSuccess) {
            const uploadedUrl = uploadRes.body.url;
            log(`Checking HTTP static serving of ${uploadedUrl}...`);
            const getPhotoRes = await new Promise((resolve) => {
                http.get(`http://127.0.0.1:8080/${uploadedUrl}`, res => {
                    resolve({ statusCode: res.statusCode, contentType: res.headers['content-type'] });
                }).on('error', err => resolve({ error: err.message }));
            });
            log('Photo Static Serving Check:', getPhotoRes);
            photoStaticServingPass = (getPhotoRes.statusCode === 200 && getPhotoRes.contentType === 'image/png');
        }

        // Test getDestinationImage helper
        const imageHelperCheck = await win.webContents.executeJavaScript(`
            (() => {
                const testDest1 = { imageUrl: 'uploads/destinations/dest_123.jpg' };
                const testDest2 = { imageUrl: 'sambawan_island.jpg' };
                return {
                    uploadedPath: getDestinationImage(testDest1),
                    assetPath: getDestinationImage(testDest2)
                };
            })()
        `);
        log('getDestinationImage helper check:', imageHelperCheck);

        // Test Edit Destination modal pre-populates existing photo
        const editDestModalCheck = await win.webContents.executeJavaScript(`
            (() => {
                state.activeRole = 'TOURISM_OFFICE';
                // Mock or find a destination with an image
                const dest = state.destinations[0] || { destinationId: 999, name: 'Sambawan Beach Resort' };
                dest.imageUrl = 'uploads/destinations/sample_island.png';
                if (!state.destinations.find(x => x.destinationId === dest.destinationId)) {
                    state.destinations.push(dest);
                }

                openEditDestinationModal(dest.destinationId);
                const previewsGrid = document.getElementById('dest-photo-previews-grid');
                const hasPreview = previewsGrid && previewsGrid.querySelectorAll('.dest-preview-card').length > 0;
                const hiddenVal = document.getElementById('dest-form-image-url')?.value;
                const previewImg = previewsGrid ? previewsGrid.querySelector('.dest-preview-card img') : null;
                const hasRemoveBtn = previewsGrid ? previewsGrid.querySelector('.btn-remove-photo') !== null : false;
                closeModal('modal-add-destination');

                return {
                    destName: dest.name,
                    hasPreview: hasPreview,
                    hiddenImageUrl: hiddenVal,
                    previewImgSrc: previewImg ? previewImg.src : '',
                    hasRemoveBtn: hasRemoveBtn
                };
            })()
        `);
        log('Edit Destination Modal Photo Check:', editDestModalCheck);

        // -------------------------------------------------------------
        // SUMMARY ASSERTIONS
        // -------------------------------------------------------------
        const assertions = [
            // Requirement 1
            { name: 'Req 1: Live notice bar visible on Overview Dashboard', pass: overviewBannerCheck.noticeBarDisplay === 'flex' && !overviewBannerCheck.noticeBarHidden },
            { name: 'Req 1: Live notice bar visible on Tourist Destinations page', pass: touristBannerCheck.destNoticeBarDisplay === 'flex' && !touristBannerCheck.destNoticeBarHidden },
            { name: 'Req 1: Live notice bar hidden on Bookings page with zero leftover margin', pass: secondaryTabsCheck.bookings.noticeBarDisplay === 'none' && secondaryTabsCheck.bookings.noticeBarHidden && secondaryTabsCheck.bookings.noticeBarMargin === '0px' },
            { name: 'Req 1: Live notice bar hidden on Barangay Records page', pass: secondaryTabsCheck['barangay-records'].noticeBarDisplay === 'none' },
            { name: 'Req 1: Live notice bar hidden on Verifications page', pass: secondaryTabsCheck['verifications'].noticeBarDisplay === 'none' },
            { name: 'Req 1: Live notice bar hidden on Reports page', pass: secondaryTabsCheck['reports'].noticeBarDisplay === 'none' },

            // Requirement 2
            { name: 'Req 2: Actions column header present in Bookings table', pass: bookingTableCheck.hasActionsHeader },
            { name: 'Req 2: View Details button present in row with explicit text label', pass: bookingTableCheck.pendingRowOk },
            { name: 'Req 2: View Details button includes eye icon', pass: bookingTableCheck.hasEyeIcon },
            { name: 'Req 2: View Details button has descriptive hover tooltip', pass: bookingTableCheck.btnTitle === 'View full booking/record details' },
            { name: 'Req 2: View Details button has whitespace-nowrap', pass: bookingTableCheck.isWhitespaceNowrap },
            { name: 'Req 2: Completed booking row has View Details and NO stray dash (—)', pass: bookingTableCheck.completedRowOk },

            // Requirement 3
            { name: 'Req 3: Tourism Office bookings table has NO Confirm or Cancel button', pass: !tourismOfficeUiCheck.foundConfirmOrCancelInTable },
            { name: 'Req 3: Tourism Office modal footer has NO Confirm or Cancel button (read-only)', pass: !tourismOfficeUiCheck.modalHasConfirmOrCancel },
            { name: 'Req 3: Backend enforces 403 when Tourism Office attempts /api/bookings/status', pass: backendToBlockPassed },
            { name: 'Req 3: Stakeholder modal preserves Confirm and Cancel buttons for pending bookings', pass: stakeholderUiCheck.modalHasConfirmAndCancel },

            // Requirement 4
            { name: 'Req 4: Add Destination modal includes photo upload dropzone with helper text', pass: destModalPhotoUiCheck.dropzoneExists && destModalPhotoUiCheck.hasHelperText && destModalPhotoUiCheck.hasUploadIcon },
            { name: 'Req 4: Add Destination modal file input accepts JPG, PNG, WEBP', pass: destModalPhotoUiCheck.fileInputAccepts.includes('png') && destModalPhotoUiCheck.fileInputAccepts.includes('jpeg') && destModalPhotoUiCheck.fileInputAccepts.includes('webp') },
            { name: 'Req 4: Add Destination modal has internal vertical scrolling (max-h-90vh)', pass: destModalPhotoUiCheck.modalInternalScrolling },
            { name: 'Req 4: Live image thumbnail preview card rendered with Cover Photo badge', pass: destModalPhotoUiCheck.previewCardRendered && destModalPhotoUiCheck.badgeText === 'Cover Photo' },
            { name: 'Req 4: Live preview removal (X) button removes photo cleanly', pass: destModalPhotoUiCheck.hasRemoveBtn && destModalPhotoUiCheck.removalWorks },
            { name: 'Req 4: Backend POST /api/destinations/upload saves photo and returns URL', pass: uploadSuccess },
            { name: 'Req 4: Uploaded destination photo is served correctly with 200 OK and MIME type', pass: photoStaticServingPass },
            { name: 'Req 4: getDestinationImage supports uploads/ paths correctly', pass: imageHelperCheck.uploadedPath === 'uploads/destinations/dest_123.jpg' },
            { name: 'Req 4: Edit Destination modal pre-populates existing photo in preview grid', pass: editDestModalCheck.hasPreview && editDestModalCheck.hasRemoveBtn }
        ];

        log('\n==================================================');
        log('SUMMARY ASSERTIONS:');
        assertions.forEach(a => log(`[${a.pass ? 'PASS' : 'FAIL'}] ${a.name}`));
        log('==================================================');

        const allPass = assertions.every(a => a.pass);
        if (allPass) {
            log('>>> ALL 21 VERIFICATION TESTS PASSED SUCCESSFULLY! <<<');
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
