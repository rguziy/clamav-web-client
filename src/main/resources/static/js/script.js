function updateNavItems() {
	const elements = document.querySelectorAll('.nav-link');
	let requestURI = window.location.pathname;
	if (requestURI != null && (requestURI == '/' || requestURI == '/update')) {
		requestURI = '/home';
	}
	if (elements !== null && requestURI !== null) {
		for (const element of elements) {
			if (element.href !== null) {
				if (element.href.includes(requestURI)) {
					element.classList.add('active');
					element.classList.remove('link-dark');
				} else {
					element.classList.remove('active');
					element.classList.add('link-dark');
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