package pl.volleyflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class VolleyflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolleyflowApplication.class, args);
	}

}
