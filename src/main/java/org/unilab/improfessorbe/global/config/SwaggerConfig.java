package org.unilab.improfessorbe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

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
		
		return new OpenAPI()
			.components(new Components().addSecuritySchemes("Bearer Token", apiKey))
			.addSecurityItem(securityRequirement);
	}

	private Info apiInfo(){
		return new Info()
			.title("Improfessor API")
			.description("Improfessor API Documentation")
			.version("1.0");
	}
}
