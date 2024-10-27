package info.trizub.clamav.webclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "info.trizub.clamav.webclient")
@SpringBootApplication
public class ClamAVWebClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClamAVWebClientApplication.class, args);
	}

}
