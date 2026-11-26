const fs = require('fs');

console.log('=== PHASE 1: INSPECTION ===');

// 1. Check ApiController.java
const apiController = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');
const hasAuthMePost = apiController.includes('"/api/auth/me".equals(path) && "POST".equalsIgnoreCase(method)');
const hasHandleUpdateMe = apiController.includes('private String handleUpdateMe');
console.log('ApiController.java has POST /api/auth/me mapping:', hasAuthMePost);
console.log('ApiController.java has handleUpdateMe method:', hasHandleUpdateMe);

// 2. Check UserService.java
const userService = fs.readFileSync('src/main/java/com/lakbaylokal/service/UserService.java', 'utf8');
console.log('UserService.java has updateUser:', userService.includes('updateUser('));
console.log('UserService.java has resetPassword:', userService.includes('resetPassword('));

// 3. Check app.js
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
console.log('app.js has openViewDestinationModal:', appJs.includes('function openViewDestinationModal'));
console.log('app.js has openViewBookingModal:', appJs.includes('function openViewBookingModal'));
console.log('app.js has openUserProfileModal:', appJs.includes('function openUserProfileModal'));
console.log('app.js has handleUserProfileSubmit:', appJs.includes('function handleUserProfileSubmit'));
console.log('app.js has openLoginModal calling showLoginView:', appJs.includes('function openLoginModal() { showLoginView(); }'));
console.log('app.js has deleteDestination confirm:', appJs.includes('delete this destination?'));
console.log('app.js has deletePackage confirm:', appJs.includes('delete this package?'));
console.log('app.js has deleteAnnouncement confirm:', appJs.includes('delete this announcement?'));
console.log('app.js has deleteServiceOffering confirm:', appJs.includes('delete this service offering?'));
console.log('app.js has updateBookingStatus CANCELLED confirm:', appJs.includes('cancel this booking?'));

// 4. Check index.html
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
console.log('index.html has modal-view-destination:', indexHtml.includes('id="modal-view-destination"'));
console.log('index.html has modal-view-booking:', indexHtml.includes('id="modal-view-booking"'));
console.log('index.html has modal-user-profile:', indexHtml.includes('id="modal-user-profile"'));
console.log('index.html has auth-logged-section clickable for profile:', indexHtml.includes('onclick="openUserProfileModal()"'));

// 5. Check bin/web sync
const binAppJs = fs.existsSync('bin/web/js/app.js') ? fs.readFileSync('bin/web/js/app.js', 'utf8') : '';
const binIndexHtml = fs.existsSync('bin/web/index.html') ? fs.readFileSync('bin/web/index.html', 'utf8') : '';
console.log('bin/web/js/app.js in sync with src:', binAppJs === appJs);
console.log('bin/web/index.html in sync with src:', binIndexHtml === indexHtml);

// 6. Check backups
if (fs.existsSync('backup_phase2')) {
  console.log('backup_phase2 files:', fs.readdirSync('backup_phase2').slice(0, 10));
}
