// ===================================
// COMBINED: MANAGE STAFF, MOVIES & VOUCHERS
// ===================================

// Display current year in footer
document.getElementById("year").textContent = new Date().getFullYear();

// ==========================
// 🧩 UTILITY FUNCTIONS
// ==========================

// Format date for display (YYYY-MM-DD -> DD/MM/YYYY)
function formatDateToDisplay(dateString) {
    if (!dateString || dateString === 'N/A') return 'N/A';
    if (dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
        const parts = dateString.split('-');
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateString;
}

// Generate status badge
function getStatusBadge(status) {
    let statusClass = '';
    let statusText = status;

    if (status === 'Active') {
        statusClass = 'status-active';
        // Check if we are in the staff table context to display 'Active (Working)'
        statusText = document.getElementById('staffTableBody') ? 'Active (Working)' : 'Active';
    } else if (status === 'Inactive') {
        statusClass = 'status-inactive';
    } else if (status === 'Used') {
        statusClass = 'status-inactive'; // Use inactive style for used vouchers
        statusText = 'Used';
    } else if (status === 'Available') {
        statusClass = 'status-active'; // Use active style for available vouchers
        statusText = 'Available';
    }

    return `<span class="status-badge ${statusClass}">${statusText}</span>`;
}

// Get next ID for table
function getNextId(tableBodyId) {
    const tableBody = document.getElementById(tableBodyId);
    if (!tableBody) return 1;
    const rows = tableBody.querySelectorAll('tr');
    let maxId = 0;
    rows.forEach(row => {
        let id = 0;
        // Check based on the table
        if (tableBodyId === 'voucherTableBody' && row.cells.length > 0) {
            // For vouchers, ID is in the first cell (index 0)
            id = parseInt(row.cells[0].textContent) || 0;
        } else if (row.dataset.id) {
            // For staff and movies, ID is stored in data-id attribute
            id = parseInt(row.dataset.id) || 0;
        }
        if (id > maxId) maxId = id;
    });
    return maxId + 1;
}

// Store target row for deletion (Staff and Movie use different modals, Voucher does not)
let deleteTargetRowStaff = null;
let deleteTargetRowMovie = null;


// ===================================
// I. STAFF MANAGEMENT
// ===================================

function performStaffSearch() {
    const tableBody = document.getElementById('staffTableBody');
    if (!tableBody) return;

    const query = document.getElementById('searchInput').value.toLowerCase().trim();
    const rows = tableBody.querySelectorAll('tr');

    rows.forEach(row => {
        // --- FIX: Access data directly from the table cells for reliable search ---
        // Indices based on the staff table: 1 (ID), 2 (Name), 3 (Position)
        const id = row.cells[1] ? row.cells[1].textContent.toLowerCase() : '';
        const name = row.cells[2] ? row.cells[2].textContent.toLowerCase() : '';
        const position = row.cells[3] ? row.cells[3].textContent.toLowerCase() : '';
        // --- END FIX ---

        row.style.display = (id.includes(query) || name.includes(query) || position.includes(query)) ? '' : 'none';
    });
}

if (document.getElementById('staffTableBody')) {
    // Check if the Staff table body exists before attaching listeners

    // --- Select All & Bulk Delete ---
    document.getElementById('selectAll').addEventListener('change', function () {
        const checkboxes = document.querySelectorAll('#staffTableBody .row-checkbox');
        checkboxes.forEach(cb => cb.checked = this.checked);
    });

    document.getElementById('deleteSelectedBtn').addEventListener('click', function () {
        const selected = Array.from(document.querySelectorAll('#staffTableBody .row-checkbox:checked'));
        if (selected.length === 0) {
            // Use a custom alert/toast instead of the native alert
            console.warn("⚠️ Please select at least 1 staff member to delete!");
            // In a real application, you would show a custom modal/toast here.
            return;
        }
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteStaffModal'));
        document.getElementById('deleteStaffModalLabel').textContent = "Confirm Bulk Delete";
        document.getElementById('deleteModalBodyText').textContent = `Are you sure you want to delete ${selected.length} selected staff members?`;
        document.getElementById('confirmDeleteBtn').dataset.mode = 'bulk';
        deleteModal.show();
    });

    // --- Add Staff ---
    document.getElementById('addStaffForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const name = document.getElementById('addName').value;
        const position = document.getElementById('addPosition').value;
        const email = document.getElementById('addEmail').value;
        const phone = document.getElementById('addPhone').value;
        const dob = document.getElementById('addDoB').value;
        const sex = document.getElementById('addSex').value;
        const status = document.getElementById('addStatus').value;

        if (!name || !position || !email || !phone || !sex || !status || !dob) {
            console.error("⚠️ Please fill in all required fields!");
            // Show custom error toast/message
            return;
        }

        const tableBody = document.getElementById('staffTableBody');
        const newId = getNextId('staffTableBody');
        const formattedId = String(newId).padStart(2, '0');

        const newRow = document.createElement('tr');
        // Store all data in data-attributes for easy editing
        newRow.dataset.id = newId;
        newRow.dataset.name = name;
        newRow.dataset.position = position;
        newRow.dataset.email = email;
        newRow.dataset.phone = phone;
        newRow.dataset.sex = sex;
        newRow.dataset.dob = dob;
        newRow.dataset.status = status;

        newRow.innerHTML = `
            <td><input type="checkbox" class="row-checkbox"></td>
            <td>${formattedId}</td>
            <td>${name}</td>
            <td>${position}</td>
            <td>${sex}</td>
            <td>${formatDateToDisplay(dob)}</td>
            <td>${email}</td>
            <td>${phone}</td>
            <td>${getStatusBadge(status)}</td>
            <td>
                <div class="btn-action-group">
                    <button class="btn btn-sm-custom btn-edit-custom edit-staff-btn" data-bs-toggle="modal" data-bs-target="#editStaffModal">
                        <i class="fa fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-sm-custom btn-delete-custom delete-staff-btn" data-bs-toggle="modal" data-bs-target="#deleteStaffModal">
                        <i class="fa fa-trash"></i> Del
                    </button>
                </div>
            </td>
        `;
        tableBody.appendChild(newRow);
        bootstrap.Modal.getInstance(document.getElementById('addStaffModal')).hide();
        console.log(`✅ Added new staff: ${name}`);
        // Show custom success toast/message
        document.getElementById('addStaffForm').reset();
    });

    // --- Edit Staff - Load Data ---
    document.getElementById('staffTableBody').addEventListener('click', function (event) {
        const editButton = event.target.closest('.edit-staff-btn');
        if (editButton) {
            const row = editButton.closest('tr');
            document.getElementById('editId').value = row.dataset.id;
            document.getElementById('editName').value = row.dataset.name;
            document.getElementById('editPosition').value = row.dataset.position;
            document.getElementById('editEmail').value = row.dataset.email;
            document.getElementById('editPhone').value = row.dataset.phone;
            document.getElementById('editDoB').value = row.dataset.dob;
            document.getElementById('editSex').value = row.dataset.sex;
            document.getElementById('editStatus').value = row.dataset.status;
        }
    });

    // --- Edit Staff - Save Changes ---
    document.getElementById('editStaffForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const id = document.getElementById('editId').value;
        const row = document.querySelector(`#staffTableBody tr[data-id="${id}"]`);
        if (!row) return;

        const name = document.getElementById('editName').value;
        const position = document.getElementById('editPosition').value;
        const email = document.getElementById('editEmail').value;
        const phone = document.getElementById('editPhone').value;
        const dob = document.getElementById('editDoB').value;
        const sex = document.getElementById('editSex').value;
        const status = document.getElementById('editStatus').value;

        // 1. Update data attributes for search/future edits
        row.dataset.name = name;
        row.dataset.position = position;
        row.dataset.email = email;
        row.dataset.phone = phone;
        row.dataset.dob = dob;
        row.dataset.sex = sex;
        row.dataset.status = status;

        // 2. Update visible table cells
        // NOTE: Column indices are based on the table structure (starting at 0 for checkbox)
        row.cells[2].textContent = name;
        row.cells[3].textContent = position;
        row.cells[4].textContent = sex;
        row.cells[5].textContent = formatDateToDisplay(dob);
        row.cells[6].textContent = email;
        row.cells[7].textContent = phone;
        row.cells[8].innerHTML = getStatusBadge(status);

        bootstrap.Modal.getInstance(document.getElementById('editStaffModal')).hide();
        console.log(`✅ Saved changes for staff ID ${id} (${name})!`);
        // Show custom success toast/message
    });

    // --- Delete Staff - Setup Modal ---
    document.getElementById('staffTableBody').addEventListener('click', function (event) {
        const deleteButton = event.target.closest('.delete-staff-btn');
        if (deleteButton) {
            deleteTargetRowStaff = deleteButton.closest('tr');
            document.getElementById('deleteStaffModalLabel').textContent = "Confirm Delete";
            document.getElementById('deleteModalBodyText').textContent = `Are you sure you want to delete staff ${deleteTargetRowStaff.dataset.name}?`;
            document.getElementById('confirmDeleteBtn').dataset.mode = 'single';
        }
    });

    // --- Delete Staff - Confirm Action ---
    const confirmDeleteStaffBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteStaffBtn) {
        confirmDeleteStaffBtn.addEventListener('click', function () {
            // Check if this button is specifically meant for the staff modal (using the data-mode set above)
            const mode = this.dataset.mode;
            if (mode !== 'single' && mode !== 'bulk') return; // Exit if not staff related

            const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteStaffModal'));

            if (mode === 'single' && deleteTargetRowStaff) {
                const name = deleteTargetRowStaff.dataset.name;
                deleteTargetRowStaff.remove();
                deleteTargetRowStaff = null;
                console.log(`🗑️ Staff ${name} deleted successfully!`);
            } else if (mode === 'bulk') {
                const selected = Array.from(document.querySelectorAll('#staffTableBody .row-checkbox:checked'));
                if (selected.length > 0) {
                    selected.forEach(cb => cb.closest('tr').remove());
                    document.getElementById('selectAll').checked = false;
                    console.log(`🗑️ Deleted ${selected.length} selected staff!`);
                }
            }
            deleteModal.hide();
        });
    }
}

// ===================================
// II. MOVIE MANAGEMENT
// ===================================

function performMovieSearch() {
    const tableBody = document.getElementById('movieTableBody');
    if (!tableBody) return;

    const query = document.getElementById('searchInput').value.toLowerCase().trim();
    const rows = tableBody.querySelectorAll('tr');
    rows.forEach(row => {
        // Indices based on the movie table (assuming: 1-ID, 2-Title)
        const id = row.cells[1] ? row.cells[1].textContent.toLowerCase() : '';
        const title = row.cells[2] ? row.cells[2].textContent.toLowerCase() : '';
        row.style.display = (id.includes(query) || title.includes(query)) ? '' : 'none';
    });
}

// --- Movie Add/Edit (Placeholder for full implementation) ---
// Note: datetimepicker requires jQuery and its dependencies, which are assumed to be loaded.
$(document).ready(function () {
    if ($('#movieReleaseDatepicker').length) {
        $('#movieReleaseDatepicker').datetimepicker({ format: 'YYYY-MM-DD', widgetParent: $('#addMovieModal .modal-content') });
    }
    if ($('#editMovieReleaseDatepicker').length) {
        $('#editMovieReleaseDatepicker').datetimepicker({ format: 'YYYY-MM-DD', widgetParent: $('#editMovieModal .modal-content') });
    }
});


// ===================================
// III. VOUCHER MANAGEMENT
// ===================================

function performVoucherSearch() {
    const tableBody = document.getElementById('voucherTableBody');
    if (!tableBody) return;

    const query = document.getElementById("searchInput").value.toLowerCase().trim();
    const rows = tableBody.querySelectorAll("tr");
    rows.forEach(row => {
        // Indices based on the voucher table (assuming: 1-Code)
        const code = row.cells[1] ? row.cells[1].innerText.toLowerCase() : '';
        row.style.display = code.includes(query) ? "" : "none";
    });
}

//open modal delete modal
function openDeleteModal(id, name) {
    document.getElementById('deleteVoucherId').value = id;
    document.getElementById('deleteVoucherIdDisplay').textContent = id;
    document.getElementById('deleteVoucherCode').textContent = name;

    new bootstrap.Modal(document.getElementById('deleteVoucherModal')).show();
}

// ===================================
// IV. GLOBAL SEARCH FOR STAFF + MOVIE + VOUCHER
// ===================================

const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');

if (searchInput) {
    // Listen to 'input' for real-time filtering
    searchInput.addEventListener('input', function () {
        performStaffSearch();
        performMovieSearch();
        performVoucherSearch();
    });
    // Listen to 'keypress' (specifically Enter) to prevent form submission if it was a form
    searchInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            performStaffSearch();
            performMovieSearch();
            performVoucherSearch();
        }
    });
}

if (searchButton) {
    searchButton.addEventListener('click', function () {
        performStaffSearch();
        performMovieSearch();
        performVoucherSearch();
    });
}

// ===================================
// IV. SHIFT SCHEDULE MANAGEMENT
// ===================================
// document.addEventListener("DOMContentLoaded", function() {
//     document.querySelectorAll(".btn-delete-shift").forEach(btn => {
//         btn.addEventListener("click", function() {
//             const id = this.dataset.id;
//             const staff = this.dataset.staff;
//
//             const staffEl = document.getElementById("deleteShiftStaff");
//             const idEl = document.getElementById("deleteShiftId");
//             const idDisplayEl = document.getElementById("deleteShiftIdDisplay");
//
//             staffEl.textContent = staff;
//             idEl.value = id;
//             idDisplayEl.textContent = id;
//
//             const modalEl = document.getElementById("deleteShiftModal");
//             let modal = bootstrap.Modal.getInstance(modalEl);
//             if (!modal) modal = new bootstrap.Modal(modalEl);
//             modal.show();
//         });
//     });
// });


// Hàm check-in shift
function checkInShift(shiftId) {
    fetch(`/shift-schedules/checkin/${shiftId}`, { method: 'POST' })
        .then(() => location.reload())
        .catch(err => console.error(err));
}





document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".btn-delete-shift").forEach(btn => {
        btn.addEventListener("click", function() {
            const id = this.dataset.id;
            const staff = this.dataset.staff;

            const staffEl = document.getElementById("deleteShiftStaff");
            const idEl = document.getElementById("deleteShiftId");
            const idDisplayEl = document.getElementById("deleteShiftIdDisplay");

            staffEl.textContent = staff;
            idEl.value = id;
            idDisplayEl.textContent = id;

            const modalEl = document.getElementById("deleteShiftModal");
            let modal = bootstrap.Modal.getInstance(modalEl);
            if (!modal) modal = new bootstrap.Modal(modalEl);
            modal.show();
        });
    });
});







