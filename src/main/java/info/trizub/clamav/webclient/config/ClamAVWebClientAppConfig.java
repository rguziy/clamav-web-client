package info.trizub.clamav.webclient.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class ClamAVWebClientAppConfig extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

	List<Locale> LOCALES = Arrays.asList(new Locale("en"), new Locale("uk"));

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		String headerLang = request.getHeader("Accept-Language");
		return headerLang == null || headerLang.isEmpty() ? Locale.getDefault()
				: Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES);
	}

	@Bean
	ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasenames("messages");
		source.setDefaultEncoding("UTF-8");
		source.setUseCodeAsDefaultMessage(true);
		return source;
	}

}
