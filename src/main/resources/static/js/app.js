/* MediPulse Online Pharmacy Application Logic */

let state = {
    currentRole: 'CUSTOMER',
    currentUser: null,
    activeCategory: null,
    medicines: [],
    categories: [],
    cart: null,
    orders: [],
    prescriptions: [],
    addresses: []
};

let supabaseClient = null;

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
    await loadCurrentUser();
    await loadCategories();
    await loadMedicines();
    await loadCart();
    await loadAddresses();
    await loadPrescriptions();
    await initSupabaseRealtime();
});

// Fetch config and setup Supabase subscriptions
async function initSupabaseRealtime() {
    try {
        const res = await fetch('/api/config/supabase');
        const config = await res.json();
        if (config.url && config.anonKey && 
            !config.url.includes("your-project") && 
            !config.anonKey.includes("your-anon-key")) {
            
            console.log('Initializing Supabase client with URL:', config.url);
            supabaseClient = supabase.createClient(config.url, config.anonKey);

            // 1. Listen to orders table changes
            supabaseClient
                .channel('public:orders')
                .on('postgres_changes', { event: '*', schema: 'public', table: 'orders' }, payload => {
                    console.log('Realtime Order Change detected:', payload);
                    if (state.currentRole === 'CUSTOMER') {
                        loadCustomerOrders();
                    } else if (state.currentRole === 'PHARMACIST') {
                        loadPharmacistOrders();
                    } else if (state.currentRole === 'ADMIN') {
                        loadAdminData();
                    }
                })
                .subscribe();

            // 2. Listen to prescriptions table changes
            supabaseClient
                .channel('public:prescriptions')
                .on('postgres_changes', { event: '*', schema: 'public', table: 'prescriptions' }, payload => {
                    console.log('Realtime Prescription Change detected:', payload);
                    loadPrescriptions();
                    if (state.currentRole === 'PHARMACIST') {
                        loadPendingPrescriptions();
                    }
                })
                .subscribe();

            // 3. Listen to medicines table changes
            supabaseClient
                .channel('public:medicines')
                .on('postgres_changes', { event: '*', schema: 'public', table: 'medicines' }, payload => {
                    console.log('Realtime Medicine Change detected:', payload);
                    loadMedicines();
                    if (state.currentRole === 'ADMIN') {
                        loadAdminData();
                    }
                })
                .subscribe();
                
            console.log('Supabase Realtime subscriptions established successfully.');
        } else {
            console.warn('Supabase Realtime credentials are not fully configured or are placeholder values.');
        }
    } catch (err) {
        console.error('Failed to initialize Supabase Realtime:', err);
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
        
        // Update nav display
        document.getElementById('navUserEmail').innerText = state.currentUser.fullName || state.currentUser.email;
        
        // Update role buttons and view display on initial load
        const role = state.currentRole;
        document.querySelectorAll('.role-btn').forEach(btn => btn.classList.remove('active'));
        if (role === 'CUSTOMER') document.getElementById('roleCustomerBtn').classList.add('active');
        if (role === 'PHARMACIST') document.getElementById('rolePharmacistBtn').classList.add('active');
        if (role === 'ADMIN') document.getElementById('roleAdminBtn').classList.add('active');
        
        document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');
        if (role === 'CUSTOMER') {
            document.getElementById('customerView').style.display = 'block';
        } else if (role === 'PHARMACIST') {
            document.getElementById('pharmacistView').style.display = 'block';
            loadPharmacistData();
        } else if (role === 'ADMIN') {
            document.getElementById('adminView').style.display = 'block';
            loadAdminData();
        }
    } catch (err) {
        console.error('Error fetching current user:', err);
    }
}

// Logout User
async function logoutUser() {
    try {
        const res = await fetch('/api/auth/logout', { method: 'POST' });
        if (res.ok) {
            window.location.href = '/login';
        }
    } catch (err) {
        console.error('Logout failed:', err);
    }
}

// Role Switching Logic
async function switchUserRole(role) {
    state.currentRole = role;

    // Update active button UI
    document.querySelectorAll('.role-btn').forEach(btn => btn.classList.remove('active'));
    if (role === 'CUSTOMER') document.getElementById('roleCustomerBtn').classList.add('active');
    if (role === 'PHARMACIST') document.getElementById('rolePharmacistBtn').classList.add('active');
    if (role === 'ADMIN') document.getElementById('roleAdminBtn').classList.add('active');

    // Switch section views
    document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');

    try {
        await fetch(`/api/auth/switch-role/${role}`, { method: 'POST' });
        const res = await fetch('/api/auth/current');
        state.currentUser = await res.json();
        document.getElementById('navUserEmail').innerText = state.currentUser.fullName || state.currentUser.email;
    } catch (err) {
        console.error('Role switch failed', err);
    }

    if (role === 'CUSTOMER') {
        document.getElementById('customerView').style.display = 'block';
        loadMedicines();
    } else if (role === 'PHARMACIST') {
        document.getElementById('pharmacistView').style.display = 'block';
        loadPharmacistData();
    } else if (role === 'ADMIN') {
        document.getElementById('adminView').style.display = 'block';
        loadAdminData();
    }
}

// Sub-Tab Switcher for Customer
function switchCustomerSubTab(tab) {
    if (tab === 'catalog') {
        document.getElementById('subTabCatalog').style.display = 'block';
        document.getElementById('subTabOrders').style.display = 'none';
        document.getElementById('tabCatalogBtn').style.fontWeight = '700';
        document.getElementById('tabOrdersBtn').style.fontWeight = '500';
    } else {
        document.getElementById('subTabCatalog').style.display = 'none';
        document.getElementById('subTabOrders').style.display = 'block';
        document.getElementById('tabCatalogBtn').style.fontWeight = '500';
        document.getElementById('tabOrdersBtn').style.fontWeight = '700';
        loadCustomerOrders();
    }
}

// Load Categories
async function loadCategories() {
    try {
        const res = await fetch('/api/medicines/categories');
        state.categories = await res.json();
        renderCategoryPills();
    } catch (err) {
        console.error('Error loading categories:', err);
    }
}

function renderCategoryPills() {
    const list = document.getElementById('categoryPillList');
    list.innerHTML = `<li class="cat-pill active" onclick="filterByCategory(null, this)">All Medicines</li>`;
    state.categories.forEach(cat => {
        list.innerHTML += `<li class="cat-pill" onclick="filterByCategory(${cat.categoryId}, this)">${cat.categoryName}</li>`;
    });
}

function filterByCategory(catId, pillElement) {
    state.activeCategory = catId;
    document.querySelectorAll('.cat-pill').forEach(p => p.classList.remove('active'));
    pillElement.classList.add('active');
    loadMedicines();
}

function handleSearch(event) {
    const query = event.target.value;
    if (event.key === 'Enter' || query.length > 2 || query.length === 0) {
        loadMedicines(query);
    }
}

// Load Medicines
async function loadMedicines(searchQuery = '') {
    try {
        let url = '/api/medicines';
        const params = new URLSearchParams();
        if (state.activeCategory) params.append('categoryId', state.activeCategory);
        if (searchQuery) params.append('search', searchQuery);
        if (params.toString()) url += '?' + params.toString();

        const res = await fetch(url);
        state.medicines = await res.json();
        renderMedicineCards();
    } catch (err) {
        console.error('Error loading medicines:', err);
    }
}

function renderMedicineCards() {
    const grid = document.getElementById('medicineGrid');
    const countLabel = document.getElementById('catalogCount');
    countLabel.innerText = `Showing ${state.medicines.length} items`;

    if (state.medicines.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-muted);">
                <i class="fa-solid fa-box-open" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                <p>No medicines found matching your criteria.</p>
            </div>`;
        return;
    }

    grid.innerHTML = state.medicines.map(med => {
        const hasDiscount = med.discount && parseFloat(med.discount) > 0;
        const discountPrice = hasDiscount 
            ? (parseFloat(med.price) * (1 - parseFloat(med.discount) / 100)).toFixed(2)
            : parseFloat(med.price).toFixed(2);

        return `
            <div class="medicine-card">
                <div class="card-image-wrap">
                    <img src="${med.imageUrl || 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60'}" alt="${med.medicineName}">
                    ${med.requiresPrescription ? '<span class="rx-badge"><i class="fa-solid fa-file-prescription"></i> Rx Required</span>' : '<span class="otc-badge"><i class="fa-solid fa-check"></i> OTC</span>'}
                    ${hasDiscount ? `<span class="discount-tag">-${parseInt(med.discount)}% OFF</span>` : ''}
                </div>
                <div class="card-body">
                    <div class="medicine-brand">${med.brand || 'Pharma'}</div>
                    <div class="medicine-title">${med.medicineName}</div>
                    <div class="medicine-desc">${med.description || 'Quality formulation for health care.'}</div>
                    <div style="font-size: 0.78rem; color: ${med.stockQuantity > 0 ? 'var(--success)' : 'var(--danger)'}; margin-bottom: 0.5rem; font-weight: 600;">
                        ${med.stockQuantity > 0 ? `In Stock (${med.stockQuantity} available)` : 'Out of Stock'}
                    </div>
                    <div class="card-footer-row">
                        <div class="price-box">
                            <span class="current-price">$${discountPrice}</span>
                            ${hasDiscount ? `<span class="original-price">$${parseFloat(med.price).toFixed(2)}</span>` : ''}
                        </div>
                        <button class="btn btn-primary" ${med.stockQuantity <= 0 ? 'disabled style="opacity: 0.5;"' : ''} onclick="addToCart(${med.medicineId})">
                            <i class="fa-solid fa-cart-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Cart Handling
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

// Cart Modal Open/Close
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
                    <img src="${item.medicine.imageUrl || 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60'}" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover;">
                    <div>
                        <div style="font-weight: 700;">${item.medicine.medicineName} ${item.medicine.requiresPrescription ? '<span style="color: var(--danger); font-size: 0.75rem;">(Rx Required)</span>' : ''}</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted);">$${parseFloat(item.price).toFixed(2)} each</div>
                    </div>
                </div>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="display: flex; align-items: center; border: 1px solid var(--border-color); border-radius: 50px; padding: 2px 8px;">
                        <button style="border: none; background: transparent; cursor: pointer; padding: 2px 6px;" onclick="updateCartItemQty(${item.cartItemId}, ${item.quantity - 1})">-</button>
                        <span style="padding: 0 8px; font-weight: 600;">${item.quantity}</span>
                        <button style="border: none; background: transparent; cursor: pointer; padding: 2px 6px;" onclick="updateCartItemQty(${item.cartItemId}, ${item.quantity + 1})">+</button>
                    </div>
                    <div style="font-weight: 700; width: 70px; text-align: right;">$${itemTotal.toFixed(2)}</div>
                </div>
            </div>
        `;
    }).join('');

    document.getElementById('cartGrandTotal').innerText = `$${totalAmount.toFixed(2)}`;

    // Show Rx select if needed
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
    if (state.prescriptions.length === 0) {
        select.innerHTML = `<option value="">No prescription uploaded yet! Please upload Rx first.</option>`;
        return;
    }
    select.innerHTML = state.prescriptions.map(rx => `
        <option value="${rx.prescriptionId}">Rx #${rx.prescriptionId} - Status: [${rx.status}] (${rx.notes || 'Uploaded'})</option>
    `).join('');
}

// Checkout Execution
async function processCheckout() {
    const addressId = document.getElementById('checkoutAddressSelect').value;
    const rxId = document.getElementById('checkoutRxSelect').value;
    const paymentMethod = document.getElementById('checkoutPaymentMethod').value;

    if (!addressId) {
        alert('Please select a delivery address');
        return;
    }

    try {
        const res = await fetch('/api/orders/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                addressId: parseInt(addressId),
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

        if (orders.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; color: var(--text-muted);">
                    <i class="fa-solid fa-box-open" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                    <p>You have not placed any orders yet.</p>
                </div>`;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div style="background: white; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.25rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 1rem;">
                    <div>
                        <span style="font-weight: 800; font-size: 1.1rem; font-family: 'Outfit', sans-serif;">Order #${order.orderId}</span>
                        <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 0.75rem;">Placed on: ${new Date(order.orderDate).toLocaleString()}</span>
                    </div>
                    <div>
                        <span class="status-pill status-${order.orderStatus}">${order.orderStatus}</span>
                    </div>
                </div>

                <div style="margin-bottom: 1rem;">
                    ${order.orderItems.map(item => `
                        <div style="display: flex; justify-content: space-between; font-size: 0.9rem; margin-bottom: 0.4rem;">
                            <span>${item.medicine.medicineName} x ${item.quantity}</span>
                            <span style="font-weight: 600;">$${parseFloat(item.subtotal).toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 0.75rem; border-radius: var(--radius-sm);">
                    <div>
                        <span style="font-size: 0.85rem; color: var(--text-muted);">Payment: <strong>${order.payment ? order.payment.paymentMethod : 'CARD'}</strong> (${order.paymentStatus})</span>
                    </div>
                    <div style="font-size: 1.1rem; font-weight: 800; color: var(--primary);">
                        Total: $${parseFloat(order.totalAmount).toFixed(2)}
                    </div>
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
            alert('Prescription uploaded successfully! Sent to Pharmacist for approval.');
            await loadPrescriptions();
        } else {
            const err = await res.json();
            alert(err.error || 'Upload failed');
        }
    } catch (err) {
        console.error('Rx Upload Error:', err);
    }
}

// ================= PHARMACIST WORKSPACE =================
async function loadPharmacistData() {
    loadPendingPrescriptions();
    loadPharmacistOrders();
}

async function loadPendingPrescriptions() {
    try {
        const res = await fetch('/api/prescriptions/all');
        const rxs = await res.json();
        const tbody = document.getElementById('pharmacistRxTableBody');

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
        console.error('Error loading pharmacist rxs:', err);
    }
}

async function updateRxStatus(rxId, status) {
    try {
        const res = await fetch(`/api/prescriptions/${rxId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: status })
        });
        if (res.ok) {
            loadPendingPrescriptions();
        }
    } catch (err) {
        console.error('Update rx status error:', err);
    }
}

async function loadPharmacistOrders() {
    try {
        const res = await fetch('/api/orders/all');
        const orders = await res.json();
        const tbody = document.getElementById('pharmacistOrderTableBody');

        tbody.innerHTML = orders.map(order => `
            <tr>
                <td><strong>#${order.orderId}</strong></td>
                <td>${order.user.fullName}</td>
                <td>$${parseFloat(order.totalAmount).toFixed(2)}</td>
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
            body: JSON.stringify({ status: status })
        });
        loadPharmacistOrders();
    } catch (err) {
        console.error('Update order status error:', err);
    }
}

// ================= ADMIN DASHBOARD =================
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

        // KPIs
        const totalSales = orders.reduce((acc, o) => acc + parseFloat(o.totalAmount), 0);
        document.getElementById('kpiTotalSales').innerText = `$${totalSales.toFixed(2)}`;
        document.getElementById('kpiTotalOrders').innerText = orders.length;
        document.getElementById('kpiTotalMedicines').innerText = meds.length;
        document.getElementById('kpiTotalUsers').innerText = users.length;

        // Render Inventory Table
        const tbody = document.getElementById('adminMedicineTableBody');
        tbody.innerHTML = meds.map(m => `
            <tr>
                <td>#${m.medicineId}</td>
                <td><strong>${m.medicineName}</strong></td>
                <td>${m.category.categoryName}</td>
                <td>${m.brand}</td>
                <td>$${parseFloat(m.price).toFixed(2)}</td>
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
    document.getElementById('medBrand').value = med.brand;
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
        categoryId: parseInt(document.getElementById('medCategory').value),
        brand: document.getElementById('medBrand').value,
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
        } else {
            const err = await res.json();
            alert(err.error || 'Failed to save medicine');
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

function previewImage(url) {
    document.getElementById('previewImg').src = url;
    document.getElementById('imagePreviewModal').classList.add('active');
}

function closeImagePreviewModal() {
    document.getElementById('imagePreviewModal').classList.remove('active');
}

function showNotification(msg) {
    // Quick notification toast
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
