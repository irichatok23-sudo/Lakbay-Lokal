const fs = require('fs');

// We simulate the rendering logic extracted from app.js
function getActionsHtml(role, myProfileId, service) {
    const isAvail = service.available !== undefined ? Boolean(service.available) : Boolean(service.isAvailable);
    const isOwner = role === 'STAKEHOLDER' && myProfileId === Number(service.stakeholderId);
    const isTourismOversight = role === 'TOURISM_OFFICE';
    const canManage = isOwner || role === 'SYSTEM_ADMIN';

    let actionsHtml = '';
    if (isTourismOversight) {
        actionsHtml = `<span class="badge badge-info" title="Community-owned offering; monitored for safety and packaging standards"><i class="fa-solid fa-eye"></i> Oversight View Only</span>`;
    } else if (canManage) {
        actionsHtml = `
            <div style="display: flex; gap: 0.35rem; flex-wrap: wrap;">
                <button class="btn btn-sm btn-outline" onclick="openEditServiceModal(${service.serviceId})"><i class="fa-solid fa-pen"></i> Edit</button>
                <button class="btn btn-sm ${isAvail ? 'btn-outline' : 'btn-success'}" onclick="toggleServiceAvailability(${service.serviceId})">
                    ${isAvail ? 'Set Unavailable' : 'Set Available'}
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteServiceOffering(${service.serviceId})"><i class="fa-solid fa-trash"></i></button>
            </div>
        `;
    } else if (isAvail) {
        actionsHtml = role === 'TOURIST'
            ? `<button class="btn btn-sm btn-primary" onclick="openServiceBookingModal(${service.serviceId})"><i class="fa-solid fa-calendar-check"></i> Book</button>`
            : `<span class="badge badge-success"><i class="fa-solid fa-check"></i> Available</span>`;
    } else {
        actionsHtml = `<span class="text-muted">Unavailable</span>`;
    }
    return actionsHtml;
}

const testService = { serviceId: 101, stakeholderId: 5, serviceName: 'Guided Marine Snorkel Tour', available: true };

console.log('=== TEST 1: Tourism Office View ===');
const tourismHtml = getActionsHtml('TOURISM_OFFICE', 0, testService);
console.log('Output:', tourismHtml.trim());
console.log('Contains "Oversight View Only":', tourismHtml.includes('Oversight View Only'));
console.log('Has Edit button:', tourismHtml.includes('Edit'));
console.log('Has Delete button:', tourismHtml.includes('deleteServiceOffering'));
console.log('Has Toggle button:', tourismHtml.includes('toggleServiceAvailability'));

console.log('\n=== TEST 2: Community Worker (Owner - stakeholderId 5) View ===');
const ownerHtml = getActionsHtml('STAKEHOLDER', 5, testService);
console.log('Has Edit button:', ownerHtml.includes('Edit'));
console.log('Has Delete button:', ownerHtml.includes('deleteServiceOffering'));
console.log('Has Toggle button:', ownerHtml.includes('toggleServiceAvailability'));
console.log('Has "Oversight View Only":', ownerHtml.includes('Oversight View Only'));

console.log('\n=== TEST 3: Community Worker (Non-owner - stakeholderId 9) View ===');
const nonOwnerHtml = getActionsHtml('STAKEHOLDER', 9, testService);
console.log('Has Edit button:', nonOwnerHtml.includes('Edit'));
console.log('Has Delete button:', nonOwnerHtml.includes('deleteServiceOffering'));
console.log('Status badge displayed:', nonOwnerHtml.includes('Available'));

console.log('\n=== TEST 4: Tourist View ===');
const touristHtml = getActionsHtml('TOURIST', 0, testService);
console.log('Has Book button:', touristHtml.includes('Book'));
