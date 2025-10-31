package org.unilab.improfessorbe.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JwtToken {
	private String grantType;
	private String accessToken;
	private String refreshToken;
}
