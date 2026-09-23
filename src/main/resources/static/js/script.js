(function () {
	// Apply the saved (or OS preferred) theme as early as possible to avoid a flash of light theme.
	document.documentElement.setAttribute('data-bs-theme', getPreferredTheme());
})();

function getPreferredTheme() {
	try {
		const stored = localStorage.getItem('theme');
		if (stored === 'light' || stored === 'dark') {
			return stored;
		}
	} catch (e) {
		// localStorage may be unavailable (privacy mode) - fall back to OS preference
	}
	return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function updateThemeToggle() {
	const button = document.getElementById('themeToggle');
	if (button === null) {
		return;
	}
	const dark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
	// Show the icon of the theme the button will switch to
	const icon = button.querySelector('i');
	if (icon !== null) {
		icon.classList.toggle('fa-sun', dark);
		icon.classList.toggle('fa-moon', !dark);
	}
	const label = dark ? button.dataset.labelLight : button.dataset.labelDark;
	if (label) {
		button.setAttribute('title', label);
		button.setAttribute('aria-label', label);
	}
}

function toggleTheme() {
	const next = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
	document.documentElement.setAttribute('data-bs-theme', next);
	try {
		localStorage.setItem('theme', next);
	} catch (e) {
		// ignore - theme just won't persist
	}
	updateThemeToggle();
}

function updateNavItems() {
	const elements = document.querySelectorAll('.nav-link');
	let requestURI = window.location.pathname;
	if (requestURI != null && (requestURI == '/' || requestURI == '/update' || requestURI.startsWith('/setLocale'))) {
		requestURI = '/settings';
	}
	if (elements !== null && requestURI !== null) {
		for (const element of elements) {
			if (element.href !== null) {
				if (element.href.includes(requestURI)) {
					element.classList.add('active');
					element.classList.remove('link-body-emphasis');
				} else {
					element.classList.remove('active');
					element.classList.add('link-body-emphasis');
				}
			}
		}
	}
}

function addFormEventListener(formName, waitingId, formButton) {
	// Get the form and the waiting screen elements
	const form = document.getElementById(formName);
	const waitingScreen = document.getElementById(waitingId);
	const button = document.getElementById(formButton);
	if (form !== null && waitingScreen !== null && button != null) {
		// Add an event listener to the form's submit event
		form.addEventListener('submit', (e) => {
			// Display the waiting screen
			waitingScreen.style.display = 'block';
			// Disable submit button
			button.disabled = true;
		});
	}
}