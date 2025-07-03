package org.unilab.improfessorbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${server.url}")
	private String serverUrl;

	@Bean
	public OpenAPI openAPI(){
		SecurityScheme apiKey = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.in(SecurityScheme.In.HEADER)
			.name("Authorization")
			.scheme("bearer")
			.bearerFormat("JWT");
		
		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("Bearer Token");

		Server server = new Server();
		server.setUrl(serverUrl);
		
		return new OpenAPI()
			.components(new Components().addSecuritySchemes("Bearer Token", apiKey))
			.info(apiInfo())
			.addSecurityItem(securityRequirement)
			.addServersItem(server);
	}

	private Info apiInfo(){
		return new Info()
			.title("Improfessor API")
			.description("Improfessor API Documentation")
			.version("1.0");
	}
}
