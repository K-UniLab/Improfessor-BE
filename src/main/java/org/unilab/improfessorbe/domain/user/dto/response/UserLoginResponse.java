package org.unilab.improfessorbe.domain.user.dto.response;

import org.unilab.improfessorbe.global.security.jwt.JwtToken;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserLoginResponse {
	private String grantType;
	private String accessToken;
	private String refreshToken;

	public static UserLoginResponse of(JwtToken jwtToken) {
		return UserLoginResponse.builder()
			.grantType(jwtToken.getGrantType())
			.accessToken(jwtToken.getAccessToken())
			.refreshToken(jwtToken.getRefreshToken())
			.build();
	}
}
