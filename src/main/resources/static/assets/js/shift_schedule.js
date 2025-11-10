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