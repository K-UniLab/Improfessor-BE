package org.unilab.improfessorbe.global.security.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;
import org.unilab.improfessorbe.global.security.jwt.JwtToken;
import org.unilab.improfessorbe.global.security.jwt.JwtTokenProvider;
import org.unilab.improfessorbe.global.security.oauth2.CustomOAuth2User;
import org.unilab.improfessorbe.global.util.RedisUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisUtil redisUtil;

	@Value("${app.oauth2.authorized-redirect-uri}")
	private String redirectUri;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		try {
			// JWT 토큰 생성
			JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

			// Refresh Token을 Redis에 저장
			long refreshTokenExpirationMillis = 604800000L; // 7일
			redisUtil.setDataExpire(oAuth2User.getName(), jwtToken.getRefreshToken(),
				refreshTokenExpirationMillis / 1000);

			// 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
			String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
				.queryParam("grant_type", jwtToken.getGrantType())
				.queryParam("accessToken", jwtToken.getAccessToken())
				.queryParam("refreshToken", jwtToken.getRefreshToken())
				.build().toUriString();

			log.info("OAuth2 로그인 성공: {}", oAuth2User.getEmail());
			response.sendRedirect(targetUrl);

		} catch (Exception e) {
			log.error("OAuth2 로그인 성공 처리 중 오류: {}", e.getMessage());
			response.sendRedirect(redirectUri + "?error=token_generation_failed");
		}
	}
}