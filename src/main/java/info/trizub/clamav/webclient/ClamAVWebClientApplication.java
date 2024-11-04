/**
 * Class Name: ClamAVWebClientApplication
 * Description: This is the main class to run Spring MVC application.
 * 
 * Author: Ruslan Huzii
 * Email:  ruslan@trizub.info
 * Date: 2024-11-04
 * 
 * License: GNU Lesser General Public License v2.1
 */
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
