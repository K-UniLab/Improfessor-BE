package org.unilab.improfessorbe.global.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		return path.equals("/") ||
			path.startsWith("/api/users/register") ||
			path.startsWith("/api/users/login") ||
			path.startsWith("/api/users/refresh-token") ||
			path.startsWith("/api/users/email") ||
			path.startsWith("/swagger-ui") ||
			path.startsWith("/v3/api-docs") ||
			path.startsWith("/actuator") ||
			path.startsWith("/oauth2") ||
			path.startsWith("/login/oauth2") ||
			path.equals("/favicon.ico");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		String token = resolveToken(request);

		if (token != null) {
			try {
				if (jwtTokenProvider.validateToken(token)) {
					Authentication authentication = jwtTokenProvider.getAuthentication(token);
					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.debug("JWT authentication success for URI: {}", requestURI);
				} else {
					log.error("Token validation failed for URI: {}, Token: {}",
						requestURI, token.substring(0, Math.min(20, token.length())) + "...");
				}
			} catch (Exception e) {
				log.error("Token authentication error for URI: {}, Error: {}",
					requestURI, e.getMessage(), e);
			}
		} else {
			log.warn("No token found for URI: {}, Authorization header: {}",
				requestURI, request.getHeader("Authorization"));
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
