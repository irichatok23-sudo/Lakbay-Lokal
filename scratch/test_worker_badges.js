// Unit test for role-based labels, filter visibility, and partner badges

function testRoleBadgesAndLabels(role, myProfileId, stakeholders) {
    // 1. Label simulation
    let navLabel = 'Community Worker Directory';
    let paneTitle = 'Community Worker Directory';
    let filterStatusVisible = true;

    if (role === 'STAKEHOLDER') {
        navLabel = 'Local Tourism Partner Network';
        paneTitle = 'Local Tourism Partner Network';
        filterStatusVisible = false;
    }

    // 2. Card badge simulation
    const cards = stakeholders.map(stk => {
        const isOwner = Number(myProfileId) === Number(stk.profileId);
        let partnerBadge = null;
        if (role === 'STAKEHOLDER') {
            partnerBadge = isOwner ? 'My Active Profile' : 'Cooperative Partner';
        }
        return {
            id: stk.profileId,
            name: stk.businessName,
            isOwner,
            badge: partnerBadge
        };
    });

    return { navLabel, paneTitle, filterStatusVisible, cards };
}

const mockStakeholders = [
    { profileId: 1, businessName: 'Pedro Island Treks' },
    { profileId: 2, businessName: 'Dela Cruz Habal-Habal' },
    { profileId: 3, businessName: 'Flores Motorized Bancas' }
];

console.log('=== TEST 1: Community Worker (guide_pedro, profileId 1) ===');
const cwResult = testRoleBadgesAndLabels('STAKEHOLDER', 1, mockStakeholders);
console.log('Nav Label:', cwResult.navLabel);
console.log('Pane Title:', cwResult.paneTitle);
console.log('Status Filter Visible:', cwResult.filterStatusVisible);
cwResult.cards.forEach(c => console.log(`Card ${c.id} (${c.name}): [${c.badge}]`));

console.log('\n=== TEST 2: Tourism Office (tourismoffice, profileId 0) ===');
const toResult = testRoleBadgesAndLabels('TOURISM_OFFICE', 0, mockStakeholders);
console.log('Nav Label:', toResult.navLabel);
console.log('Pane Title:', toResult.paneTitle);
console.log('Status Filter Visible:', toResult.filterStatusVisible);
toResult.cards.forEach(c => console.log(`Card ${c.id} (${c.name}): [${c.badge || 'No partner badge'}]`));

console.log('\n=== TEST 3: Municipal Tourism Officer (lgu_naval, profileId 0) ===');
const lguResult = testRoleBadgesAndLabels('LGU_OFFICER', 0, mockStakeholders);
console.log('Nav Label:', lguResult.navLabel);
console.log('Status Filter Visible:', lguResult.filterStatusVisible);

console.log('\n=== TEST 4: Tourist (tourist_ana, profileId 0) ===');
const touristResult = testRoleBadgesAndLabels('TOURIST', 0, mockStakeholders);
console.log('Nav Label:', touristResult.navLabel);
