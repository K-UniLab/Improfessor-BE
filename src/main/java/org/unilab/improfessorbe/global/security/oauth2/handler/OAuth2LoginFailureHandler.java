package org.unilab.improfessorbe.global.security.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

	@Value("${app.oauth2.authorized-redirect-uri}")
	private String redirectUri;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		log.error("OAuth2 로그인 실패: {}", exception.getMessage());

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
			.queryParam("error", "oauth2_login_failed")
			.queryParam("message", exception.getMessage())
			.build().toUriString();

		response.sendRedirect(targetUrl);
	}
}