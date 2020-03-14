package ru.octoshell.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Точка входа приложения
 */
@SpringBootApplication
public class OctoshellBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(OctoshellBotApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }
}
