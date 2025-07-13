// Theme management module
const ThemeManager = {
    init() {
        const savedTheme = localStorage.getItem('theme') || 'dark';
        this.applyTheme(savedTheme);
    },

    applyTheme(theme) {
        const isLight = theme === 'light';
        document.body.classList.toggle('light-theme', isLight);
        document.body.classList.toggle('dark-theme', !isLight);

        const themeSwitcher = document.getElementById('themeSwitcher');
        if (themeSwitcher) {
            themeSwitcher.textContent = isLight ? '🌙 Dark Mode' : '☀️ Light Mode';
        }
    },

    saveTheme(theme) {
        localStorage.setItem('theme', theme);
    },

    toggleTheme() {
        const currentTheme = document.body.classList.contains('light-theme') ? 'light' : 'dark';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        this.saveTheme(newTheme);
        this.applyTheme(newTheme);
    }
};

// Form management module
const FormManager = {
    switchForm(formToShow) {
        // Hide all forms
        document.querySelectorAll('.auth-form').forEach(form => {
            form.classList.remove('active');
        });

        // Deactivate all tabs
        document.querySelectorAll('.auth-tab').forEach(tab => {
            tab.classList.remove('active');
        });

        // Show selected form and activate tab
        const form = document.getElementById(`${formToShow}Form`);
        const tab = document.querySelector(`.auth-tab[data-tab="${formToShow}"]`);

        if (form) form.classList.add('active');
        if (tab) tab.classList.add('active');
    },

    clearForm(formId) {
        const form = document.getElementById(formId);
        if (form) {
            form.reset();
            const errorElements = form.querySelectorAll('.error-message');
            errorElements.forEach(el => el.style.display = 'none');
        }
    }
};

// Validation module
const Validator = {
    validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    },

    validatePassword(password) {
        return password.length >= 8;
    },

    validateUsername(username) {
        return username.length >= 3;
    },

    showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    },

    hideError(elementId) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.style.display = 'none';
        }
    },

    validateLoginForm(username, password) {
        let isValid = true;

        if (!username) {
            this.showError('loginUsernameError', 'Username is required');
            isValid = false;
        }

        if (!password) {
            this.showError('loginPasswordError', 'Password is required');
            isValid = false;
        }

        return isValid;
    },

    validateRegisterForm(username, email, password, confirmPassword) {
        let isValid = true;

        if (!username) {
            this.showError('registerUsernameError', 'Username is required');
            isValid = false;
        } else if (!this.validateUsername(username)) {
            this.showError('registerUsernameError', 'Username must be at least 3 characters');
            isValid = false;
        }

        if (!email) {
            this.showError('registerEmailError', 'Email is required');
            isValid = false;
        } else if (!this.validateEmail(email)) {
            this.showError('registerEmailError', 'Please enter a valid email');
            isValid = false;
        }

        if (!password) {
            this.showError('registerPasswordError', 'Password is required');
            isValid = false;
        } else if (!this.validatePassword(password)) {
            this.showError('registerPasswordError', 'Password must be at least 8 characters');
            isValid = false;
        }

        if (password !== confirmPassword) {
            this.showError('registerConfirmPasswordError', 'Passwords do not match');
            isValid = false;
        }

        return isValid;
    }
};

// Auth service module
const AuthService = {
    async login(username, password) {
        try {
            const response = await fetch('/api/auth', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Login failed');
            }

            const data = await response.json();
            this.setAuthCookies(data.accessToken, data.refreshToken);
            return { success: true };
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    },

    async register(username, email, password) {
        try {
            const response = await fetch('/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, email, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Registration failed');
            }

            return await response.json();
        } catch (error) {
            console.error('Registration error:', error);
            throw error;
        }
    },

    setAuthCookies(accessToken, refreshToken) {
        const cookieOptions = [
            `Path=/`,
            `SameSite=Strict`,
            `Max-Age=${30 * 24 * 60 * 60}`
        ].join('; ');

        document.cookie = `access_token=${accessToken}; ${cookieOptions}`;
        document.cookie = `refresh_token=${refreshToken}; ${cookieOptions}`;
    }
};

// UI Helpers module
const UIHelpers = {
    initFormSwitches() {
        document.querySelectorAll('.auth-tab').forEach(tab => {
            tab.addEventListener('click', function() {
                FormManager.switchForm(this.getAttribute('data-tab'));
            });
        });

        const switchLinks = {
            'switchToRegister': 'register',
            'switchToLogin': 'login'
        };

        Object.entries(switchLinks).forEach(([id, form]) => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('click', (e) => {
                    e.preventDefault();
                    FormManager.switchForm(form);
                });
            }
        });
    },

    setLoading(form, isLoading) {
        const submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.disabled = isLoading;
            if (isLoading) {
                submitButton.setAttribute('data-original-text', submitButton.textContent);
                submitButton.innerHTML = '<span class="spinner"></span> Processing...';
            } else {
                submitButton.textContent = submitButton.getAttribute('data-original-text') || 'Submit';
            }
        }
    },

    showAlert(message, type = 'success') {
        const alertBox = document.createElement('div');
        alertBox.className = `alert ${type}`;
        alertBox.textContent = message;

        document.body.appendChild(alertBox);

        setTimeout(() => {
            alertBox.classList.add('fade-out');
            setTimeout(() => alertBox.remove(), 500);
        }, 3000);
    }
};

// Main initialization
document.addEventListener('DOMContentLoaded', () => {
    // Initialize theme
    ThemeManager.init();

    // Initialize form switches
    UIHelpers.initFormSwitches();

    // Theme switcher
    const themeSwitcher = document.getElementById('themeSwitcher');
    if (themeSwitcher) {
        themeSwitcher.addEventListener('click', () => {
            ThemeManager.toggleTheme();
        });
    }

    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            Validator.hideError('loginUsernameError');
            Validator.hideError('loginPasswordError');

            const username = document.getElementById('loginUsername').value.trim();
            const password = document.getElementById('loginPassword').value.trim();

            if (Validator.validateLoginForm(username, password)) {
                UIHelpers.setLoading(this, true);

                try {
                    await AuthService.login(username, password);
                    UIHelpers.showAlert('Login successful! Redirecting...');
                    setTimeout(() => window.location.href = '/', 1500);
                } catch (error) {
                    Validator.showError('loginPasswordError', error.message || 'Login failed');
                } finally {
                    UIHelpers.setLoading(this, false);
                }
            }
        });
    }

    // Register form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            Validator.hideError('registerUsernameError');
            Validator.hideError('registerEmailError');
            Validator.hideError('registerPasswordError');
            Validator.hideError('registerConfirmPasswordError');

            const username = document.getElementById('registerUsername').value.trim();
            const email = document.getElementById('registerEmail').value.trim();
            const password = document.getElementById('registerPassword').value.trim();
            const confirmPassword = document.getElementById('registerConfirmPassword').value.trim();

            if (Validator.validateRegisterForm(username, email, password, confirmPassword)) {
                UIHelpers.setLoading(this, true);

                try {
                    await AuthService.register(username, email, password);
                    UIHelpers.showAlert('Registration successful! Please login.');
                    FormManager.switchForm('login');
                    FormManager.clearForm('registerForm');
                } catch (error) {
                    Validator.showError('registerEmailError', error.message || 'Registration failed');
                } finally {
                    UIHelpers.setLoading(this, false);
                }
            }
        });
    }
});