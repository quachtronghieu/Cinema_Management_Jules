// ----------------------
// Khai báo biến chính
// ----------------------
var form = document.getElementById('registerForm');
var message = document.getElementById('formMessage');

// ----------------------
// Hàm reset form
// ----------------------
function resetForm() {
    if (!form) return;
    form.reset();

    document.querySelectorAll('.form__input--error').forEach(input => {
        input.classList.remove('form__input--error');
    });

    document.querySelectorAll('.form__error').forEach(error => {
        error.classList.remove('visible');
        error.classList.add('hidden');
    });

    message.className = 'form__message hidden';

    // Reset hiển thị mật khẩu
    document.getElementById('password').type = 'password';
    document.getElementById('confirmPassword').type = 'password';
    document.getElementById('passwordToggle').textContent = 'Show';
    document.getElementById('confirmPasswordToggle').textContent = 'Show';
}

// ----------------------
// Hiển thị / ẩn password
// ----------------------
function togglePassword(inputId, buttonId) {
    var input = document.getElementById(inputId);
    var button = document.getElementById(buttonId);

    if (!input || !button) return;

    if (input.type === 'password') {
        input.type = 'text';
        button.textContent = 'Hidden';
    } else {
        input.type = 'password';
        button.textContent = 'Show';
    }
}

function toggleNewPassword(inputId, buttonId) {
    var input = document.getElementById(inputId);
    var button = document.getElementById(buttonId);

    if (!input || !button) return;

    if (input.type === 'password') {
        input.type = 'text';
        button.textContent = 'Hidden';
    } else {
        input.type = 'password';
        button.textContent = 'Show';
    }
}

// ----------------------
// Hiển thị lỗi 1 ô input
// ----------------------
function showError(fieldName, errorMessage) {
    var input = document.getElementById(fieldName);
    var errorDiv = document.getElementById(fieldName + 'Error');
    if (!input || !errorDiv) return;

    input.classList.add('form__input--error');
    errorDiv.textContent = errorMessage;
    errorDiv.classList.remove('hidden');
    errorDiv.classList.add('visible');
}

// Ẩn lỗi
function hideError(fieldName) {
    var input = document.getElementById(fieldName);
    var errorDiv = document.getElementById(fieldName + 'Error');
    if (!input || !errorDiv) return;

    input.classList.remove('form__input--error');
    errorDiv.classList.remove('visible');
    errorDiv.classList.add('hidden');
}

// ----------------------
// Validate từng field
// ----------------------
function validateField(fieldName) {
    var field = document.getElementById(fieldName);
    if (!field) return false;

    hideError(fieldName);
    var value = field.type === 'checkbox' ? field.checked : field.value.trim();

    switch (fieldName) {
        case 'username':
            if (value === '') {
                showError(fieldName, 'The username is required.');
                return false;
            }
            if (field.value.length < 2 || field.value.length > 20) {
                showError(fieldName, 'The name must have at least 2 characters.');
                return false;
            }
            // Không cho phép khoảng trắng
            if (/\s/.test(field.value)) {
                showError(fieldName, 'Usernames cannot contain spaces.');
                return false;
            }
            break

        case 'dob':
            if (value === '') {
                showError(fieldName, 'Date of birth is required.');
                return false;
            }
            const today = new Date().toISOString().split('T')[0];
            if (value > today) {
                showError(fieldName, 'The date of birth cannot be later than today.');
                return false;
            }
            break;

        case 'email':
            if (value === '') {
                showError(fieldName, 'Email is required.');
                return false;
            }
            break;

        case 'phone':
            if (value === '') {
                showError(fieldName, 'The phone number is required.');
                return false;
            }
            break;

        case 'password':
            if (value === '') {
                showError(fieldName, 'A password is required.');
                return false;
            }
            if (field.value.length < 6) {
                showError(fieldName, 'The password must be at least 6 characters long.');
                return false;
            }
            break;

        case 'confirmPassword':
            var password = document.getElementById('password').value;
            if (value === '') {
                showError(fieldName, 'Please confirm the password.');
                return false;
            }
            if (field.value !== password) {
                showError(fieldName, 'The confirmation password does not match.');
                return false;
            }
            break;

        case 'terms':
            if (!value) {
                showError(fieldName, 'You must agree to the terms of service.');
                return false;
            }
            break;

        case 'sex':
            if (value === '' || value === null) {
                showError(fieldName, 'Please select a gender.');
                return false;
            }
            break;
    }

    return true;
}

// ----------------------
// Validate toàn bộ form
// ----------------------
function validateForm() {
    var isValid = true;

    ['username', 'dob', 'sex', 'email', 'phone', 'password', 'confirmPassword', 'terms'].forEach(field => {
        if (!validateField(field)) isValid = false;
    });

    return isValid;
}

// ----------------------
// Hiển thị thông báo chung
// ----------------------
function showMessage(text, type) {
    if (!message) return;

    message.textContent = text;
    message.classList.remove('hidden', 'form__message--error', 'form__message--success');

    if (type === 'error') {
        message.classList.add('form__message--error', 'visible');
    } else {
        message.classList.add('form__message--success', 'visible');
    }
}

// ----------------------
// Xử lý khi nhấn Submit
// ----------------------
function handleSubmit(event) {
    event.preventDefault();

    if (validateForm()) {
        showMessage("Registration successful! Sending information...", "success");
        setTimeout(() => form.submit(), 500);
    } else {
        showMessage("Please enter all the required information and agree to the terms!", "error");
    }
}

// ----------------------
// Gắn các sự kiện
// ----------------------
function initModal() {
    // Toggle password
    document.getElementById('passwordToggle')?.addEventListener('click', () => togglePassword('password', 'passwordToggle'));
    document.getElementById('confirmPasswordToggle')?.addEventListener('click', () => togglePassword('confirmPassword', 'confirmPasswordToggle'));
    document.getElementById('toggleNewPassword').addEventListener('click', () => togglePassword('new_password', 'toggleNewPassword'));

    // Validation khi blur
    ['username', 'dob', 'email', 'phone', 'password', 'confirmPassword'].forEach(id => {
        var el = document.getElementById(id);
        if (el) el.addEventListener('blur', () => validateField(id));
    });

    // Checkbox terms
    document.getElementById('terms')?.addEventListener('change', () => validateField('terms'));
}

// ----------------------
// Chạy khi trang load
// ----------------------
window.addEventListener('load', function () {
    initModal();
    form?.addEventListener('submit', handleSubmit);
});
