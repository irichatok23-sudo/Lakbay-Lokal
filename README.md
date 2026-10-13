# 🌴 Lakbay Lokal: Community-Based Tourism Information System

**Target Locale:** Biliran Province, Region VIII (Eastern Visayas), Philippines  
**Platform:** Modern Windows Desktop Application (Electron + Java Embedded REST Backend + MySQL)  
**Public Web Fallback:** Embedded HTTP Server at `http://127.0.0.1:8080/`  

---

## 📌 1. Project Overview & Architecture

**Lakbay Lokal** is an integrated community-based tourism information and management platform designed to empower local stakeholders (island tour guides, boat operators, habal-habal drivers, homestay cooks, and local artisans) across Biliran Province, Philippines.

Originally developed as a web application, **Lakbay Lokal** has been transformed into a **native-feeling Windows desktop application** using **Electron** while preserving its Java backend, MySQL relational database architecture, and responsive web capabilities.

### 🏛️ System Architecture

```text
+---------------------------------------------------------------------------------------------------+
|                                     WINDOWS DESKTOP APPLICATION                                    |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  |                     Electron Desktop Shell (v33.4.11 / Chromium / Node.js)                  |  |
|  |  • Custom Integrated Titlebar (Minimize, Maximize, Close IPC)                               |  |
|  |  • Secure Renderer Isolation: contextIsolation: true, nodeIntegration: false, sandbox     |  |
|  |  • Safe Preload Bridge: window.desktopAPI (No database credentials exposed to frontend)     |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                │                                                  |
|                                                ▼                                                  |
|  +---------------------------------------------------------------------------------------------+  |
|  |                                  FRONTEND PRESENTATION LAYER                                |  |
|  |  • Preserved Public Homepage (Sambawan, Tinago Falls, Higatangan Sandbar, Ulan-Ulan)        |  |
|  |  • Authentic Glassmorphism Login Portal (Frosted glass panel, backdrop blur, test logins)   |  |
|  |  • 6-Role Role-Based Access Control (RBAC) Management Dashboard (16 Modular Panes)          |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                ▲                                                  |
|                                                │ Fetch API / JSON                                 |
|                                                ▼                                                  |
|  +---------------------------------------------------------------------------------------------+  |
|  |                          EMBEDDED JAVA HTTP BACKEND (Port 8080)                             |  |
|  |  • com.lakbaylokal.server.LakbayServer: Embedded HTTP server & static web server            |  |
|  |  • ApiController & REST Endpoints (/api/destinations, /api/auth/login, etc.)                |  |
|  |  • Business Service Layer (Auth, Verification, Bookings, Analytics, Reports, Audit Logs)    |  |
|  |  • Prepared Statement DAOs with SHA-256 + Salt password hashing                             |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                │                                                  |
|                                                ▼                                                  |
|  +---------------------------------------------------------------------------------------------+  |
|  |                                     DATABASE STORAGE LAYER                                  |  |
|  |  • MySQL 8.x Database (lakbay_lokal_db: 18 Tables + 3 Analytical Views)                     |  |
|  |  • Resilient Fallback Engine: Gracefully provides demo dataset if MySQL is offline         |  |
|  +---------------------------------------------------------------------------------------------+  |
+---------------------------------------------------------------------------------------------------+
```

---

## 🎨 2. Key Interface Improvements

### A. Preserved & Enhanced Public Homepage
- **Lakbay Lokal Branding:** Biliran seal and leaf badge with tagline *"Empowering communities, celebrating Biliran's natural heritage."*
- **Scenic Hero Section:** Authentic high-resolution photographs of Biliran wonders (Sambawan Island Marine Sanctuary, Tinago Cascades, Higatangan Shifting Sandbar, Ulan-Ulan Falls).
- **Interactive Destination Search & Filtering:** Real-time search by keyword and category pills (All, Islands, Waterfalls, Beaches, Mountains, Cultural).
- **Destination Cards:** Rich cards displaying authentic imagery, municipality badges, descriptions, and experience highlights.
- **Community Tourism Discovery:** Local stakeholder directory highlighting verified guides, boatmen, drivers, and local artisans.
- **Seamless Navigation:** Direct access to explore destinations and launch the Login Portal.

### B. Glassmorphism / Frosted-Glass Login Portal
- **Authentic Backdrop Filter Blur:** True CSS `backdrop-filter: blur(24px)` with semi-translucent glass paneling (`rgba(255, 255, 255, 0.08)`), subtle neon-teal glow, and delicate frosted borders.
- **Two-Column Split Layout:**
  - *Left Panel:* Lakbay Lokal mission, Biliran community tourism message, and scenic visual accents.
  - *Right Panel:* Accessible login form with email/username, password visibility toggle, remember me checkbox, and forgot password recovery modal.
- **1-Click Test Role Buttons:** Instant buttons for all 6 roles to automatically fill demo credentials and log in with zero typing required.

---

## 👥 3. The Six User Roles & Access Matrix

All pre-seeded test accounts use the password: **`password123`**

| Role | Username | Password | Purpose & Scope |
| :--- | :--- | :--- | :--- |
| **1. Tourist** | `tourist_ana` | `password123` | Ana Sophia Cortez (Visitor from Cebu) |
| **2. Stakeholder** | `guide_pedro` | `password123` | Pedro Penduko (Sambawan Eco-Trek Guide) |
| **2. Stakeholder** | `driver_juan` | `password123` | Juan Dela Cruz (Habal-Habal Adventure Transport) |
| **2. Stakeholder** | `boat_capitan` | `password123` | Capitan Danilo Flores (Maripipi Boat Operator) |
| **3. Barangay Official** | `brgy_olog` | `password123` | Capt. Ramon Corpin (Brgy. Ol-og / Sambawan) |
| **4. LGU Officer** | `lgu_naval` | `password123` | Katrina Gonzales (Naval Municipal Officer) |
| **5. Tourism Office** | `tourismoffice` | `password123` | Director Eduardo Reyes (Provincial Tourism) |
| **6. System Admin** | `admin` | `password123` | Maria Santos (Superuser / Universal Role Switcher) |

---

## 📦 4. The 16 Integrated System Modules

1. **Dashboard Overview:** Live KPI summary cards (Registered Users, Active Stakeholders, Verified Destinations, Pending Verifications), interactive charts, and recent activity logs.
2. **Destination Management:** Browse, search, filter, and register tourist spots across Biliran.
3. **Tourism Information Management:** Provincial guidelines, weather bulletins, and travel advisories.
4. **Local Stakeholder Management:** Directory of accredited guides, boatmen, drivers, and artisans.
5. **Service & Experience Management:** Stakeholder rate cards, itineraries, and package offerings.
6. **Package Management:** Community-based packages (Sambawan Explorer, Biliran Waterfalls Circuit, Higatangan Sandbar Escapade).
7. **Booking & Reservation Management:** Real-time booking requests, approvals, and status tracking.
8. **Stakeholder Verification:** Barangay & Tourism Office document verification workflow.
9. **Barangay Tourism Records:** Local jurisdiction footfall and carrying-capacity logging.
10. **Tourism Office & LGU Monitoring:** Inter-municipal oversight, consolidated footfall, and carrying-capacity alerts.
11. **Reviews & Feedback:** 5-star rating system with authentic tourist testimonials.
12. **Reports & Analytics:** Provincial arrival charts, revenue reports, and export options.
13. **User & Role Management:** Account activation, deactivation, and role permission assignment.
14. **System Settings:** Application configuration, offline modes, and branding options.
15. **Audit Logs:** Immutable security tracking of administrative actions and authentication events.
16. **Backup & Recovery:** Database snapshot dump generation and restore controls.

---

## 🚀 5. How to Run the Application (Beginner Guide)

### Option 1: 1-Click Desktop Launcher (Recommended)
Simply double-click:
```text
run_desktop.bat
```
This script automatically compiles the backend if needed and launches the Electron desktop application.

### Option 2: Running via Command Line
Open PowerShell or Command Prompt in the project folder and run:
```powershell
npm start
```

### Option 3: Running the Web Browser Version
Double-click `run.bat` (or run `.\run.ps1` in PowerShell).  
Then open your browser to: **`http://localhost:8080/`**

---

## 🔨 6. Building the Windows Installer & Executable

The project includes preconfigured scripts using `electron-builder` to package the app into standard Windows distributables:

### 1. Build the Unpacked Application (For testing without installer)
```powershell
.\powershell.cmd -File package_app.ps1
```
*Output location:* `dist\win-unpacked\Lakbay Lokal.exe`

### 2. Build the Portable Executable (Single .exe, no install needed)
```powershell
.\powershell.cmd -File build_installer.ps1
```
*Output location:* `dist\Lakbay Lokal 1.0.0.exe` (~79.5 MB)

### 3. Build the Full Windows Setup Installer (NSIS Installer)
```powershell
.\powershell.cmd -File build_nsis.ps1
```
*Output location:* `dist\Lakbay Lokal Setup 1.0.0.exe` (~92.3 MB)

---

## 🗄️ 7. Database Configuration (MySQL)

Lakbay Lokal is preconfigured to connect to a local MySQL server on port 3306.

### Setting Up MySQL:
1. Start MySQL (e.g. via XAMPP, WAMP, Docker, or standalone MySQL).
2. Create the database and import schema:
   ```bash
   mysql -u root -p < sql/01_schema.sql
   mysql -u root -p < sql/02_seed_data.sql
   mysql -u root -p < sql/03_views_procedures.sql
   ```
3. Copy `.env.example` to `.env` or set environment variables:
   ```env
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=lakbay_lokal_db
   DB_USER=root
   DB_PASSWORD=your_mysql_password
   ```

> **Note on Offline / Standalone Fallback:**  
> If MySQL is not running on your machine, **Lakbay Lokal will not crash**. The backend automatically detects the absence of the database and enables its integrated demo dataset so you can still test every feature, role, and dashboard module.

---

## 🧪 8. Testing & Verification Summary

| Component | Status | Details |
| :--- | :--- | :--- |
| **Java Backend Compilation** | ✅ PASSED | All source files compiled cleanly to `bin/` with UTF-8 encoding. |
| **Embedded Server (LakbayServer)** | ✅ PASSED | Serves HTTP REST endpoints and web frontend on port 8080. |
| **Public Homepage** | ✅ PASSED | Biliran scenic hero images, destination search, category pills, and community cards render cleanly. |
| **Glassmorphism Login Portal** | ✅ PASSED | Frosted-glass design with backdrop blur, password toggle, validation, and 1-click test roles verified. |
| **Electron Desktop Integration** | ✅ PASSED | `contextIsolation: true`, `nodeIntegration: false`, IPC window controls, and safe preload bridge verified. |
| **Unpacked Desktop Build** | ✅ PASSED | `dist\win-unpacked\Lakbay Lokal.exe` generated. |
| **Portable Windows Executable** | ✅ PASSED | `dist\Lakbay Lokal 1.0.0.exe` generated (79.5 MB). |
| **NSIS Windows Installer** | ✅ PASSED | `dist\Lakbay Lokal Setup 1.0.0.exe` generated (92.3 MB). |

---

## 📄 9. Deliverables Summary

1. **Desktop Shell:** Electron main (`electron/main.js`) and preload script (`electron/preload.js`).
2. **Custom Desktop UI:** Seamless dark forest-green titlebar with minimize, maximize, and close controls.
3. **Preserved Public Homepage:** Full Biliran tourist portal with search, category filtering, and stakeholder discovery.
4. **Glassmorphism Login:** Real CSS backdrop-blur frosted-glass panel with role quick-fill buttons.
5. **Role-Based Dashboards:** 6 distinct user roles across 16 fully functional system modules.
6. **Packaging:** Windows Portable Executable and NSIS Installer ready in `dist/`.
7. **Environment File:** `.env.example` with clear parameter documentation.
8. **1-Click Launchers:** `run_desktop.bat` for instant development launching.
