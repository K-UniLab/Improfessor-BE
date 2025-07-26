package org.unilab.improfessorbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiWebClientConfig {

	@Value("${gemini.api.base-url}")
	private String geminiBaseUrl;

	@Value("${gemini.api.timeout}")
	private int timeout;

	@Bean
	public WebClient geminiWebClient() {
		return WebClient.builder()
			.baseUrl(geminiBaseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}
}