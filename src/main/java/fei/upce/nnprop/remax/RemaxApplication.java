package fei.upce.nnprop.remax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RemaxApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemaxApplication.class, args);
	}

}
