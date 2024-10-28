function updateNavItems() {
	const elements = document.querySelectorAll('.nav-link');
	let requestURI = window.location.pathname;
	if (requestURI != null && requestURI === '/') {
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