package de.opengamebackend.matchmaking;

import de.opengamebackend.util.config.ApplicationConfig;
import de.opengamebackend.util.EnableOpenGameBackendUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableOpenGameBackendUtils
public class MatchmakingApplication {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public OpenAPI customOpenAPI(ApplicationConfig applicationConfig) {
		return new OpenAPI().info(new Info()
				.title("Open Game Backend Matchmaking")
				.version(applicationConfig.getVersion())
				.description("Provides lists of available game servers, and allows players to connect to them.")
				.license(new License().name("MIT").url("https://github.com/open-game-backend/matchmaking/blob/develop/LICENSE")));
	}

	public static void main(String[] args) {
		SpringApplication.run(MatchmakingApplication.class, args);
	}
}
