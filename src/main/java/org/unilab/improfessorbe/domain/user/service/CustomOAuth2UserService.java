package org.unilab.improfessorbe.domain.user.service;

import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.global.security.oauth2.CustomOAuth2User;
import org.unilab.improfessorbe.global.security.oauth2.userInfo.OAuth2UserInfo;
import org.unilab.improfessorbe.global.security.oauth2.userInfo.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserService userService;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		// OAuth2 사용자 정보 추출
		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes(),
			userNameAttributeName);

		// 사용자 처리 (신규 가입 또는 기존 사용자 업데이트)
		User user = processOAuth2User(userInfo);

		return new CustomOAuth2User(oAuth2User, user.getEmail(), registrationId, userNameAttributeName);
	}

	private User processOAuth2User(OAuth2UserInfo userInfo) {
		Optional<User> userOptional = userService.findByEmailAndDeletedAtIsNull(userInfo.getEmail());

		if (userOptional.isPresent()) {
			User existingUser = userOptional.get();
			// 기존 사용자 정보 업데이트
			if (!existingUser.getProvider().equals(userInfo.getProvider())) {
				existingUser.updateProvider(userInfo.getProvider());
				existingUser.updateProviderId(userInfo.getId());
				log.info("기존 사용자 정보 업데이트. provider:" + userInfo.getProvider() + " providerId: " + userInfo.getId());
			}
			return existingUser;
		} else {
			// 닉네임 중복 방지
			String uniqueNickname = generateUniqueNickname(userInfo.getProvider(), userInfo.getId());
			// 새 사용자 생성
			User newUser = User.createOAuth2User(uniqueNickname, userInfo.getEmail(), userInfo.getProvider(),
				userInfo.getId());
			log.info("새 사용자 생성. nickname:" + uniqueNickname + " email: " + userInfo.getEmail());
			return userService.saveUser(newUser);
		}
	}

	private String generateUniqueNickname(String provider, String providerId) {
		String hash = DigestUtils.md5Hex(providerId).substring(0, 6);
		return provider + "_" + hash;
	}

}