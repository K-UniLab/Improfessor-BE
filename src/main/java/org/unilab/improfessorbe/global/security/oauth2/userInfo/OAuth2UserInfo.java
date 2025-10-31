package org.unilab.improfessorbe.global.security.oauth2.userInfo;

import java.util.Map;

import lombok.Getter;

@Getter
public abstract class OAuth2UserInfo {
	protected Map<String, Object> attributes;
	protected String userNameAttributeName;

	public OAuth2UserInfo(Map<String, Object> attributes, String userNameAttributeName) {
		this.attributes = attributes;
		this.userNameAttributeName = userNameAttributeName;
	}

	public String getId() {
		Object idValue = attributes.get(userNameAttributeName);
		return idValue != null ? idValue.toString() : null;
	}

	public abstract String getNickname();

	public abstract String getEmail();

	public abstract String getProvider();
}