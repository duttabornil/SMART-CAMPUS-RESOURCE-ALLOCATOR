document.addEventListener('DOMContentLoaded', () => {
    // --- State & DOM Elements ---
    let currentPanelId = 'view-bookings';
    const navItems = document.querySelectorAll('.nav-item');
    const panels = document.querySelectorAll('.panel');
    const themeToggleBtn = document.getElementById('theme-toggle');
    const body = document.body;
    let isDarkMode = false;

    // Password Modal
    const modal = document.getElementById('password-modal');
    const adminPasswordInput = document.getElementById('admin-password');
    const modalCancel = document.getElementById('modal-cancel');
    const modalSubmit = document.getElementById('modal-submit');
    let pendingTarget = null;

    // Toast
    const toast = document.getElementById('toast');

    // --- Navigation ---
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = item.getAttribute('data-target');
            
            if (targetId === 'approve-bookings') {
                pendingTarget = item;
                modal.classList.add('active');
                adminPasswordInput.value = '';
                adminPasswordInput.focus();
                return;
            }

            switchPanel(item, targetId);
        });
    });

    function switchPanel(navItem, targetId) {
        navItems.forEach(nav => nav.classList.remove('active'));
        navItem.classList.add('active');

        panels.forEach(panel => panel.classList.remove('active'));
        document.getElementById(targetId).classList.add('active');
        currentPanelId = targetId;

        // Trigger load data based on panel
        if (targetId === 'view-bookings') {
            loadBookings();
        }
    }

    // Modal Events
    modalCancel.addEventListener('click', () => {
        modal.classList.remove('active');
        pendingTarget = null;
    });

    modalSubmit.addEventListener('click', () => {
        if (adminPasswordInput.value === 'root') {
            modal.classList.remove('active');
            if (pendingTarget) {
                switchPanel(pendingTarget, pendingTarget.getAttribute('data-target'));
                pendingTarget = null;
            }
        } else {
            showToast('Incorrect password!', 'error');
            adminPasswordInput.value = '';
        }
    });

    // Theme Toggle
    themeToggleBtn.addEventListener('click', () => {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            body.classList.remove('light-mode');
            body.classList.add('dark-mode');
        } else {
            body.classList.remove('dark-mode');
            body.classList.add('light-mode');
        }
    });

    // --- Utility Functions ---
    function showToast(message, type = 'success') {
        toast.textContent = message;
        toast.className = `toast show ${type}`;
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }

    async function fetchApi(url, options = {}) {
        try {
            const response = await fetch(url, options);
            const data = await response.json().catch(() => ({}));
            
            if (!response.ok) {
                throw new Error(data.error || 'API request failed');
            }
            return data;
        } catch (error) {
            showToast(error.message, 'error');
            throw error;
        }
    }

    // --- View Bookings ---
    const refreshBookingsBtn = document.getElementById('refresh-bookings');
    const bookingsTableBody = document.querySelector('#bookings-table tbody');

    async function loadBookings() {
        try {
            const bookings = await fetchApi('/api/bookings');
            bookingsTableBody.innerHTML = '';
            
            bookings.forEach(b => {
                const tr = document.createElement('tr');
                
                let statusClass = 'status-pending';
                if (b.Status.toLowerCase() === 'approved') statusClass = 'status-approved';
                else if (b.Status.toLowerCase() === 'rejected') statusClass = 'status-rejected';

                tr.innerHTML = `
                    <td>${b.Booking_ID}</td>
                    <td>${b.User_ID}</td>
                    <td>${b.User_Name}</td>
                    <td>${b.Resource_Name}</td>
                    <td>${b.Booking_Date}</td>
                    <td>${b.Start_Time}</td>
                    <td>${b.End_Time}</td>
                    <td><span class="status-badge ${statusClass}">${b.Status}</span></td>
                `;
                bookingsTableBody.appendChild(tr);
            });
        } catch (error) {
            console.error(error);
        }
    }

    refreshBookingsBtn.addEventListener('click', loadBookings);

    // --- Book Resource ---
    const bookForm = document.getElementById('book-form');
    const findResourcesBtn = document.getElementById('find-resources');
    const resourceSelect = document.getElementById('book-resource-select');

    findResourcesBtn.addEventListener('click', async () => {
        const date = document.getElementById('book-date').value;
        const start = document.getElementById('book-start').value;
        const end = document.getElementById('book-end').value;

        if (!date || !start || !end) {
            showToast('Please fill out Date, Start Time, and End Time first.', 'error');
            return;
        }

        try {
            // Append seconds if missing
            const sTime = start.length === 5 ? `${start}:00` : start;
            const eTime = end.length === 5 ? `${end}:00` : end;

            const resources = await fetchApi(`/api/resources/available?date=${date}&startTime=${sTime}&endTime=${eTime}`);
            resourceSelect.innerHTML = '<option value="" disabled selected>Select a resource...</option>';
            
            if (resources.length === 0) {
                showToast('No resources available for the selected time.', 'error');
            } else {
                resources.forEach(r => {
                    const opt = document.createElement('option');
                    opt.value = r.Resource_ID;
                    opt.textContent = `${r.Resource_ID} - ${r.Resource_Name}`;
                    resourceSelect.appendChild(opt);
                });
                showToast('Available resources found!', 'success');
            }
        } catch (error) {
            console.error(error);
        }
    });

    bookForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const date = document.getElementById('book-date').value;
        const start = document.getElementById('book-start').value;
        const end = document.getElementById('book-end').value;
        const resourceId = resourceSelect.value;
        const purpose = document.getElementById('book-purpose').value;
        const userId = document.getElementById('book-user-id').value;

        if (!resourceId) {
            showToast('Please find and select an available resource first.', 'error');
            return;
        }

        const sTime = start.length === 5 ? `${start}:00` : start;
        const eTime = end.length === 5 ? `${end}:00` : end;

        try {
            const data = await fetchApi('/api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ date, startTime: sTime, endTime: eTime, resourceId, purpose, userId })
            });
            showToast(data.message, 'success');
            bookForm.reset();
            resourceSelect.innerHTML = '<option value="" disabled selected>Select a resource...</option>';
        } catch (error) {}
    });

    // --- Add User ---
    const userForm = document.getElementById('user-form');
    userForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            id: document.getElementById('user-id').value,
            name: document.getElementById('user-name').value,
            email: document.getElementById('user-email').value,
            role: document.getElementById('user-role').value,
            deptId: document.getElementById('user-dept').value
        };

        try {
            const data = await fetchApi('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showToast(data.message, 'success');
            userForm.reset();
        } catch (error) {}
    });

    // --- Add Resource ---
    const resourceForm = document.getElementById('resource-form');
    resourceForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            id: document.getElementById('res-id').value,
            name: document.getElementById('res-name').value,
            type: document.getElementById('res-type').value,
            location: document.getElementById('res-location').value,
            capacity: document.getElementById('res-capacity').value,
            deptId: document.getElementById('res-dept').value
        };

        try {
            const data = await fetchApi('/api/resources', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showToast(data.message, 'success');
            resourceForm.reset();
        } catch (error) {}
    });

    // --- Add Department ---
    const deptForm = document.getElementById('dept-form');
    deptForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            id: document.getElementById('dept-id').value,
            name: document.getElementById('dept-name').value,
            block: document.getElementById('dept-block').value
        };

        try {
            const data = await fetchApi('/api/departments', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showToast(data.message, 'success');
            deptForm.reset();
        } catch (error) {}
    });

    // --- Approve Bookings ---
    const approveForm = document.getElementById('approve-form');
    approveForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('approve-booking-id').value;
        const status = document.getElementById('approve-status').value;

        try {
            const data = await fetchApi(`/api/bookings/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status })
            });
            showToast(data.message, 'success');
            approveForm.reset();
        } catch (error) {}
    });

    // --- Add Usage Log ---
    const usageLogForm = document.getElementById('usage-log-form');
    usageLogForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const start = document.getElementById('log-start').value;
        const end = document.getElementById('log-end').value;

        const sTime = start.length === 5 ? `${start}:00` : start;
        const eTime = end.length === 5 ? `${end}:00` : end;

        const payload = {
            logId: document.getElementById('log-id').value,
            bookingId: document.getElementById('log-booking-id').value,
            startTime: sTime,
            endTime: eTime,
            duration: document.getElementById('log-duration').value
        };

        try {
            const data = await fetchApi('/api/usage-logs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showToast(data.message, 'success');
            usageLogForm.reset();
        } catch (error) {}
    });

    // Initial load
    loadBookings();
});
