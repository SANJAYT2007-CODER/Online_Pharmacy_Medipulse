/* MediPulse Online Healthcare & Pharmacy Platform Logic */

let state = {
    currentRole: 'CUSTOMER',
    currentUser: null,
    activeCategory: null,
    medicines: [],
    categories: [],
    doctors: [],
    labTests: [],
    healthPackages: [],
    healthBlogs: [],
    cart: null,
    orders: [],
    prescriptions: [],
    doctorPrescriptions: [],
    addresses: [],
    wishlist: new Set(),
    activeRoute: '/'
};

let supabaseClient = null;

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
    await loadCurrentUser();
    await loadCategories();
    await loadMedicines();
    await loadDoctors();
    await loadLabTests();
    await loadHealthPackages();
    await loadHealthBlogs();
    await loadCart();
    await loadAddresses();
    await loadPrescriptions();
    await loadDoctorPrescriptions();
    await initSupabaseRealtime();
    
    // Handle Routing based on current URL path
    handleRouting(window.location.pathname);
    window.onpopstate = () => handleRouting(window.location.pathname);
});

// Currency Formatter Helper (Indian Rupee ₹)
function formatCurrency(amount) {
    const val = parseFloat(amount) || 0;
    return '₹' + val.toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Medicine Image Helper with Fallback
function getMedicineImageUrl(url) {
    if (!url || url.trim() === '') {
        return '/images/placeholder-medicine.svg';
    }
    return url;
}

// Global Search
function handleGlobalSearch(e) {
    const query = e.target.value;
    if (e.key === 'Enter' || query.length > 2 || query.length === 0) {
        if (state.activeRoute !== '/medicines') {
            navigateTo('/medicines');
        }
        document.getElementById('medSearchInput').value = query;
        filterMedicineCatalog();
    }
}

// Client-Side URL Routing
function navigateTo(path, event) {
    if (event) event.preventDefault();
    window.history.pushState({}, '', path);
    handleRouting(path);
}

function handleRouting(path) {
    state.activeRoute = path;
    
    // Hide all views
    document.querySelectorAll('.route-view').forEach(view => view.style.display = 'none');
    document.querySelectorAll('.cat-pill').forEach(pill => pill.classList.remove('active'));

    // Remove active role section display if route changes
    if (state.currentRole !== 'CUSTOMER') {
        const custView = document.getElementById('customerView');
        if (custView) custView.style.display = 'none';
    }

    if (path.startsWith('/medicines')) {
        document.getElementById('viewMedicines').style.display = 'block';
        document.getElementById('navMedicinesPill').classList.add('active');
        renderDedicatedMedicineCatalog();
    } else if (path.startsWith('/doctor-consult')) {
        document.getElementById('viewDoctorConsult').style.display = 'block';
        document.getElementById('navDoctorPill').classList.add('active');
        renderDoctorsGrid();
        loadMyConsultations();
    } else if (path.startsWith('/lab-tests')) {
        document.getElementById('viewLabTests').style.display = 'block';
        document.getElementById('navLabPill').classList.add('active');
        renderLabTestsGrid();
        loadMyLabBookings();
    } else if (path.startsWith('/healthcare-products')) {
        document.getElementById('viewHealthcareProducts').style.display = 'block';
        document.getElementById('navProductsPill').classList.add('active');
        renderHealthcareProductsGrid();
    } else if (path.startsWith('/health-check')) {
        document.getElementById('viewHealthCheck').style.display = 'block';
        document.getElementById('navCheckPill').classList.add('active');
        renderHealthPackagesGrid();
    } else if (path.startsWith('/health-blogs')) {
        document.getElementById('viewHealthBlogs').style.display = 'block';
        document.getElementById('navBlogsPill').classList.add('active');
        renderHealthBlogsGrid();
    } else if (path.startsWith('/branded-substitute')) {
        document.getElementById('viewBrandedSubstitute').style.display = 'block';
        renderSubstituteMedicinesGrid();
    } else if (path.startsWith('/plus')) {
        document.getElementById('viewPlus').style.display = 'block';
    } else if (path.startsWith('/offers')) {
        document.getElementById('viewOffers').style.display = 'block';
    } else if (path.startsWith('/value-store')) {
        document.getElementById('viewValueStore').style.display = 'block';
        renderValueStoreGrid();
    } else {
        // Default Home View
        document.getElementById('viewHome').style.display = 'block';
        document.getElementById('navHomePill').classList.add('active');
        renderMedicineCards();
        renderDoctorsGridHome();
        renderLabTestsGridHome();
        renderHealthcareProductsGridHome();
        renderHealthPackagesGridHome();
        renderHealthBlogsGridHome();
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Fetch config and setup Supabase subscriptions
async function initSupabaseRealtime() {
    try {
        const res = await fetch('/api/config/supabase');
        const config = await res.json();
        if (config.url && config.anonKey && 
            !config.url.includes("your-project") && 
            !config.anonKey.includes("your-anon-key")) {
            
            supabaseClient = supabase.createClient(config.url, config.anonKey);
            supabaseClient
                .channel('public:orders')
                .on('postgres_changes', { event: '*', schema: 'public', table: 'orders' }, () => {
                    if (state.currentRole === 'CUSTOMER') loadCustomerOrders();
                    else if (state.currentRole === 'PHARMACIST') loadPharmacistOrders();
                    else if (state.currentRole === 'ADMIN') loadAdminData();
                })
                .subscribe();
        }
    } catch (err) {
        console.error('Supabase init error:', err);
    }
}

// Load Current User
async function loadCurrentUser() {
    try {
        const res = await fetch('/api/auth/current');
        if (res.status === 401) {
            window.location.href = '/login';
            return;
        }
        state.currentUser = await res.json();
        state.currentRole = state.currentUser.role;
        
        document.getElementById('navUserEmail').innerText = state.currentUser.fullName || state.currentUser.email;
        
        const role = state.currentRole;
        document.querySelectorAll('.role-btn').forEach(btn => btn.classList.remove('active'));
        if (role === 'CUSTOMER') document.getElementById('roleCustomerBtn').classList.add('active');
        if (role === 'PHARMACIST') document.getElementById('rolePharmacistBtn').classList.add('active');
        if (role === 'ADMIN') document.getElementById('roleAdminBtn').classList.add('active');
        
        document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');
        if (role === 'CUSTOMER') {
            document.getElementById('viewHome').style.display = 'block';
        } else if (role === 'PHARMACIST') {
            document.getElementById('pharmacistView').style.display = 'block';
            loadPharmacistData();
        } else if (role === 'ADMIN') {
            document.getElementById('adminView').style.display = 'block';
            loadAdminData();
        }
    } catch (err) {
        console.error('Error fetching user:', err);
    }
}

// Logout User
async function logoutUser() {
    try {
        const res = await fetch('/api/auth/logout', { method: 'POST' });
        if (res.ok) window.location.href = '/login';
    } catch (err) {
        console.error('Logout failed:', err);
    }
}

// Role Switching Logic
async function switchUserRole(role) {
    state.currentRole = role;
    document.querySelectorAll('.role-btn').forEach(btn => btn.classList.remove('active'));
    if (role === 'CUSTOMER') document.getElementById('roleCustomerBtn').classList.add('active');
    if (role === 'PHARMACIST') document.getElementById('rolePharmacistBtn').classList.add('active');
    if (role === 'ADMIN') document.getElementById('roleAdminBtn').classList.add('active');

    document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');
    document.querySelectorAll('.route-view').forEach(view => view.style.display = 'none');

    try {
        await fetch(`/api/auth/switch-role/${role}`, { method: 'POST' });
        const res = await fetch('/api/auth/current');
        state.currentUser = await res.json();
        document.getElementById('navUserEmail').innerText = state.currentUser.fullName || state.currentUser.email;
    } catch (err) {
        console.error('Role switch failed', err);
    }

    if (role === 'CUSTOMER') {
        handleRouting(window.location.pathname);
    } else if (role === 'PHARMACIST') {
        document.getElementById('pharmacistView').style.display = 'block';
        loadPharmacistData();
    } else if (role === 'ADMIN') {
        document.getElementById('adminView').style.display = 'block';
        loadAdminData();
    }
}

// Sub-Tab Switcher for Customer Dashboard
function switchCustomerSubTab(tab) {
    document.getElementById('subTabCatalog').style.display = 'none';
    document.getElementById('subTabOrders').style.display = 'none';
    document.getElementById('subTabPrescriptions').style.display = 'none';
    
    document.getElementById('tabCatalogBtn').style.fontWeight = '500';
    document.getElementById('tabOrdersBtn').style.fontWeight = '500';
    document.getElementById('tabRxBtn').style.fontWeight = '500';

    if (tab === 'catalog') {
        document.getElementById('subTabCatalog').style.display = 'block';
        document.getElementById('tabCatalogBtn').style.fontWeight = '700';
    } else if (tab === 'orders') {
        document.getElementById('subTabOrders').style.display = 'block';
        document.getElementById('tabOrdersBtn').style.fontWeight = '700';
        loadCustomerOrders();
    } else if (tab === 'prescriptions') {
        document.getElementById('subTabPrescriptions').style.display = 'block';
        document.getElementById('tabRxBtn').style.fontWeight = '700';
        loadMyPrescriptions();
    }
}

// Categories & Medicines Loading
async function loadCategories() {
    try {
        const res = await fetch('/api/medicines/categories');
        state.categories = await res.json();
    } catch (err) {
        console.error('Error loading categories:', err);
    }
}

async function loadMedicines() {
    try {
        const res = await fetch('/api/medicines');
        state.medicines = await res.json();
        renderMedicineCards();
        renderHealthcareProductsGrid();
        renderHealthcareProductsGridHome();
    } catch (err) {
        console.error('Error loading medicines:', err);
    }
}

function renderMedicineCards() {
    const grid = document.getElementById('medicineGrid');
    if (!grid) return;
    const countLabel = document.getElementById('catalogCount');
    if (countLabel) countLabel.innerText = `Showing ${state.medicines.length} items`;

    if (state.medicines.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-box-open" style="font-size: 3rem; margin-bottom: 1rem;"></i><p>No medicines found.</p></div>`;
        return;
    }

    grid.innerHTML = state.medicines.slice(0, 8).map(med => renderSingleMedicineCard(med)).join('');
}

function renderSingleMedicineCard(med) {
    const hasDiscount = med.discount && parseFloat(med.discount) > 0;
    const discountPrice = hasDiscount 
        ? (parseFloat(med.price) * (1 - parseFloat(med.discount) / 100)).toFixed(2)
        : parseFloat(med.price).toFixed(2);

    const imgUrl = getMedicineImageUrl(med.imageUrl);

    return `
        <div class="medicine-card">
            <div class="card-image-wrap" onclick="openProductDetailModal(${med.medicineId})" style="cursor: pointer;">
                <img src="${imgUrl}" alt="${med.medicineName}" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                ${med.requiresPrescription ? '<span class="rx-badge"><i class="fa-solid fa-file-prescription"></i> Rx Required</span>' : '<span class="otc-badge"><i class="fa-solid fa-check"></i> OTC</span>'}
                ${hasDiscount ? `<span class="discount-tag">-${parseInt(med.discount)}% OFF</span>` : ''}
            </div>
            <div class="card-body">
                <div class="medicine-brand">${med.brand || 'Pharma'}</div>
                <div class="medicine-title" onclick="openProductDetailModal(${med.medicineId})" style="cursor: pointer;">${med.medicineName}</div>
                ${med.genericName ? `<div style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">Generic: ${med.genericName}</div>` : ''}
                ${med.manufacturer ? `<div style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.25rem;">Mfr: ${med.manufacturer}</div>` : ''}
                <div class="medicine-desc">${med.description || 'Quality formulation for healthcare.'}</div>
                <div style="font-size: 0.78rem; color: ${med.stockQuantity > 0 ? 'var(--success)' : 'var(--danger)'}; margin-bottom: 0.5rem; font-weight: 600;">
                    ${med.stockQuantity > 0 ? `In Stock (${med.stockQuantity} available)` : 'Out of Stock'}
                </div>
                <div class="card-footer-row">
                    <div class="price-box">
                        <span class="current-price">${formatCurrency(discountPrice)}</span>
                        ${hasDiscount ? `<span class="original-price">${formatCurrency(med.price)}</span>` : ''}
                    </div>
                    <div style="display: flex; gap: 0.4rem;">
                        <button class="btn btn-secondary" style="padding: 0.4rem 0.6rem; font-size: 0.8rem;" onclick="openProductDetailModal(${med.medicineId})">
                            View
                        </button>
                        <button class="btn btn-primary" ${med.stockQuantity <= 0 ? 'disabled style="opacity: 0.5;"' : ''} onclick="addToCart(${med.medicineId})">
                            <i class="fa-solid fa-cart-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// 1. DEDICATED MEDICINES PAGE CATALOG
function renderDedicatedMedicineCatalog() {
    filterMedicineCatalog();
}

function filterMedicineCatalog() {
    const grid = document.getElementById('dedicatedMedicineGrid');
    if (!grid) return;

    const query = (document.getElementById('medSearchInput') ? document.getElementById('medSearchInput').value : '').toLowerCase();
    const rxFilter = document.getElementById('medRxFilter') ? document.getElementById('medRxFilter').value : 'ALL';
    const sort = document.getElementById('medSortSelect') ? document.getElementById('medSortSelect').value : 'DEFAULT';

    let list = [...state.medicines];

    if (query) {
        list = list.filter(m => 
            (m.medicineName && m.medicineName.toLowerCase().includes(query)) ||
            (m.genericName && m.genericName.toLowerCase().includes(query)) ||
            (m.brand && m.brand.toLowerCase().includes(query)) ||
            (m.manufacturer && m.manufacturer.toLowerCase().includes(query))
        );
    }

    if (rxFilter === 'RX') {
        list = list.filter(m => m.requiresPrescription === true);
    } else if (rxFilter === 'OTC') {
        list = list.filter(m => !m.requiresPrescription);
    }

    if (sort === 'PRICE_LOW') {
        list.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
    } else if (sort === 'PRICE_HIGH') {
        list.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
    } else if (sort === 'NAME_AZ') {
        list.sort((a, b) => a.medicineName.localeCompare(b.medicineName));
    }

    document.getElementById('medCountLabel').innerText = `Showing ${list.length} medicines`;

    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-pills" style="font-size: 3rem; margin-bottom: 1rem;"></i><p>No medicines match your search criteria.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(med => renderSingleMedicineCard(med)).join('');
}

// 2. DOCTOR CONSULTATION SYSTEM
async function loadDoctors() {
    try {
        const res = await fetch('/api/doctors');
        state.doctors = await res.json();
        renderDoctorsGrid();
        renderDoctorsGridHome();
    } catch (err) {
        console.error('Error loading doctors:', err);
    }
}

function renderDoctorsGrid(spec = '') {
    const grid = document.getElementById('doctorsGrid');
    if (!grid) return;

    let list = state.doctors;
    if (spec) {
        list = list.filter(d => d.specialization.toLowerCase().includes(spec.toLowerCase()));
    }

    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-user-doctor" style="font-size: 3rem; margin-bottom: 1rem;"></i><p>No doctors found in this specialization.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(doc => `
        <div class="doctor-card">
            <div style="display: flex; gap: 1rem; margin-bottom: 1rem; align-items: center;">
                <div style="width: 52px; height: 52px; border-radius: 12px; background: linear-gradient(135deg, #e0f2fe, #bae6fd); color: #0284c7; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0;">
                    <i class="fa-solid fa-user-doctor"></i>
                </div>
                <div>
                    <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.15rem; color: var(--text-dark);">${doc.name}</h3>
                    <div style="font-size: 0.85rem; color: var(--primary); font-weight: 700;">${doc.specialization}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${doc.qualification} • ${doc.experienceYears} Yrs Exp</div>
                </div>
            </div>
            <div style="background: #f8fafc; padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Consultation Fee:</span>
                <span style="font-size: 1.1rem; font-weight: 800; color: var(--text-dark); font-family: 'Outfit', sans-serif;">${formatCurrency(doc.consultationFee)}</span>
            </div>
            <button class="btn btn-primary" style="width: 100%; background: #0284c7;" onclick="openDoctorBookingModal(${doc.doctorId})">
                <i class="fa-solid fa-calendar-check"></i> Book Consultation
            </button>
        </div>
    `).join('');
}

function renderDoctorsGridHome() {
    const grid = document.getElementById('doctorsGridHome');
    if (!grid) return;

    const list = state.doctors.slice(0, 4);
    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);"><p>No doctors available right now.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(doc => `
        <div class="doctor-card">
            <div style="display: flex; gap: 1rem; margin-bottom: 1rem; align-items: center;">
                <div style="width: 52px; height: 52px; border-radius: 12px; background: linear-gradient(135deg, #e0f2fe, #bae6fd); color: #0284c7; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0;">
                    <i class="fa-solid fa-user-doctor"></i>
                </div>
                <div>
                    <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.15rem; color: var(--text-dark);">${doc.name}</h3>
                    <div style="font-size: 0.85rem; color: var(--primary); font-weight: 700;">${doc.specialization}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${doc.qualification} • ${doc.experienceYears} Yrs Exp</div>
                </div>
            </div>
            <div style="background: #f8fafc; padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Consultation Fee:</span>
                <span style="font-size: 1.1rem; font-weight: 800; color: var(--text-dark); font-family: 'Outfit', sans-serif;">${formatCurrency(doc.consultationFee)}</span>
            </div>
            <button class="btn btn-primary" style="width: 100%; background: #0284c7;" onclick="openDoctorBookingModal(${doc.doctorId})">
                <i class="fa-solid fa-calendar-check"></i> Book Consultation
            </button>
        </div>
    `).join('');
}

function filterDoctorsBySpec(spec, element) {
    document.querySelectorAll('#doctorSpecPills button').forEach(b => b.classList.remove('active'));
    element.classList.add('active');
    renderDoctorsGrid(spec);
}

function openDoctorBookingModal(doctorId) {
    const doc = state.doctors.find(d => d.doctorId === doctorId);
    if (!doc) return;

    document.getElementById('bookingDoctorId').value = doc.doctorId;
    document.getElementById('docBookingModalTitle').innerHTML = `<i class="fa-solid fa-user-doctor"></i> Book Consultation with ${doc.name}`;
    document.getElementById('bookingFeeLabel').innerText = formatCurrency(doc.consultationFee);
    
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('bookingDate').value = today;

    // Time slots
    const slots = doc.availableTimeSlots ? doc.availableTimeSlots.split(',') : ['10:00 AM', '02:00 PM', '05:00 PM'];
    const select = document.getElementById('bookingTimeSlot');
    select.innerHTML = slots.map(s => `<option value="${s.trim()}">${s.trim()}</option>`).join('');

    document.getElementById('doctorBookingModal').classList.add('active');
}

function closeDoctorBookingModal() {
    document.getElementById('doctorBookingModal').classList.remove('active');
}

async function submitDoctorBooking(e) {
    e.preventDefault();
    const doctorId = document.getElementById('bookingDoctorId').value;
    const patientName = document.getElementById('bookingPatientName').value;
    const appointmentDate = document.getElementById('bookingDate').value;
    const appointmentTime = document.getElementById('bookingTimeSlot').value;
    const notes = document.getElementById('bookingNotes').value;

    try {
        const res = await fetch('/api/doctors/consultations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ doctorId, patientName, appointmentDate, appointmentTime, notes })
        });
        if (res.ok) {
            closeDoctorBookingModal();
            showNotification('Doctor Consultation Booked Successfully!');
            loadMyConsultations();
        } else {
            const err = await res.json();
            alert(err.error || 'Failed to book consultation.');
        }
    } catch (err) {
        console.error('Booking error:', err);
    }
}

async function loadMyConsultations() {
    try {
        const res = await fetch('/api/doctors/consultations/my');
        const list = await res.json();
        const container = document.getElementById('doctorConsultationsList');
        if (!container) return;

        if (list.length === 0) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);"><p>No consultations booked yet.</p></div>`;
            return;
        }

        container.innerHTML = list.map(c => `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
                    <div>
                        <strong style="font-size: 1.1rem;">Consultation #${c.consultationId} - ${c.doctor.name}</strong>
                        <span style="font-size: 0.85rem; color: var(--primary); font-weight: 700; margin-left: 0.5rem;">(${c.doctor.specialization})</span>
                    </div>
                    <span class="status-pill status-APPROVED">${c.status}</span>
                </div>
                <div style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 0.5rem;">
                    <strong>Patient:</strong> ${c.patientName} • <strong>Date:</strong> ${c.appointmentDate} at ${c.appointmentTime} • <strong>Fee:</strong> ${formatCurrency(c.consultationFee)}
                </div>
                ${c.notes ? `<div style="font-size: 0.85rem; background: #f8fafc; padding: 0.5rem 0.75rem; border-radius: 6px; color: var(--text-dark);">Notes: ${c.notes}</div>` : ''}
            </div>
        `).join('');
    } catch (err) {
        console.error('Error loading consultations:', err);
    }
}

// 3. LAB TESTS SYSTEM
async function loadLabTests() {
    try {
        const res = await fetch('/api/labs/tests');
        state.labTests = await res.json();
        renderLabTestsGrid();
        renderLabTestsGridHome();
    } catch (err) {
        console.error('Error loading lab tests:', err);
    }
}

function renderLabTestsGrid() {
    const grid = document.getElementById('labTestsGrid');
    if (!grid) return;

    grid.innerHTML = state.labTests.map(test => `
        <div class="lab-card">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                    <span style="background: #fae8ff; color: #9333ea; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">${test.category}</span>
                    <span style="color: var(--success); font-weight: 700; font-size: 0.85rem;">-${parseInt(test.discount)}% OFF</span>
                </div>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.2rem; color: var(--text-dark); margin-bottom: 0.4rem;">${test.testName}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.75rem; line-height: 1.4;">${test.description}</p>
                ${test.prepInstructions ? `<div style="font-size: 0.8rem; background: #fdf4ff; border-left: 3px solid #9333ea; padding: 0.4rem 0.6rem; border-radius: 4px; margin-bottom: 1rem; color: #6b21a8;"><i class="fa-solid fa-circle-info"></i> ${test.prepInstructions}</div>` : ''}
            </div>
            <div>
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
                    <div>
                        <span style="font-size: 1.25rem; font-weight: 800; font-family: 'Outfit', sans-serif;">${formatCurrency(parseFloat(test.price) * (1 - parseFloat(test.discount)/100))}</span>
                        <span style="text-decoration: line-through; color: var(--text-muted); font-size: 0.85rem; margin-left: 0.4rem;">${formatCurrency(test.price)}</span>
                    </div>
                </div>
                <button class="btn btn-primary" style="width: 100%; background: #9333ea;" onclick="openLabBookingModal('${test.testName}', ${(parseFloat(test.price) * (1 - parseFloat(test.discount)/100)).toFixed(2)})">
                    <i class="fa-solid fa-calendar-plus"></i> Book Test
                </button>
            </div>
        </div>
    `).join('');
}

function renderLabTestsGridHome() {
    const grid = document.getElementById('labTestsGridHome');
    if (!grid) return;

    const list = state.labTests.slice(0, 4);
    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);"><p>No lab tests available.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(test => `
        <div class="lab-card">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                    <span style="background: #fae8ff; color: #9333ea; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">${test.category}</span>
                    <span style="color: var(--success); font-weight: 700; font-size: 0.85rem;">-${parseInt(test.discount)}% OFF</span>
                </div>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.2rem; color: var(--text-dark); margin-bottom: 0.4rem;">${test.testName}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.75rem; line-height: 1.4;">${test.description}</p>
                ${test.prepInstructions ? `<div style="font-size: 0.8rem; background: #fdf4ff; border-left: 3px solid #9333ea; padding: 0.4rem 0.6rem; border-radius: 4px; margin-bottom: 1rem; color: #6b21a8;"><i class="fa-solid fa-circle-info"></i> ${test.prepInstructions}</div>` : ''}
            </div>
            <div>
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
                    <div>
                        <span style="font-size: 1.25rem; font-weight: 800; font-family: 'Outfit', sans-serif;">${formatCurrency(parseFloat(test.price) * (1 - parseFloat(test.discount)/100))}</span>
                        <span style="text-decoration: line-through; color: var(--text-muted); font-size: 0.85rem; margin-left: 0.4rem;">${formatCurrency(test.price)}</span>
                    </div>
                </div>
                <button class="btn btn-primary" style="width: 100%; background: #9333ea;" onclick="openLabBookingModal('${test.testName}', ${(parseFloat(test.price) * (1 - parseFloat(test.discount)/100)).toFixed(2)})">
                    <i class="fa-solid fa-calendar-plus"></i> Book Test
                </button>
            </div>
        </div>
    `).join('');
}

function openLabBookingModal(testName, price) {
    document.getElementById('labTestName').value = testName;
    document.getElementById('labTestPrice').value = price;
    document.getElementById('labTestDisplay').value = testName;
    document.getElementById('labPriceDisplay').innerText = formatCurrency(price);
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('labDate').value = today;
    document.getElementById('labBookingModal').classList.add('active');
}

function closeLabBookingModal() {
    document.getElementById('labBookingModal').classList.remove('active');
}

async function submitLabBooking(e) {
    e.preventDefault();
    const testName = document.getElementById('labTestName').value;
    const price = document.getElementById('labTestPrice').value;
    const patientName = document.getElementById('labPatientName').value;
    const bookingDate = document.getElementById('labDate').value;
    const bookingTime = document.getElementById('labTime').value;

    try {
        const res = await fetch('/api/labs/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ testName, price, patientName, bookingDate, bookingTime })
        });
        if (res.ok) {
            closeLabBookingModal();
            showNotification('Lab Test Booked Successfully!');
            loadMyLabBookings();
        }
    } catch (err) {
        console.error('Lab booking error:', err);
    }
}

async function loadMyLabBookings() {
    try {
        const res = await fetch('/api/labs/bookings/my');
        const list = await res.json();
        const container = document.getElementById('labBookingsList');
        if (!container) return;

        if (list.length === 0) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);"><p>No lab tests booked yet.</p></div>`;
            return;
        }

        container.innerHTML = list.map(b => `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1rem 1.25rem; margin-bottom: 0.85rem; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <strong style="font-size: 1.05rem;">${b.testName}</strong>
                    <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.2rem;">Patient: ${b.patientName} • Sample Collection: ${b.bookingDate} (${b.bookingTime})</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 800; color: #9333ea;">${formatCurrency(b.totalPrice)}</div>
                    <span class="status-pill status-APPROVED">${b.status}</span>
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Error loading lab bookings:', err);
    }
}

// 4. HEALTHCARE PRODUCTS STORE
function renderHealthcareProductsGrid() {
    const grid = document.getElementById('healthcareProductsGrid');
    if (!grid) return;

    // Filter OTC or non-prescription products
    const list = state.medicines.filter(m => !m.requiresPrescription || m.category.categoryName.includes('Vitamins') || m.category.categoryName.includes('General'));

    grid.innerHTML = list.map(prod => {
        const hasDiscount = prod.discount && parseFloat(prod.discount) > 0;
        const discountPrice = hasDiscount 
            ? (parseFloat(prod.price) * (1 - parseFloat(prod.discount) / 100)).toFixed(2)
            : parseFloat(prod.price).toFixed(2);
        const isWishlisted = state.wishlist.has(prod.medicineId);

        return `
            <div class="medicine-card">
                <div class="card-image-wrap" onclick="openProductDetailModal(${prod.medicineId})" style="cursor: pointer;">
                    <img src="${getMedicineImageUrl(prod.imageUrl)}" alt="${prod.medicineName}" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                    <button style="position: absolute; top: 10px; right: 10px; border: none; background: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: ${isWishlisted ? '#ef4444' : '#94a3b8'}; cursor: pointer; box-shadow: var(--shadow-sm);" onclick="toggleWishlist(${prod.medicineId}, event)">
                        <i class="fa-solid fa-heart"></i>
                    </button>
                    ${hasDiscount ? `<span class="discount-tag">-${parseInt(prod.discount)}% OFF</span>` : ''}
                </div>
                <div class="card-body">
                    <div class="medicine-brand">${prod.brand || 'Wellness'}</div>
                    <div class="medicine-title">${prod.medicineName}</div>
                    <div class="medicine-desc">${prod.description || 'Premium health and wellness product.'}</div>
                    <div class="card-footer-row">
                        <div class="price-box">
                            <span class="current-price">${formatCurrency(discountPrice)}</span>
                            ${hasDiscount ? `<span class="original-price">${formatCurrency(prod.price)}</span>` : ''}
                        </div>
                        <button class="btn btn-primary" style="background: #d97706;" onclick="addToCart(${prod.medicineId})">
                            <i class="fa-solid fa-cart-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderHealthcareProductsGridHome() {
    const grid = document.getElementById('healthcareProductsGridHome');
    if (!grid) return;

    const list = state.medicines.filter(m => !m.requiresPrescription || (m.category && m.category.categoryName && (m.category.categoryName.includes('Vitamins') || m.category.categoryName.includes('General')))).slice(0, 4);

    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);"><p>No products found.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(prod => {
        const hasDiscount = prod.discount && parseFloat(prod.discount) > 0;
        const discountPrice = hasDiscount 
            ? (parseFloat(prod.price) * (1 - parseFloat(prod.discount) / 100)).toFixed(2)
            : parseFloat(prod.price).toFixed(2);
        const isWishlisted = state.wishlist.has(prod.medicineId);

        return `
            <div class="medicine-card">
                <div class="card-image-wrap" onclick="openProductDetailModal(${prod.medicineId})" style="cursor: pointer;">
                    <img src="${getMedicineImageUrl(prod.imageUrl)}" alt="${prod.medicineName}" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                    <button style="position: absolute; top: 10px; right: 10px; border: none; background: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: ${isWishlisted ? '#ef4444' : '#94a3b8'}; cursor: pointer; box-shadow: var(--shadow-sm);" onclick="toggleWishlist(${prod.medicineId}, event)">
                        <i class="fa-solid fa-heart"></i>
                    </button>
                    ${hasDiscount ? `<span class="discount-tag">-${parseInt(prod.discount)}% OFF</span>` : ''}
                </div>
                <div class="card-body">
                    <div class="medicine-brand">${prod.brand || 'Wellness'}</div>
                    <div class="medicine-title">${prod.medicineName}</div>
                    <div class="medicine-desc">${prod.description || 'Premium health and wellness product.'}</div>
                    <div class="card-footer-row">
                        <div class="price-box">
                            <span class="current-price">${formatCurrency(discountPrice)}</span>
                            ${hasDiscount ? `<span class="original-price">${formatCurrency(prod.price)}</span>` : ''}
                        </div>
                        <button class="btn btn-primary" style="background: #d97706;" onclick="addToCart(${prod.medicineId})">
                            <i class="fa-solid fa-cart-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function toggleWishlist(prodId, event) {
    if (event) event.stopPropagation();
    if (state.wishlist.has(prodId)) {
        state.wishlist.delete(prodId);
        showNotification('Removed from Wishlist');
    } else {
        state.wishlist.add(prodId);
        showNotification('Added to Wishlist!');
    }
    renderHealthcareProductsGrid();
}

// 5. HEALTH CHECK PACKAGES
async function loadHealthPackages() {
    try {
        const res = await fetch('/api/health-checks/packages');
        state.healthPackages = await res.json();
        renderHealthPackagesGrid();
        renderHealthPackagesGridHome();
    } catch (err) {
        console.error('Error loading packages:', err);
    }
}

function renderHealthPackagesGrid() {
    const grid = document.getElementById('healthPackagesGrid');
    if (!grid) return;

    grid.innerHTML = state.healthPackages.map(pkg => `
        <div class="package-card">
            <div>
                <span style="background: #ffe4e6; color: #e11d48; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">PREVENTIVE PACKAGE</span>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.3rem; color: var(--text-dark); margin: 0.5rem 0 0.3rem 0;">${pkg.packageName}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.75rem;">${pkg.description}</p>
                <div style="background: #fff1f2; padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem;">
                    <strong style="font-size: 0.82rem; color: #be123c;">Includes Tests:</strong>
                    <div style="font-size: 0.8rem; color: #9f1239; margin-top: 0.2rem;">${pkg.includedTests}</div>
                </div>
            </div>
            <div>
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
                    <div>
                        <span style="font-size: 1.4rem; font-weight: 800; font-family: 'Outfit', sans-serif; color: #e11d48;">${formatCurrency(pkg.discountedPrice)}</span>
                        <span style="text-decoration: line-through; color: var(--text-muted); font-size: 0.9rem; margin-left: 0.4rem;">${formatCurrency(pkg.originalPrice)}</span>
                    </div>
                </div>
                <button class="btn btn-primary" style="width: 100%; background: #e11d48;" onclick="openPackageBookingModal('${pkg.packageName}', ${pkg.discountedPrice})">
                    <i class="fa-solid fa-heart-circle-check"></i> Book Checkup Now
                </button>
            </div>
        </div>
    `).join('');
}

function renderHealthPackagesGridHome() {
    const grid = document.getElementById('healthPackagesGridHome');
    if (!grid) return;

    const list = state.healthPackages.slice(0, 4);
    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);"><p>No health check packages available.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(pkg => `
        <div class="package-card">
            <div>
                <span style="background: #ffe4e6; color: #e11d48; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">PREVENTIVE PACKAGE</span>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.3rem; color: var(--text-dark); margin: 0.5rem 0 0.3rem 0;">${pkg.packageName}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.75rem;">${pkg.description}</p>
                <div style="background: #fff1f2; padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem;">
                    <strong style="font-size: 0.82rem; color: #be123c;">Includes Tests:</strong>
                    <div style="font-size: 0.8rem; color: #9f1239; margin-top: 0.2rem;">${pkg.includedTests}</div>
                </div>
            </div>
            <div>
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
                    <div>
                        <span style="font-size: 1.4rem; font-weight: 800; font-family: 'Outfit', sans-serif; color: #e11d48;">${formatCurrency(pkg.discountedPrice)}</span>
                        <span style="text-decoration: line-through; color: var(--text-muted); font-size: 0.9rem; margin-left: 0.4rem;">${formatCurrency(pkg.originalPrice)}</span>
                    </div>
                </div>
                <button class="btn btn-primary" style="width: 100%; background: #e11d48;" onclick="openPackageBookingModal('${pkg.packageName}', ${pkg.discountedPrice})">
                    <i class="fa-solid fa-heart-circle-check"></i> Book Checkup Now
                </button>
            </div>
        </div>
    `).join('');
}

function openPackageBookingModal(name, price) {
    document.getElementById('pkgName').value = name;
    document.getElementById('pkgPrice').value = price;
    document.getElementById('pkgDisplay').value = name;
    document.getElementById('pkgPriceDisplay').innerText = formatCurrency(price);
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('pkgDate').value = today;
    document.getElementById('packageBookingModal').classList.add('active');
}

function closePackageBookingModal() {
    document.getElementById('packageBookingModal').classList.remove('active');
}

async function submitPackageBooking(e) {
    e.preventDefault();
    const packageName = document.getElementById('pkgName').value;
    const price = document.getElementById('pkgPrice').value;
    const patientName = document.getElementById('pkgPatientName').value;
    const bookingDate = document.getElementById('pkgDate').value;
    const bookingTime = document.getElementById('pkgTime').value;

    try {
        const res = await fetch('/api/health-checks/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ packageName, price, patientName, bookingDate, bookingTime })
        });
        if (res.ok) {
            closePackageBookingModal();
            showNotification('Health Package Booked Successfully!');
        }
    } catch (err) {
        console.error('Package booking error:', err);
    }
}

// 6. HEALTH BLOGS SYSTEM
async function loadHealthBlogs() {
    try {
        const res = await fetch('/api/blogs');
        state.healthBlogs = await res.json();
        renderHealthBlogsGrid();
        renderHealthBlogsGridHome();
    } catch (err) {
        console.error('Error loading blogs:', err);
    }
}

function renderHealthBlogsGrid() {
    const grid = document.getElementById('healthBlogsGrid');
    if (!grid) return;

    grid.innerHTML = state.healthBlogs.map(blog => `
        <div class="blog-card">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="background: #ccfbf1; color: #0d9488; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">${blog.category}</span>
                    <span style="font-size: 0.78rem; color: var(--text-muted);">${blog.publishedDate}</span>
                </div>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.15rem; color: var(--text-dark); margin-bottom: 0.5rem;">${blog.title}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem; line-height: 1.45;">${blog.shortDescription}</p>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                <span style="font-size: 0.8rem; font-weight: 600; color: var(--text-dark);"><i class="fa-solid fa-user-pen"></i> ${blog.author}</span>
                <button class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="openBlogModal(${blog.blogId})">
                    Read More <i class="fa-solid fa-arrow-right"></i>
                </button>
            </div>
        </div>
    `).join('');
}

function renderHealthBlogsGridHome() {
    const grid = document.getElementById('healthBlogsGridHome');
    if (!grid) return;

    const list = state.healthBlogs.slice(0, 3);
    if (list.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);"><p>No health blogs found.</p></div>`;
        return;
    }

    grid.innerHTML = list.map(blog => `
        <div class="blog-card">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="background: #ccfbf1; color: #0d9488; font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 50px;">${blog.category}</span>
                    <span style="font-size: 0.78rem; color: var(--text-muted);">${blog.publishedDate}</span>
                </div>
                <h3 style="font-family: 'Outfit', sans-serif; font-size: 1.15rem; color: var(--text-dark); margin-bottom: 0.5rem;">${blog.title}</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem; line-height: 1.45;">${blog.shortDescription}</p>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                <span style="font-size: 0.8rem; font-weight: 600; color: var(--text-dark);"><i class="fa-solid fa-user-pen"></i> ${blog.author}</span>
                <button class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="openBlogModal(${blog.blogId})">
                    Read More <i class="fa-solid fa-arrow-right"></i>
                </button>
            </div>
        </div>
    `).join('');
}

function openBlogModal(blogId) {
    const blog = state.healthBlogs.find(b => b.blogId === blogId);
    if (!blog) return;

    document.getElementById('blogModalTitle').innerHTML = `<i class="fa-solid fa-book-open"></i> ${blog.title}`;
    document.getElementById('blogModalBody').innerHTML = `
        <div style="margin-bottom: 1rem; font-size: 0.85rem; color: var(--text-muted);">
            By <strong>${blog.author}</strong> • Published on ${blog.publishedDate} • Category: <span style="color: #0d9488; font-weight: 700;">${blog.category}</span>
        </div>
        <div style="font-size: 1rem; line-height: 1.7; color: var(--text-dark); background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
            ${blog.fullContent}
        </div>
    `;
    document.getElementById('blogModal.classList' ? 'blogModal' : 'blogModal').classList.add('active');
}

function closeBlogModal() {
    document.getElementById('blogModal').classList.remove('active');
}

// TASK 2: DOCTOR PRESCRIPTION → MEDICINE PURCHASE WORKFLOW
async function loadDoctorPrescriptions() {
    try {
        const res = await fetch('/api/doctor-prescriptions/my');
        state.doctorPrescriptions = await res.json();
    } catch (err) {
        console.error('Error loading doctor rxs:', err);
    }
}

async function loadMyPrescriptions() {
    await loadDoctorPrescriptions();
    await loadPrescriptions();
    renderCustomerPrescriptionsList();
}

function renderCustomerPrescriptionsList() {
    const container = document.getElementById('customerPrescriptionsList');
    if (!container) return;

    if (state.doctorPrescriptions.length === 0 && state.prescriptions.length === 0) {
        container.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-receipt" style="font-size: 3rem; margin-bottom: 1rem;"></i><p>No digital or uploaded prescriptions found.</p></div>`;
        return;
    }

    let html = '';

    // Digital Doctor Prescriptions
    state.doctorPrescriptions.forEach(rx => {
        html += `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.25rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
                    <div>
                        <strong style="font-size: 1.15rem; font-family: 'Outfit', sans-serif;">Digital Rx #${rx.rxId}</strong>
                        <span style="font-size: 0.85rem; color: var(--text-muted); margin-left: 0.75rem;">Doctor: <strong>${rx.doctorName}</strong> (${rx.doctorSpecialization})</span>
                    </div>
                    <span class="status-pill status-APPROVED">${rx.status}</span>
                </div>

                <div style="margin-bottom: 1rem; font-size: 0.9rem;">
                    <div><strong>Patient Name:</strong> ${rx.patientName} • <strong>Date:</strong> ${rx.consultationDate}</div>
                    ${rx.diagnosisNotes ? `<div style="margin-top: 0.4rem; color: var(--text-muted);"><strong>Diagnosis:</strong> ${rx.diagnosisNotes}</div>` : ''}
                </div>

                <div style="background: #f8fafc; padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem;">
                    <strong style="font-size: 0.85rem;">Prescribed Medicines:</strong>
                    ${rx.items.map(item => `
                        <div style="display: flex; justify-content: space-between; font-size: 0.88rem; margin-top: 0.3rem;">
                            <span><i class="fa-solid fa-pills" style="color: var(--primary);"></i> <strong>${item.medicineName}</strong> (${item.dosage}) - ${item.frequency} for ${item.duration} [${item.timing}]</span>
                            <span style="font-weight: 700;">${formatCurrency(item.price)}</span>
                        </div>
                    `).join('')}
                </div>

                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <button class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="openRxDetailModal(${rx.rxId})">
                        <i class="fa-solid fa-eye"></i> View Prescription
                    </button>
                    <button class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="printRxDetail(${rx.rxId})">
                        <i class="fa-solid fa-print"></i> Print Rx
                    </button>
                    <button class="btn btn-primary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="buyMedicinesFromRx(${rx.rxId})">
                        <i class="fa-solid fa-cart-plus"></i> Buy Prescribed Medicines
                    </button>
                </div>
            </div>
        `;
    });

    // Uploaded Prescriptions
    state.prescriptions.forEach(rx => {
        html += `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.25rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
                    <div>
                        <strong style="font-size: 1.15rem; font-family: 'Outfit', sans-serif;">Uploaded Rx #${rx.prescriptionId}</strong>
                        <span style="font-size: 0.85rem; color: var(--text-muted); margin-left: 0.75rem;">Uploaded on: ${new Date(rx.uploadedAt).toLocaleString()}</span>
                    </div>
                    <span class="status-pill status-${rx.status}">${rx.status}</span>
                </div>
                <div style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 1rem;">Notes: ${rx.notes || 'Document uploaded'}</div>
                <button class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="previewImage('${rx.imageUrl}')">
                    <i class="fa-solid fa-file-image"></i> View Document
                </button>
            </div>
        `;
    });

    container.innerHTML = html;
}

function openRxDetailModal(rxId) {
    const rx = state.doctorPrescriptions.find(r => r.rxId === rxId);
    if (!rx) return;

    document.getElementById('rxModalDetailBody').innerHTML = `
        <div style="background: #ffffff; border: 2px stroke #e2e8f0; padding: 1.5rem; border-radius: 12px;">
            <div style="display: flex; justify-content: space-between; border-bottom: 2px solid var(--primary); padding-bottom: 0.75rem; margin-bottom: 1rem;">
                <div>
                    <h2 style="font-family: 'Outfit', sans-serif; color: var(--primary);">${rx.doctorName}</h2>
                    <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted);">${rx.doctorSpecialization}</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 800; font-size: 1.2rem; font-family: 'Outfit', sans-serif;">MediPulse Rx</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">Date: ${rx.consultationDate}</div>
                </div>
            </div>

            <div style="margin-bottom: 1rem; font-size: 0.95rem;">
                <strong>Patient Name:</strong> ${rx.patientName}
            </div>

            ${rx.diagnosisNotes ? `<div style="background: #f8fafc; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.9rem;"><strong>Diagnosis / Clinical Notes:</strong> ${rx.diagnosisNotes}</div>` : ''}

            <table class="custom-table" style="margin-bottom: 1.5rem;">
                <thead>
                    <tr>
                        <th>Medicine</th>
                        <th>Dosage</th>
                        <th>Frequency</th>
                        <th>Duration</th>
                        <th>Instructions</th>
                        <th>Price</th>
                    </tr>
                </thead>
                <tbody>
                    ${rx.items.map(item => `
                        <tr>
                            <td><strong>${item.medicineName}</strong></td>
                            <td>${item.dosage}</td>
                            <td>${item.frequency}</td>
                            <td>${item.duration}</td>
                            <td>${item.instructions} [${item.timing}]</td>
                            <td><strong>${formatCurrency(item.price)}</strong></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>

            <button class="btn btn-primary" style="width: 100%;" onclick="buyMedicinesFromRx(${rx.rxId}); closeRxDetailModal();">
                <i class="fa-solid fa-cart-plus"></i> Add All Prescribed Medicines to Cart
            </button>
        </div>
    `;
    document.getElementById('viewRxDetailModal').classList.add('active');
}

function closeRxDetailModal() {
    document.getElementById('viewRxDetailModal').classList.remove('active');
}

function printRxDetail(rxId) {
    openRxDetailModal(rxId);
    setTimeout(() => window.print(), 500);
}

async function buyMedicinesFromRx(rxId) {
    const rx = state.doctorPrescriptions.find(r => r.rxId === rxId);
    if (!rx) return;

    for (const item of rx.items) {
        // Try to match medicine ID or add directly
        const matchedMed = state.medicines.find(m => m.medicineName.toLowerCase().includes(item.medicineName.toLowerCase()) || item.medicineName.toLowerCase().includes(m.medicineName.toLowerCase()));
        const medId = matchedMed ? matchedMed.medicineId : (item.medicineId || state.medicines[0].medicineId);
        
        await fetch('/api/cart/items', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ medicineId: medId, quantity: 1 })
        });
    }

    await loadCart();
    openCartModal();
    showNotification('Prescription medicines added to cart!');
}

// Doctor Created Rx (Doctor Workspace)
function openDoctorCreateRxModal() {
    const container = document.getElementById('rxMedicinesContainer');
    container.innerHTML = '';
    addRxItemRow();
    document.getElementById('doctorCreateRxModal').classList.add('active');
}

function closeDoctorCreateRxModal() {
    document.getElementById('doctorCreateRxModal').classList.remove('active');
}

function addRxItemRow() {
    const container = document.getElementById('rxMedicinesContainer');
    const index = container.children.length;
    const div = document.createElement('div');
    div.className = 'rx-item-row';
    div.style.cssText = 'background: #f8fafc; padding: 0.85rem; border-radius: 8px; margin-bottom: 0.75rem; border: 1px solid var(--border-color);';
    div.innerHTML = `
        <div style="display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 0.5rem; margin-bottom: 0.5rem;">
            <input type="text" class="form-control rx-med-name" placeholder="Medicine Name (e.g. Zofran 4mg)" required>
            <input type="text" class="form-control rx-med-dosage" placeholder="Dosage (1 tab)" value="1 tablet">
            <input type="text" class="form-control rx-med-freq" placeholder="Frequency" value="Twice daily">
            <input type="text" class="form-control rx-med-dur" placeholder="Duration" value="5 days">
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center;">
            <input type="text" class="form-control rx-med-inst" placeholder="Instructions (e.g. After food)" value="Take after food" style="flex: 1;">
            <input type="number" class="form-control rx-med-price" placeholder="Price (₹)" value="150" style="width: 110px;">
            <button type="button" class="btn btn-danger" style="padding: 0.4rem 0.6rem;" onclick="this.parentElement.parentElement.remove()">
                <i class="fa-solid fa-trash"></i>
            </button>
        </div>
    `;
    container.appendChild(div);
}

async function submitDoctorCreatedRx(e) {
    e.preventDefault();
    const doctorName = document.getElementById('rxDocName').value;
    const doctorSpecialization = document.getElementById('rxDocSpec').value;
    const patientName = document.getElementById('rxPatientName').value;
    const consultationDate = document.getElementById('rxConsultDate').value;
    const diagnosisNotes = document.getElementById('rxDiagnosisNotes').value;

    const rows = document.querySelectorAll('.rx-item-row');
    const medicines = [];

    rows.forEach(r => {
        medicines.add ? null : null;
        medicines.push({
            medicineName: r.querySelector('.rx-med-name').value,
            dosage: r.querySelector('.rx-med-dosage').value,
            frequency: r.querySelector('.rx-med-freq').value,
            duration: r.querySelector('.rx-med-dur').value,
            instructions: r.querySelector('.rx-med-inst').value,
            timing: 'After food',
            price: parseFloat(r.querySelector('.rx-med-price').value || 150)
        });
    });

    try {
        const res = await fetch('/api/doctor-prescriptions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ doctorName, doctorSpecialization, patientName, consultationDate, diagnosisNotes, medicines })
        });
        if (res.ok) {
            closeDoctorCreateRxModal();
            showNotification('Prescription Created & Sent to Patient!');
            loadMyPrescriptions();
        }
    } catch (err) {
        console.error('Create rx error:', err);
    }
}

// Cart Handling & Checkout
async function loadCart() {
    try {
        const res = await fetch('/api/cart');
        state.cart = await res.json();
        updateCartBadge();
    } catch (err) {
        console.error('Error loading cart:', err);
    }
}

function updateCartBadge() {
    const badge = document.getElementById('cartCountBadge');
    if (!state.cart || !state.cart.cartItems) {
        badge.innerText = '0';
        return;
    }
    const count = state.cart.cartItems.reduce((acc, item) => acc + item.quantity, 0);
    badge.innerText = count;
}

async function addToCart(medicineId) {
    try {
        const res = await fetch('/api/cart/items', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ medicineId: medicineId, quantity: 1 })
        });
        if (res.ok) {
            state.cart = await res.json();
            updateCartBadge();
            showNotification('Item added to shopping cart!');
        } else {
            const err = await res.json();
            alert(err.error || 'Failed to add item to cart');
        }
    } catch (err) {
        console.error('Add to cart error:', err);
    }
}

async function addToCartByDetails(productName, price) {
    // Find matching medicine or fallback to first available catalog item
    const matched = state.medicines.find(m => m.medicineName.toLowerCase().includes(productName.toLowerCase()) || productName.toLowerCase().includes(m.medicineName.toLowerCase()));
    const medId = matched ? matched.medicineId : (state.medicines.length > 0 ? state.medicines[0].medicineId : 1);
    await addToCart(medId);
}

function renderSubstituteMedicinesGrid() {
    const grid = document.getElementById('substituteMedicinesGrid');
    if (!grid) return;
    grid.innerHTML = state.medicines.slice(0, 6).map(med => renderSingleMedicineCard(med)).join('');
}

function renderValueStoreGrid() {
    const grid = document.getElementById('valueStoreGrid');
    if (!grid) return;
    const cheap = state.medicines.filter(m => parseFloat(m.price) < 150);
    grid.innerHTML = (cheap.length > 0 ? cheap : state.medicines.slice(0, 6)).map(med => renderSingleMedicineCard(med)).join('');
}

async function updateCartItemQty(cartItemId, newQty) {
    try {
        const res = await fetch(`/api/cart/items/${cartItemId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: newQty })
        });
        if (res.ok) {
            state.cart = await res.json();
            updateCartBadge();
            renderCartModalContent();
        }
    } catch (err) {
        console.error('Update qty error:', err);
    }
}

function openCartModal() {
    renderCartModalContent();
    document.getElementById('cartModal').classList.add('active');
}

function closeCartModal() {
    document.getElementById('cartModal').classList.remove('active');
}

function renderCartModalContent() {
    const container = document.getElementById('cartItemsContainer');
    const formSection = document.getElementById('checkoutFormSection');

    if (!state.cart || !state.cart.cartItems || state.cart.cartItems.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: var(--text-muted);">
                <i class="fa-solid fa-cart-arrow-down" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                <p>Your shopping cart is currently empty.</p>
            </div>`;
        formSection.style.display = 'none';
        return;
    }

    formSection.style.display = 'block';
    let totalAmount = 0;
    let rxRequiredInCart = false;

    container.innerHTML = state.cart.cartItems.map(item => {
        const itemTotal = parseFloat(item.price) * item.quantity;
        totalAmount += itemTotal;
        if (item.medicine.requiresPrescription) rxRequiredInCart = true;

        return `
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.85rem 0; border-bottom: 1px solid var(--border-color);">
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <img src="${getMedicineImageUrl(item.medicine.imageUrl)}" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover;" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                    <div>
                        <div style="font-weight: 700;">${item.medicine.medicineName} ${item.medicine.requiresPrescription ? '<span style="color: var(--danger); font-size: 0.75rem;">(Rx Required)</span>' : ''}</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted);">${formatCurrency(item.price)} each</div>
                    </div>
                </div>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="display: flex; align-items: center; border: 1px solid var(--border-color); border-radius: 50px; padding: 2px 8px;">
                        <button style="border: none; background: transparent; cursor: pointer; padding: 2px 6px;" onclick="updateCartItemQty(${item.cartItemId}, ${item.quantity - 1})">-</button>
                        <span style="padding: 0 8px; font-weight: 600;">${item.quantity}</span>
                        <button style="border: none; background: transparent; cursor: pointer; padding: 2px 6px;" onclick="updateCartItemQty(${item.cartItemId}, ${item.quantity + 1})">+</button>
                    </div>
                    <div style="font-weight: 700; width: 80px; text-align: right;">${formatCurrency(itemTotal)}</div>
                </div>
            </div>
        `;
    }).join('');

    document.getElementById('cartGrandTotal').innerText = formatCurrency(totalAmount);

    const rxGroup = document.getElementById('rxSelectGroup');
    if (rxRequiredInCart) {
        rxGroup.style.display = 'block';
        renderRxDropdown();
    } else {
        rxGroup.style.display = 'none';
    }
}

async function loadAddresses() {
    try {
        const res = await fetch('/api/orders/addresses');
        state.addresses = await res.json();
        renderAddressDropdown();
    } catch (err) {
        console.error('Error loading addresses:', err);
    }
}

function renderAddressDropdown() {
    const select = document.getElementById('checkoutAddressSelect');
    if (!select) return;
    if (state.addresses.length === 0) {
        select.innerHTML = `<option value="">No address saved. (Demo Address Selected)</option>`;
        return;
    }
    select.innerHTML = state.addresses.map(addr => `
        <option value="${addr.addressId}">${addr.fullName} - ${addr.addressLine1}, ${addr.city} (${addr.pincode})</option>
    `).join('');
}

async function loadPrescriptions() {
    try {
        const res = await fetch('/api/prescriptions/my');
        state.prescriptions = await res.json();
        renderRxDropdown();
    } catch (err) {
        console.error('Error loading prescriptions:', err);
    }
}

function renderRxDropdown() {
    const select = document.getElementById('checkoutRxSelect');
    if (!select) return;
    if (state.prescriptions.length === 0 && state.doctorPrescriptions.length === 0) {
        select.innerHTML = `<option value="">No prescription uploaded yet!</option>`;
        return;
    }
    let html = state.doctorPrescriptions.map(rx => `<option value="${rx.rxId}">Digital Rx #${rx.rxId} - Dr. ${rx.doctorName} [${rx.status}]</option>`).join('');
    html += state.prescriptions.map(rx => `<option value="${rx.prescriptionId}">Uploaded Rx #${rx.prescriptionId} - [${rx.status}]</option>`).join('');
    select.innerHTML = html;
}

async function processCheckout() {
    const addressId = document.getElementById('checkoutAddressSelect').value;
    const rxId = document.getElementById('checkoutRxSelect').value;
    const paymentMethod = document.getElementById('checkoutPaymentMethod').value;

    try {
        const res = await fetch('/api/orders/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                addressId: addressId ? parseInt(addressId) : 1,
                prescriptionId: rxId ? parseInt(rxId) : null,
                paymentMethod: paymentMethod
            })
        });

        if (res.ok) {
            const order = await res.json();
            closeCartModal();
            state.cart = { cartItems: [] };
            updateCartBadge();
            alert(`Order #${order.orderId} placed successfully! Status: ${order.orderStatus}`);
            switchCustomerSubTab('orders');
        } else {
            const err = await res.json();
            alert(err.error || 'Checkout failed.');
        }
    } catch (err) {
        console.error('Checkout error:', err);
    }
}

// Load Customer Orders
async function loadCustomerOrders() {
    try {
        const res = await fetch('/api/orders/my');
        const orders = await res.json();
        const container = document.getElementById('customerOrdersList');
        if (!container) return;

        if (orders.length === 0) {
            container.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-box-open" style="font-size: 3rem; margin-bottom: 1rem;"></i><p>You have not placed any orders yet.</p></div>`;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.25rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 1rem;">
                    <div>
                        <span style="font-weight: 800; font-size: 1.1rem; font-family: 'Outfit', sans-serif;">Order #${order.orderId}</span>
                        <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 0.75rem;">Placed on: ${new Date(order.orderDate).toLocaleString()}</span>
                    </div>
                    <span class="status-pill status-${order.orderStatus}">${order.orderStatus}</span>
                </div>

                <div style="margin-bottom: 1rem;">
                    ${order.orderItems.map(item => `
                        <div style="display: flex; align-items: center; justify-content: space-between; font-size: 0.9rem; margin-bottom: 0.5rem;">
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <img src="${getMedicineImageUrl(item.medicine.imageUrl)}" alt="${item.medicine.medicineName}" style="width: 36px; height: 36px; border-radius: 6px; object-fit: cover;" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                                <span>${item.medicine.medicineName} <strong style="color: var(--text-muted);">x ${item.quantity}</strong></span>
                            </div>
                            <span style="font-weight: 600;">${formatCurrency(item.subtotal)}</span>
                        </div>
                    `).join('')}
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 0.75rem; border-radius: var(--radius-sm);">
                    <span style="font-size: 0.85rem; color: var(--text-muted);">Payment: <strong>${order.payment ? order.payment.paymentMethod : 'CARD'}</strong> (${order.paymentStatus})</span>
                    <div style="font-size: 1.1rem; font-weight: 800; color: var(--primary);">Total: ${formatCurrency(order.totalAmount)}</div>
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Error loading orders:', err);
    }
}

// Upload Prescription Modal
function openRxModal() { document.getElementById('rxModal').classList.add('active'); }
function closeRxModal() { document.getElementById('rxModal').classList.remove('active'); }

async function handleRxUpload(e) {
    e.preventDefault();
    const fileInput = document.getElementById('rxFileInput');
    const notesInput = document.getElementById('rxNotesInput');

    if (!fileInput.files[0]) return;
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('notes', notesInput.value);

    try {
        const res = await fetch('/api/prescriptions/upload', {
            method: 'POST',
            body: formData
        });
        if (res.ok) {
            closeRxModal();
            fileInput.value = '';
            notesInput.value = '';
            showNotification('Prescription uploaded successfully!');
            await loadMyPrescriptions();
        } else {
            const err = await res.json();
            alert(err.error || 'Upload failed');
        }
    } catch (err) {
        console.error('Rx Upload Error:', err);
    }
}

// Pharmacist Workspace
async function loadPharmacistData() {
    loadPendingPrescriptions();
    loadPharmacistOrders();
}

async function loadPendingPrescriptions() {
    try {
        const res = await fetch('/api/prescriptions/all');
        const rxs = await res.json();
        const tbody = document.getElementById('pharmacistRxTableBody');
        if (!tbody) return;

        if (rxs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No prescriptions found.</td></tr>`;
            return;
        }

        tbody.innerHTML = rxs.map(rx => `
            <tr>
                <td><strong>#${rx.prescriptionId}</strong></td>
                <td>${rx.user.fullName} (${rx.user.email})</td>
                <td>${rx.notes || 'No doctor notes'}</td>
                <td>${new Date(rx.uploadedAt).toLocaleString()}</td>
                <td><span class="status-pill status-${rx.status}">${rx.status}</span></td>
                <td>
                    <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="previewImage('${rx.imageUrl}')">
                        <i class="fa-solid fa-eye"></i> View Doc
                    </button>
                </td>
                <td>
                    ${rx.status === 'PENDING' ? `
                        <button class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="updateRxStatus(${rx.prescriptionId}, 'APPROVED')">Approve</button>
                        <button class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="updateRxStatus(${rx.prescriptionId}, 'REJECTED')">Reject</button>
                    ` : '<span style="color: var(--text-muted); font-size: 0.85rem;">Reviewed</span>'}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading rxs:', err);
    }
}

async function updateRxStatus(rxId, status) {
    try {
        const res = await fetch(`/api/prescriptions/${rxId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (res.ok) loadPendingPrescriptions();
    } catch (err) {
        console.error('Update rx status error:', err);
    }
}

async function loadPharmacistOrders() {
    try {
        const res = await fetch('/api/orders/all');
        const orders = await res.json();
        const tbody = document.getElementById('pharmacistOrderTableBody');
        if (!tbody) return;

        tbody.innerHTML = orders.map(order => `
            <tr>
                <td><strong>#${order.orderId}</strong></td>
                <td>${order.user.fullName}</td>
                <td>${formatCurrency(order.totalAmount)}</td>
                <td>${order.prescription ? `<span class="status-pill status-${order.prescription.status}">Rx ${order.prescription.status}</span>` : '<span style="color: var(--text-muted);">OTC</span>'}</td>
                <td><span class="status-pill status-${order.orderStatus}">${order.orderStatus}</span></td>
                <td>
                    <select class="form-control" style="padding: 0.3rem; font-size: 0.85rem;" onchange="updateOrderStatus(${order.orderId}, this.value)">
                        <option value="PLACED" ${order.orderStatus === 'PLACED' ? 'selected' : ''}>PLACED</option>
                        <option value="APPROVED" ${order.orderStatus === 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                        <option value="PROCESSING" ${order.orderStatus === 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                        <option value="SHIPPED" ${order.orderStatus === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                        <option value="DELIVERED" ${order.orderStatus === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                        <option value="CANCELLED" ${order.orderStatus === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                    </select>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading pharmacist orders:', err);
    }
}

async function updateOrderStatus(orderId, status) {
    try {
        await fetch(`/api/orders/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        loadPharmacistOrders();
    } catch (err) {
        console.error('Update order status error:', err);
    }
}

// Admin Dashboard
async function loadAdminData() {
    try {
        const [medsRes, ordersRes, usersRes] = await Promise.all([
            fetch('/api/medicines/admin/all'),
            fetch('/api/orders/all'),
            fetch('/api/auth/users')
        ]);

        const meds = await medsRes.json();
        const orders = await ordersRes.json();
        const users = await usersRes.json();

        const totalSales = orders.reduce((acc, o) => acc + parseFloat(o.totalAmount), 0);
        document.getElementById('kpiTotalSales').innerText = formatCurrency(totalSales);
        document.getElementById('kpiTotalOrders').innerText = orders.length;
        document.getElementById('kpiTotalMedicines').innerText = meds.length;
        document.getElementById('kpiTotalUsers').innerText = users.length;

        const tbody = document.getElementById('adminMedicineTableBody');
        tbody.innerHTML = meds.map(m => `
            <tr>
                <td>#${m.medicineId}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${getMedicineImageUrl(m.imageUrl)}" alt="${m.medicineName}" style="width: 36px; height: 36px; border-radius: 6px; object-fit: cover;" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
                        <strong>${m.medicineName}</strong>
                    </div>
                </td>
                <td>${m.category ? m.category.categoryName : 'N/A'}</td>
                <td>${m.brand}</td>
                <td>${formatCurrency(m.price)}</td>
                <td><span style="font-weight: 700; color: ${m.stockQuantity > 0 ? 'var(--success)' : 'var(--danger)'};">${m.stockQuantity}</span></td>
                <td>${m.requiresPrescription ? 'Yes (Rx)' : 'No (OTC)'}</td>
                <td><span class="status-pill ${m.status === 'ACTIVE' ? 'status-APPROVED' : 'status-CANCELLED'}">${m.status}</span></td>
                <td>
                    <button class="btn btn-secondary" style="padding: 0.25rem 0.5rem; font-size: 0.8rem;" onclick="editMedicine(${m.medicineId})">Edit</button>
                    <button class="btn btn-danger" style="padding: 0.25rem 0.5rem; font-size: 0.8rem;" onclick="deleteMedicine(${m.medicineId})">Inactivate</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading admin dashboard:', err);
    }
}

function openAddMedicineModal() {
    document.getElementById('medEditId').value = '';
    document.getElementById('medicineForm').reset();
    document.getElementById('medicineModalTitle').innerHTML = '<i class="fa-solid fa-capsules"></i> Add New Medicine';
    populateCategorySelect();
    document.getElementById('medicineModal').classList.add('active');
}

function closeMedicineModal() {
    document.getElementById('medicineModal').classList.remove('active');
}

function populateCategorySelect(selectedId = null) {
    const select = document.getElementById('medCategory');
    select.innerHTML = state.categories.map(c => `
        <option value="${c.categoryId}" ${selectedId === c.categoryId ? 'selected' : ''}>${c.categoryName}</option>
    `).join('');
}

async function editMedicine(id) {
    const med = state.medicines.find(m => m.medicineId === id);
    if (!med) return;

    document.getElementById('medEditId').value = med.medicineId;
    document.getElementById('medName').value = med.medicineName;
    document.getElementById('medGeneric').value = med.genericName || '';
    document.getElementById('medBrand').value = med.brand || '';
    document.getElementById('medManufacturer').value = med.manufacturer || '';
    document.getElementById('medLocation').value = med.location || '';
    document.getElementById('medPrice').value = med.price;
    document.getElementById('medDiscount').value = med.discount;
    document.getElementById('medStock').value = med.stockQuantity;
    document.getElementById('medImage').value = med.imageUrl;
    document.getElementById('medDescription').value = med.description;
    document.getElementById('medRequiresRx').checked = med.requiresPrescription;

    populateCategorySelect(med.category.categoryId);
    document.getElementById('medicineModalTitle').innerText = 'Edit Medicine #' + med.medicineId;
    document.getElementById('medicineModal').classList.add('active');
}

async function saveMedicine(e) {
    e.preventDefault();
    const id = document.getElementById('medEditId').value;
    const body = {
        medicineName: document.getElementById('medName').value,
        genericName: document.getElementById('medGeneric').value,
        brand: document.getElementById('medBrand').value,
        manufacturer: document.getElementById('medManufacturer').value,
        location: document.getElementById('medLocation').value,
        categoryId: parseInt(document.getElementById('medCategory').value),
        price: parseFloat(document.getElementById('medPrice').value),
        discount: parseFloat(document.getElementById('medDiscount').value || 0),
        stockQuantity: parseInt(document.getElementById('medStock').value),
        imageUrl: document.getElementById('medImage').value,
        description: document.getElementById('medDescription').value,
        requiresPrescription: document.getElementById('medRequiresRx').checked
    };

    const url = id ? `/api/medicines/${id}` : '/api/medicines';
    const method = id ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            closeMedicineModal();
            loadMedicines();
            loadAdminData();
        }
    } catch (err) {
        console.error('Save medicine error:', err);
    }
}

async function deleteMedicine(id) {
    if (!confirm('Are you sure you want to inactivate this medicine?')) return;
    try {
        const res = await fetch(`/api/medicines/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadMedicines();
            loadAdminData();
        }
    } catch (err) {
        console.error('Delete medicine error:', err);
    }
}

function openProductDetailModal(medicineId) {
    const med = state.medicines.find(m => m.medicineId === medicineId);
    if (!med) return;

    const hasDiscount = med.discount && parseFloat(med.discount) > 0;
    const discountPrice = hasDiscount 
        ? (parseFloat(med.price) * (1 - parseFloat(med.discount) / 100)).toFixed(2)
        : parseFloat(med.price).toFixed(2);

    const imgUrl = getMedicineImageUrl(med.imageUrl);

    document.getElementById('productDetailModalBody').innerHTML = `
        <div style="display: flex; gap: 1.5rem; flex-wrap: wrap; align-items: flex-start;">
            <div style="flex: 1; min-width: 220px; text-align: center; background: #f8fafc; padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                <img src="${imgUrl}" alt="${med.medicineName}" style="width: 100%; max-height: 240px; object-fit: contain; border-radius: var(--radius-sm);" onerror="this.onerror=null; this.src='/images/placeholder-medicine.svg';">
            </div>
            <div style="flex: 1.2; min-width: 240px;">
                <div style="font-size: 0.8rem; color: var(--primary); font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.25rem;">
                    ${med.brand || 'Pharma'} • ${med.category ? med.category.categoryName : 'General'}
                </div>
                <h2 style="font-size: 1.4rem; font-family: 'Outfit', sans-serif; margin-bottom: 0.25rem; color: var(--text-dark);">${med.medicineName}</h2>
                ${med.genericName ? `<div style="font-size: 0.88rem; color: var(--text-muted); font-style: italic; margin-bottom: 0.4rem;">Generic: ${med.genericName}</div>` : ''}
                ${med.manufacturer ? `<div style="font-size: 0.82rem; color: var(--text-muted);">Manufacturer: <strong>${med.manufacturer}</strong></div>` : ''}
                ${med.location ? `<div style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 0.75rem;"><i class="fa-solid fa-location-dot"></i> ${med.location}</div>` : ''}

                <div style="margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem;">
                    ${med.requiresPrescription ? '<span class="rx-badge"><i class="fa-solid fa-file-prescription"></i> Rx Required</span>' : '<span class="otc-badge"><i class="fa-solid fa-check"></i> OTC</span>'}
                    ${hasDiscount ? `<span class="discount-tag" style="position: static;">-${parseInt(med.discount)}% OFF</span>` : ''}
                </div>

                <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5; margin-bottom: 1rem;">${med.description || 'Quality formulation for healthcare.'}</p>
                <div style="font-size: 0.85rem; color: ${med.stockQuantity > 0 ? 'var(--success)' : 'var(--danger)'}; font-weight: 600; margin-bottom: 1.25rem;">
                    ${med.stockQuantity > 0 ? `In Stock (${med.stockQuantity} items available)` : 'Currently Out of Stock'}
                </div>

                <div style="display: flex; align-items: center; justify-content: space-between; background: #f8fafc; padding: 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
                    <div>
                        <div style="font-size: 1.4rem; font-weight: 800; color: var(--text-dark); font-family: 'Outfit', sans-serif;">${formatCurrency(discountPrice)}</div>
                        ${hasDiscount ? `<div style="text-decoration: line-through; color: var(--text-muted); font-size: 0.85rem;">${formatCurrency(med.price)}</div>` : ''}
                    </div>
                    <button class="btn btn-primary" ${med.stockQuantity <= 0 ? 'disabled style="opacity: 0.5;"' : ''} onclick="addToCart(${med.medicineId}); closeProductDetailModal();">
                        <i class="fa-solid fa-cart-plus"></i> Add to Cart
                    </button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('productDetailModal').classList.add('active');
}

function closeProductDetailModal() {
    document.getElementById('productDetailModal').classList.remove('active');
}

function previewImage(url) {
    document.getElementById('previewImg').src = url;
    document.getElementById('imagePreviewModal').classList.add('active');
}

function closeImagePreviewModal() {
    document.getElementById('imagePreviewModal').classList.remove('active');
}

function showNotification(msg) {
    const toast = document.createElement('div');
    toast.innerText = msg;
    toast.style.cssText = `
        position: fixed; bottom: 20px; right: 20px;
        background: #059669; color: white; padding: 12px 24px;
        border-radius: 50px; font-weight: 600; font-size: 0.9rem;
        box-shadow: 0 10px 25px rgba(0,0,0,0.15); z-index: 2000;
        animation: fadeIn 0.3s ease;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}
