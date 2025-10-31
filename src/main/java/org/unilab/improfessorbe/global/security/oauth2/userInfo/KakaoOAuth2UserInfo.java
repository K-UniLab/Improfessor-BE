package org.unilab.improfessorbe.global.security.oauth2.userInfo;

import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

	public KakaoOAuth2UserInfo(Map<String, Object> attributes, String userNameAttributeName) {
		super(attributes, userNameAttributeName);
	}

	@Override
	public String getNickname() {
		Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");
		if (properties == null) {
			return null;
		}
		return (String)properties.get("nickname");
	}

	@Override
	public String getEmail() {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		if (kakaoAccount == null) {
			return null;
		}
		return (String)kakaoAccount.get("email");
	}

	@Override
	public String getProvider() {
		return "kakao";
	}
}