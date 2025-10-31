package org.unilab.improfessorbe.global.security.oauth2.userInfo;

import java.util.Map;

import org.unilab.improfessorbe.global.exception.OAuth2AuthenticationProcessingException;

public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes,
		String userNameAttributeName) {
		if ("kakao".equals(registrationId)) {
			return new KakaoOAuth2UserInfo(attributes, userNameAttributeName);
		}
		// TODO: 구글, 네이버 추가

		throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported");
	}
}