const fs = require('fs');

let html = fs.readFileSync('bin/web/index.html', 'utf8').replace(/\r\n/g, '\n');

// 1. Overview Announcements Feed Card
const overviewTarget = '<!-- Quick Overview Columns -->';
const overviewReplacement = `<!-- Official Tourism Announcements & Advisories Feed -->
                        <div class="card mb-4" id="overview-announcements-card">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--primary-600);"></i> Official Tourism Announcements &amp; Advisories</h3>
                                <button class="btn btn-link btn-sm" onclick="switchTab('announcements')">View All Announcements <i class="fa-solid fa-arrow-right"></i></button>
                            </div>
                            <div class="card-body" id="overview-announcements-feed" style="padding: 1rem 1.25rem;">
                                <!-- Injected dynamically by renderOverviewAnnouncementsFeed() -->
                            </div>
                        </div>

                        <!-- Quick Overview Columns -->`;

if (!html.includes(overviewTarget)) throw new Error('Target for overview not found');
html = html.replace(overviewTarget, overviewReplacement);

// 2. pane-announcements enhancement
const annoTarget = `<section class="tab-pane" id="pane-announcements">
                        <div class="page-header">
                            <div>
                                <h2 class="page-title">Tourism Information Management</h2>
                                <p class="page-desc">Official announcements, upcoming festivals, weather advisories, and eco-tourism guidelines.</p>
                            </div>
                            <div class="header-actions">
                                <button class="btn btn-primary" id="btn-add-announcement" onclick="openAddAnnouncementModal()">
                                    <i class="fa-solid fa-plus"></i> Post Update
                                </button>
                            </div>
                        </div>

                        <div class="announcements-grid" id="announcements-cards-grid">
                            <!-- Injected dynamically -->
                        </div>
                    </section>`;

const annoReplacement = `<section class="tab-pane" id="pane-announcements">
                        <div class="page-header">
                            <div>
                                <h2 class="page-title">Tourism Information Management</h2>
                                <p class="page-desc">Official announcements, upcoming festivals, weather advisories, and eco-tourism guidelines.</p>
                            </div>
                            <div class="header-actions">
                                <button class="btn btn-primary" id="btn-add-announcement" onclick="openAddAnnouncementModal()">
                                    <i class="fa-solid fa-plus"></i> Post Update
                                </button>
                            </div>
                        </div>

                        <!-- Status Filter Tabs (For Tourism Office & System Admin) -->
                        <div class="sub-nav-tabs" id="anno-status-tabs" style="display: none; margin-bottom: 1.25rem;">
                            <button class="sub-tab active" id="anno-tab-all" onclick="switchAnnouncementStatusTab('ALL')">
                                <i class="fa-solid fa-layer-group"></i> All Records <span class="badge badge-neutral" id="anno-count-all" style="font-size:0.7rem; margin-left:4px;">0</span>
                            </button>
                            <button class="sub-tab" id="anno-tab-published" onclick="switchAnnouncementStatusTab('PUBLISHED')">
                                <i class="fa-solid fa-circle-check" style="color:#10b981;"></i> Published <span class="badge badge-success" id="anno-count-published" style="font-size:0.7rem; margin-left:4px;">0</span>
                            </button>
                            <button class="sub-tab" id="anno-tab-draft" onclick="switchAnnouncementStatusTab('DRAFT')">
                                <i class="fa-solid fa-file-pen" style="color:#f59e0b;"></i> Drafts <span class="badge badge-warning" id="anno-count-draft" style="font-size:0.7rem; margin-left:4px;">0</span>
                            </button>
                            <button class="sub-tab" id="anno-tab-archived" onclick="switchAnnouncementStatusTab('ARCHIVED')">
                                <i class="fa-solid fa-box-archive" style="color:#64748b;"></i> Archived <span class="badge badge-neutral" id="anno-count-archived" style="font-size:0.7rem; margin-left:4px;">0</span>
                            </button>
                        </div>

                        <!-- Filter & Search Toolbar -->
                        <div class="card p-3 mb-3" id="anno-filter-toolbar" style="background: var(--slate-50); border: 1px solid var(--slate-200);">
                            <div style="display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: center;">
                                <div style="flex: 2; min-width: 220px;">
                                    <input type="text" id="anno-filter-search" class="form-input" placeholder="Search announcements..." oninput="handleAnnouncementFilterChange()">
                                </div>
                                <div style="flex: 1; min-width: 170px;">
                                    <select id="anno-filter-category" class="form-select" onchange="handleAnnouncementFilterChange()">
                                        <option value="">All Categories</option>
                                        <option value="ANNOUNCEMENT">Announcement</option>
                                        <option value="EVENT">Event</option>
                                        <option value="FESTIVAL">Festival</option>
                                        <option value="TRAVEL_GUIDELINE">Travel Guideline</option>
                                        <option value="DESTINATION_CLOSURE">Destination Closure</option>
                                        <option value="ADVISORY">Advisory</option>
                                    </select>
                                </div>
                                <div style="flex: 1; min-width: 170px;">
                                    <select id="anno-filter-scope" class="form-select" onchange="handleAnnouncementFilterChange()">
                                        <option value="">All Scopes</option>
                                        <option value="PROVINCE">Province-wide Only</option>
                                    </select>
                                </div>
                                <div>
                                    <button class="btn btn-outline btn-sm" onclick="resetAnnouncementFilters()">
                                        <i class="fa-solid fa-rotate-left"></i> Reset
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- Tourism Office & Admin Table View -->
                        <div class="card mb-4" id="anno-table-container" style="display: none;">
                            <div class="table-responsive">
                                <table class="data-table" id="anno-manager-table">
                                    <thead>
                                        <tr>
                                            <th style="width: 70px;">Poster</th>
                                            <th>Title</th>
                                            <th>Category</th>
                                            <th>Scope</th>
                                            <th>Publish Date</th>
                                            <th>Expiry Date</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody id="anno-manager-table-body">
                                        <!-- Injected dynamically -->
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Public / Tourist Grid View -->
                        <div class="announcements-grid" id="announcements-cards-grid">
                            <!-- Injected dynamically -->
                        </div>
                    </section>`;

if (!html.includes(annoTarget)) throw new Error('Target for pane-announcements not found');
html = html.replace(annoTarget, annoReplacement);

// 3. Barangay announcements feed
const brgyTarget = `                                    <tbody id="barangay-full-body">
                                        <!-- Populated dynamically -->
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </section>`;

const brgyReplacement = `                                    <tbody id="barangay-full-body">
                                        <!-- Populated dynamically -->
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Barangay Announcements & Advisories Feed -->
                        <div class="card mb-4" id="brgy-announcements-card" style="margin-top: 1.5rem;">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--primary-600);"></i> Barangay &amp; Provincial Tourism Announcements</h3>
                                <button class="btn btn-link btn-sm" onclick="switchTab('announcements')">View Full Announcements <i class="fa-solid fa-arrow-right"></i></button>
                            </div>
                            <div class="card-body" id="brgy-announcements-feed" style="padding: 1rem 1.25rem;">
                                <!-- Injected dynamically -->
                            </div>
                        </div>
                    </section>`;

if (!html.includes(brgyTarget)) throw new Error('Target for pane-barangay-records not found');
html = html.replace(brgyTarget, brgyReplacement);

// 4. LGU announcements feed
const lguTarget = `                                    <div class="card p-3" style="background: #fffbeb; border-color: #fde68a;">
                                        <h4 style="color: #92400e;"><i class="fa-solid fa-triangle-exclamation"></i> Higatangan Sandbar</h4>
                                        <p style="font-size: 0.85rem; margin-top: 0.25rem;">Capacity: 180 pax/day | Current: <strong>78%</strong> (Moderate)</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>`;

const lguReplacement = `                                    <div class="card p-3" style="background: #fffbeb; border-color: #fde68a;">
                                        <h4 style="color: #92400e;"><i class="fa-solid fa-triangle-exclamation"></i> Higatangan Sandbar</h4>
                                        <p style="font-size: 0.85rem; margin-top: 0.25rem;">Capacity: 180 pax/day | Current: <strong>78%</strong> (Moderate)</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Municipal Announcements & Advisories Feed -->
                        <div class="card mb-4" id="lgu-announcements-card" style="margin-top: 1.5rem;">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--primary-600);"></i> Municipal &amp; Provincial Tourism Advisories</h3>
                                <button class="btn btn-link btn-sm" onclick="switchTab('announcements')">View Full Announcements <i class="fa-solid fa-arrow-right"></i></button>
                            </div>
                            <div class="card-body" id="lgu-announcements-feed" style="padding: 1rem 1.25rem;">
                                <!-- Injected dynamically -->
                            </div>
                        </div>
                    </section>`;

if (!html.includes(lguTarget)) throw new Error('Target for pane-lgu-monitoring not found');
html = html.replace(lguTarget, lguReplacement);

// 5. Admin announcements monitoring widget
const adminTarget = `                        <!-- User Account Management -->
                        <div class="card mb-4">
                            <div class="card-header">
                                <h3 class="card-title"><i class="fa-solid fa-users-gear"></i> System User Registry</h3>`;

const adminReplacement = `                        <!-- Announcement Records Monitoring Widget for Admin -->
                        <div class="card mb-4" id="admin-announcement-stats-card">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h3 class="card-title"><i class="fa-solid fa-bullhorn" style="color: var(--accent-600);"></i> Official Tourism Announcements Monitoring</h3>
                                <button class="btn btn-outline btn-sm" onclick="switchTab('announcements')">
                                    <i class="fa-solid fa-eye"></i> View Announcement Registry
                                </button>
                            </div>
                            <div class="card-body">
                                <div class="grid-4-col" id="admin-announcement-stats-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem;">
                                    <div class="card p-3" style="background: var(--slate-50); border-color: var(--slate-200); text-align: center;">
                                        <div style="font-size: 0.8rem; color: var(--slate-600); font-weight: 600;">Total Announcements</div>
                                        <div id="admin-stat-anno-total" style="font-size: 1.5rem; font-weight: 700; color: var(--slate-800); margin-top: 0.25rem;">0</div>
                                    </div>
                                    <div class="card p-3" style="background: #f0fdf4; border-color: #bbf7d0; text-align: center;">
                                        <div style="font-size: 0.8rem; color: #166534; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Published (Active)</div>
                                        <div id="admin-stat-anno-published" style="font-size: 1.5rem; font-weight: 700; color: #166534; margin-top: 0.25rem;">0</div>
                                    </div>
                                    <div class="card p-3" style="background: #fffbeb; border-color: #fde68a; text-align: center;">
                                        <div style="font-size: 0.8rem; color: #92400e; font-weight: 600;"><i class="fa-solid fa-file-pen"></i> Drafts</div>
                                        <div id="admin-stat-anno-drafts" style="font-size: 1.5rem; font-weight: 700; color: #92400e; margin-top: 0.25rem;">0</div>
                                    </div>
                                    <div class="card p-3" style="background: #f1f5f9; border-color: #cbd5e1; text-align: center;">
                                        <div style="font-size: 0.8rem; color: #475569; font-weight: 600;"><i class="fa-solid fa-box-archive"></i> Archived</div>
                                        <div id="admin-stat-anno-archived" style="font-size: 1.5rem; font-weight: 700; color: #475569; margin-top: 0.25rem;">0</div>
                                    </div>
                                </div>
                                <div style="margin-top: 0.75rem; font-size: 0.8rem; color: var(--slate-500); display: flex; align-items: center; gap: 0.4rem;">
                                    <i class="fa-solid fa-circle-info"></i> Note: System Administrators have audit and read-only monitoring access to all announcement records. Authoring and publishing authority is restricted to the Tourism Office.
                                </div>
                            </div>
                        </div>

                        <!-- User Account Management -->
                        <div class="card mb-4">
                            <div class="card-header">
                                <h3 class="card-title"><i class="fa-solid fa-users-gear"></i> System User Registry</h3>`;

if (!html.includes(adminTarget)) throw new Error('Target for pane-admin not found');
html = html.replace(adminTarget, adminReplacement);

// 6. Modals: Replace modal-add-announcement and add modal-view-announcement
const modalTarget = `    <!-- ======================================================================= -->
    <!-- ADD ANNOUNCEMENT MODAL                                                  -->
    <!-- ======================================================================= -->
    <div class="modal-backdrop" id="modal-add-announcement">
        <div class="modal-dialog">
            <div class="modal-header">
                <h3 class="modal-title" id="modal-add-announcement-title"><i class="fa-solid fa-bullhorn"></i> Post Tourism Information &amp; Advisory</h3>
                <button class="btn-close" onclick="closeModal('modal-add-announcement')">&times;</button>
            </div>
            <div class="modal-body">
                <form id="form-add-announcement" onsubmit="handleAddAnnouncementSubmit(event)">
                    <input type="hidden" id="anno-form-id">
                    <div class="form-group">
                        <label class="form-label">Announcement Title</label>
                        <input type="text" id="anno-form-title" class="form-input" required placeholder="e.g. Sambawan Island Summer Regatta 2026">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Advisory Category</label>
                        <select id="anno-form-type" class="form-select">
                            <option value="ANNOUNCEMENT">General Notice / Announcement</option>
                            <option value="FESTIVAL">Festival &amp; Cultural Event</option>
                            <option value="EVENT">Tourism Event</option>
                            <option value="TRAVEL_GUIDELINE">Weather / Eco-Tourism &amp; Travel Guideline</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Advisory Content / Message</label>
                        <textarea id="anno-form-content" class="form-textarea" rows="4" required placeholder="Details for tourists, operators, and officials..."></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Publish Update</button>
                </form>
            </div>
        </div>
    </div>`;

const modalReplacement = `    <!-- ======================================================================= -->
    <!-- ADD / EDIT ANNOUNCEMENT MODAL                                           -->
    <!-- ======================================================================= -->
    <div class="modal-backdrop" id="modal-add-announcement">
        <div class="modal-dialog" style="max-width: 680px;">
            <div class="modal-header">
                <h3 class="modal-title" id="modal-add-announcement-title"><i class="fa-solid fa-bullhorn"></i> Post Tourism Information &amp; Advisory</h3>
                <button class="btn-close" onclick="closeModal('modal-add-announcement')">&times;</button>
            </div>
            <div class="modal-body">
                <form id="form-add-announcement" onsubmit="handleAnnouncementFormSubmit(event)">
                    <input type="hidden" id="anno-form-id">
                    <input type="hidden" id="anno-form-status-action" value="DRAFT">

                    <div class="form-group">
                        <label class="form-label">Announcement Title <span class="text-danger">*</span></label>
                        <input type="text" id="anno-form-title" class="form-input" required maxlength="160" placeholder="e.g. Sambawan Island Summer Regatta 2026">
                    </div>

                    <div class="form-group grid-2-col">
                        <div>
                            <label class="form-label">Category <span class="text-danger">*</span></label>
                            <select id="anno-form-type" class="form-select" required>
                                <option value="ANNOUNCEMENT">General Announcement</option>
                                <option value="EVENT">Tourism Event</option>
                                <option value="FESTIVAL">Festival &amp; Cultural Celebration</option>
                                <option value="TRAVEL_GUIDELINE">Travel &amp; Eco-Tourism Guideline</option>
                                <option value="DESTINATION_CLOSURE">Destination Closure / Notice</option>
                                <option value="ADVISORY">Public Advisory &amp; Weather Warning</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label">Target Scope / Barangay</label>
                            <select id="anno-form-target-barangay" class="form-select">
                                <option value="">All Biliran (Province-wide)</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group grid-2-col">
                        <div>
                            <label class="form-label">Publication Date</label>
                            <input type="date" id="anno-form-publish-date" class="form-input">
                            <small class="text-muted" style="font-size: 0.75rem;">Defaults to today if left empty on publish.</small>
                        </div>
                        <div>
                            <label class="form-label">Expiration Date (Optional)</label>
                            <input type="date" id="anno-form-expiry-date" class="form-input">
                            <small class="text-muted" style="font-size: 0.75rem;">Hides from public feed after this date.</small>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Announcement Poster / Image</label>
                        <div style="display: flex; gap: 0.75rem; align-items: flex-start;">
                            <div style="flex: 1;">
                                <input type="file" id="anno-form-image-file" class="form-input" accept="image/png, image/jpeg, image/webp" onchange="handleAnnouncementImageSelect(event)">
                                <input type="hidden" id="anno-form-image-url">
                                <small class="text-muted" style="font-size: 0.75rem;">Accepted: JPG, PNG, WEBP (Max 5MB). Image is optional.</small>
                            </div>
                            <div id="anno-image-preview-container" style="width: 100px; height: 68px; border: 1px dashed var(--slate-300); border-radius: 6px; overflow: hidden; display: flex; align-items: center; justify-content: center; background: var(--slate-50); flex-shrink: 0;">
                                <img id="anno-image-preview" src="" alt="Preview" style="max-width: 100%; max-height: 100%; object-fit: cover; display: none;">
                                <span id="anno-image-placeholder" style="font-size: 0.75rem; color: var(--slate-400); text-align: center;"><i class="fa-solid fa-image"></i><br>No Poster</span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Announcement Details / Content <span class="text-danger">*</span></label>
                        <textarea id="anno-form-content" class="form-textarea" rows="5" required placeholder="Write clear, comprehensive details for tourists, local operators, and government officers..."></textarea>
                    </div>

                    <div id="anno-modal-preview-box" class="card p-3 mb-3" style="display: none; background: var(--slate-50); border: 1px solid var(--slate-200);">
                        <h4 style="font-size: 0.85rem; color: var(--slate-600); margin-bottom: 0.5rem;"><i class="fa-solid fa-eye"></i> Live Preview:</h4>
                        <div id="anno-modal-preview-content"></div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem; margin-top: 1rem;">
                        <button type="button" class="btn btn-outline" onclick="toggleAnnouncementFormPreview()">
                            <i class="fa-solid fa-eye"></i> Preview
                        </button>
                        <div style="display: flex; gap: 0.5rem;">
                            <button type="button" class="btn btn-secondary" onclick="closeModal('modal-add-announcement')">Cancel</button>
                            <button type="submit" class="btn btn-warning" onclick="setAnnouncementFormAction('DRAFT')">
                                <i class="fa-solid fa-floppy-disk"></i> Save as Draft
                            </button>
                            <button type="submit" class="btn btn-primary" onclick="setAnnouncementFormAction('PUBLISHED')">
                                <i class="fa-solid fa-paper-plane"></i> Publish Now
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ======================================================================= -->
    <!-- VIEW ANNOUNCEMENT DETAILS MODAL                                         -->
    <!-- ======================================================================= -->
    <div class="modal-backdrop" id="modal-view-announcement">
        <div class="modal-dialog" style="max-width: 650px;">
            <div class="modal-header">
                <h3 class="modal-title" id="view-anno-title"><i class="fa-solid fa-bullhorn"></i> Announcement Details</h3>
                <button class="btn-close" onclick="closeModal('modal-view-announcement')">&times;</button>
            </div>
            <div class="modal-body">
                <div id="view-anno-poster-container" style="width: 100%; max-height: 260px; overflow: hidden; border-radius: 8px; margin-bottom: 1rem; display: none;">
                    <img id="view-anno-poster" src="" alt="Announcement Poster" style="width: 100%; height: 100%; object-fit: cover;">
                </div>
                <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; margin-bottom: 0.75rem;">
                    <span id="view-anno-category-badge" class="badge"></span>
                    <span id="view-anno-status-badge" class="badge"></span>
                    <span id="view-anno-scope-badge" class="badge badge-neutral"></span>
                </div>
                <h2 id="view-anno-heading" style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.75rem; color: var(--slate-900);"></h2>
                <div style="font-size: 0.8rem; color: var(--slate-500); display: flex; gap: 1rem; flex-wrap: wrap; margin-bottom: 1rem; border-bottom: 1px solid var(--slate-200); padding-bottom: 0.5rem;">
                    <span id="view-anno-author"><i class="fa-solid fa-user-tie"></i> Tourism Office</span>
                    <span id="view-anno-publish-date"><i class="fa-solid fa-calendar"></i> Published: </span>
                    <span id="view-anno-expiry-date"><i class="fa-solid fa-calendar-xmark"></i> Expiry: None</span>
                </div>
                <div id="view-anno-content" style="font-size: 0.92rem; line-height: 1.6; color: var(--slate-800); white-space: pre-wrap; margin-bottom: 1.5rem;"></div>
                <div style="text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('modal-view-announcement')">Close</button>
                </div>
            </div>
        </div>
    </div>`;

if (!html.includes(modalTarget)) throw new Error('Target for modal-add-announcement not found');
html = html.replace(modalTarget, modalReplacement);

// Convert back to CRLF
const crlfHtml = html.replace(/\n/g, '\r\n');
fs.writeFileSync('src/main/resources/web/index.html', crlfHtml, 'utf8');
fs.writeFileSync('bin/web/index.html', crlfHtml, 'utf8');
console.log('Successfully updated index.html in src and bin!');
