package de.opengamebackend.matchmaking;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MatchmakingApplication {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public OpenAPI customOpenAPI(Environment environment) {
		String applicationVersion = environment.getProperty("application.version");

		return new OpenAPI().info(new Info()
				.title("Open Game Backend Matchmaking")
				.version(applicationVersion)
				.description("Provides lists of available game servers, and allows players to connect to them.")
				.license(new License().name("MIT").url("https://github.com/open-game-backend/matchmaking/blob/develop/LICENSE")));
	}

	public static void main(String[] args) {
		SpringApplication.run(MatchmakingApplication.class, args);
	}
}
