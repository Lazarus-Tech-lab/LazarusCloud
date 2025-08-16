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
            themeSwitcher.textContent = isLight ? '🌙' : '☀️';
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

        // В обработчике смены темы
        const themeIcon = document.querySelector('#themeSwitcher .material-symbols-outlined');
        if (isLightTheme) {
            themeIcon.textContent = 'light_mode';
        } else {
            themeIcon.textContent = 'dark_mode';
        }
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
            deleteAllCookies();
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
        const res = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password }),
            credentials: 'include'
        });

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Registration failed');
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
                    setTimeout(() => window.location.href = '/home', 1500);
                } catch (error) {
                    Validator.showError('loginPasswordError', error.message || 'Login failed');
                } finally {
                    UIHelpers.setLoading(this, false);
                }
            }
        });
    }

    // Register form
    registerForm.addEventListener('submit', async function (e) {
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

                // ✅ Показываем уведомление
                showNotification("🎉 Успешно!", "Аккаунт создан. Войдите в систему");

                // ✅ Плавный переход на логин
                FormManager.switchForm('login');

                setTimeout(() => {
                    const loginForm = document.getElementById('loginForm');
                    if (loginForm) loginForm.classList.add('fade-in');

                    document.getElementById('loginUsername')?.focus();
                }, 400);

            } catch (error) {
                const msg = error.message || 'Registration failed';
                if (msg.toLowerCase().includes('email')) {
                    Validator.showError('registerEmailError', msg);
                } else if (msg.toLowerCase().includes('username')) {
                    Validator.showError('registerUsernameError', msg);
                } else {
                    Validator.showError('registerEmailError', msg);
                }
            } finally {
                UIHelpers.setLoading(this, false);
            }
        }
    });
});

function showNotification(title, message, time = "Now") {
    const notification = document.createElement('div');
    notification.className = 'notification fade-notification';
    notification.innerHTML = `
        <div class="notification-title">${title}</div>
        <div class="notification-message">${message}</div>
        <div class="notification-time">${time}</div>
        <div class="notification-progress"></div>
        <button class="notification-close">&times;</button>
    `;

    document.getElementById('notificationContainer').appendChild(notification);

    notification.updateProgress = (percent) => {
        const progressBar = notification.querySelector('.notification-progress');
        progressBar.style.width = `${percent}%`;
        progressBar.style.backgroundColor = percent === 100 ? '#4CAF50' : '#2196F3';
    };

    notification.updateMessage = (newMessage) => {
        notification.querySelector('.notification-message').textContent = newMessage;
    };

    notification.updateTitle = (newTitle) => {
        notification.querySelector('.notification-title').textContent = newTitle;
    };

    notification.autoClose = (ms = 5000) => {
        setTimeout(() => {
            notification.classList.add('hide');
            setTimeout(() => notification.remove(), 300);
        }, ms);
    };

    notification.querySelector('.notification-close').addEventListener('click', () => {
        notification.remove();
    });

    return notification;
}
function openPreviewModal(fileId, fileName, ext) {
    const url = `/api/storage/raw/${fileId}`;
    const modal = document.getElementById('previewModal');
    const content = document.getElementById('previewContent');
    const title = document.getElementById('previewTitle');

    const isAudio = ['mp3', 'wav', 'ogg'].includes(ext);
    const isImage = ['png', 'jpg', 'jpeg', 'webp', 'gif'].includes(ext);
    const isVideo = ['mp4', 'webm', 'mkv'].includes(ext);
    const isText = ext === 'txt';

    if (isAudio) {
        console.log(url);
        console.log(fileName);
        console.log(ext);
        openAudioPlayer(url, fileName, ext);
        return;
    }

    // Для всех остальных — открываем предпросмотр
    content.innerHTML = '';
    title.textContent = fileName;

    if (isImage) {
        const img = document.createElement('img');
        img.src = url;
        img.style.maxWidth = '100%';
        img.style.maxHeight = '100%';
        content.appendChild(img);
    } else if (isVideo) {
        const video = document.createElement('video');
        video.src = url;
        video.controls = true;
        video.style.width = '100%';
        content.appendChild(video);
    } else if (isText) {
        fetch(url).then(res => res.text()).then(text => {
            const pre = document.createElement('pre');
            pre.textContent = text;
            pre.style.whiteSpace = 'pre-wrap';
            content.appendChild(pre);
        });
    } else {
        content.textContent = 'Unsupported file type';
    }

    // Показываем окно
    modal.classList.add('active');
}

// Инициализация элементов
const audioPlayer = document.getElementById('audioPlayer');
const audioElement = new Audio();
const playPauseBtn = document.getElementById('playPauseBtn');
const progressBar = document.getElementById('progressBar');
const volumeBar = document.getElementById('volumeBar');
const volumeBtn = document.getElementById('volumeBtn');
const currentTimeEl = document.getElementById('currentTime');
const durationEl = document.getElementById('duration');
const closePlayer = document.getElementById('closePlayer');

// Состояние плеера
let isPlaying = false;
let lastVolume = 100;

// Функция для отображения времени
function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
}

// Обновление прогресса
function updateProgress() {
    progressBar.value = (audioElement.currentTime / audioElement.duration) * 100;
    currentTimeEl.textContent = formatTime(audioElement.currentTime);
}

// Обновление длительности
function updateDuration() {
    durationEl.textContent = formatTime(audioElement.duration);
}

// Воспроизведение/пауза
function togglePlayPause() {
    if (isPlaying) {
        audioElement.pause();
        playPauseBtn.innerHTML = '<span class="material-symbols-outlined">play_arrow</span>';
    } else {
        audioElement.play();
        playPauseBtn.innerHTML = '<span class="material-symbols-outlined">pause</span>';
    }
    isPlaying = !isPlaying;
}

// Перемотка
function setProgress() {
    audioElement.currentTime = (progressBar.value / 100) * audioElement.duration;
}

// Управление громкостью
function setVolume() {
    const volume = volumeBar.value / 100;
    audioElement.volume = volume;

    if (volume > 0.5) {
        volumeBtn.innerHTML = '<span class="material-symbols-outlined">volume_up</span>';
    } else if (volume > 0) {
        volumeBtn.innerHTML = '<span class="material-symbols-outlined">volume_down</span>';
    } else {
        volumeBtn.innerHTML = '<span class="material-symbols-outlined">volume_off</span>';
    }
}

// Переключение mute/unmute
function toggleMute() {
    if (audioElement.volume > 0) {
        lastVolume = audioElement.volume * 100;
        audioElement.volume = 0;
        volumeBar.value = 0;
        volumeBtn.innerHTML = '<span class="material-symbols-outlined">volume_off</span>';
    } else {
        audioElement.volume = lastVolume / 100;
        volumeBar.value = lastVolume;
        setVolume();
    }
}

// События
playPauseBtn.addEventListener('click', togglePlayPause);
progressBar.addEventListener('input', setProgress);
volumeBar.addEventListener('input', setVolume);
volumeBtn.addEventListener('click', toggleMute);
closePlayer.addEventListener('click', () => {
    audioPlayer.classList.add('hidden');
    audioElement.pause();
    isPlaying = false;
    playPauseBtn.innerHTML = '<span class="material-symbols-outlined">play_arrow</span>';
});

audioElement.addEventListener('timeupdate', updateProgress);
audioElement.addEventListener('loadedmetadata', updateDuration);
audioElement.addEventListener('ended', () => {
    isPlaying = false;
    playPauseBtn.innerHTML = '<span class="material-symbols-outlined">play_arrow</span>';
});

// Функция для открытия плеера
function openAudioPlayer(fileId, ext, fileName, artist = 'Неизвестный исполнитель') {
    const img = document.getElementById('audioPic');
    const titleEl = document.getElementById('audioTitle');
    const artistEl = document.getElementById('audioArtist');

    titleEl.textContent = fileName;
    artistEl.textContent = artist;
    img.src = '/api/storage/raw/thumb/' + fileId;
    img.onerror = () => { img.src = '/placeholder.jpg'; };

    audioElement.src = '/api/storage/raw/' + fileId;
    audioPlayer.classList.remove('hidden');

    // Автовоспроизведение (если разрешено)
    audioElement.play().then(() => {
        isPlaying = true;
        playPauseBtn.innerHTML = '<span class="material-symbols-outlined">pause</span>';
    }).catch(e => {
        console.log('Автовоспроизведение заблокировано:', e);
    });
}

function closePreviewModal() {
    const modal = document.getElementById('previewModal');
    modal.classList.remove('active');
}

(function enablePreviewResizeAndDrag() {
    const modal = document.getElementById('previewModal');
    const resizer = document.getElementById('previewResizer');
    const header = modal.querySelector('.preview-header');

    // RESIZE
    resizer.addEventListener('mousedown', (e) => {
        e.preventDefault();
        document.onmousemove = (ev) => {
            modal.style.width = (ev.clientX - modal.offsetLeft) + 'px';
            modal.style.height = (ev.clientY - modal.offsetTop) + 'px';
        };
        document.onmouseup = () => {
            document.onmousemove = null;
            document.onmouseup = null;
        };
    });

    // DRAG
    header.addEventListener('mousedown', (e) => {
        e.preventDefault();
        const rect = modal.getBoundingClientRect();
        const offsetX = e.clientX - rect.left;
        const offsetY = e.clientY - rect.top;

        document.onmousemove = (ev) => {
            modal.style.left = (ev.clientX - offsetX) + 'px';
            modal.style.top = (ev.clientY - offsetY) + 'px';
            modal.style.right = 'auto';
            modal.style.bottom = 'auto';
            modal.style.position = 'fixed';
        };
        document.onmouseup = () => {
            document.onmousemove = null;
            document.onmouseup = null;
        };
    });
})();

document.getElementById('fileSearchInput').addEventListener('input', (e) => {
    const query = e.target.value.toLowerCase();

    const allFiles = fileCache[currentPath] || [];
    const filtered = allFiles.filter(file => file.filename.toLowerCase().includes(query));

    renderFiles(filtered);
});

function deleteAllCookies() {
    var cookies = document.cookie.split(";");

    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eqPos = cookie.indexOf("=");
        var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
    }
}

