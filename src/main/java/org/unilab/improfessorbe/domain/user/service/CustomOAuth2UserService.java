package org.unilab.improfessorbe.domain.user.service;

import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.domain.user.infrastructure.repository.UserRepository;
import org.unilab.improfessorbe.global.security.oauth2.CustomOAuth2User;
import org.unilab.improfessorbe.global.security.oauth2.userInfo.OAuth2UserInfo;
import org.unilab.improfessorbe.global.security.oauth2.userInfo.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("=== OAuth2 사용자 로딩 시작 ===");

		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		log.info("OAuth2 Provider: {}, UserNameAttribute: {}", registrationId, userNameAttributeName);

		// OAuth2 사용자 정보 추출
		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
			oAuth2User.getAttributes(), userNameAttributeName);

		log.info("추출된 사용자 정보 - Email: {}, Provider: {}, ProviderId: {}",
			userInfo.getEmail(), userInfo.getProvider(), userInfo.getId());

		User user = processOAuth2User(userInfo);

		log.info("처리된 사용자 - UserId: {}, Email: {}", user.getUserId(), user.getEmail());

		return new CustomOAuth2User(oAuth2User, user.getUserId(), user.getEmail(),
			registrationId, userNameAttributeName);
	}

	private User processOAuth2User(OAuth2UserInfo userInfo) {
		log.info("=== processOAuth2User 시작 ===");
		log.info("입력 파라미터 - Email: {}, Provider: {}, ProviderId: {}",
			userInfo.getEmail(), userInfo.getProvider(), userInfo.getId());

		// 입력 검증
		if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
			log.error("사용자 이메일이 null 또는 빈 값입니다");
			throw new OAuth2AuthenticationException("사용자 이메일이 필요합니다");
		}

		try {
			Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(userInfo.getEmail());
			log.info("기존 사용자 조회 결과: {}", userOptional.isPresent() ? "존재" : "없음");

			if (userOptional.isPresent()) {
				User existingUser = userOptional.get();
				log.info("기존 사용자 발견 - UserId: {}, Provider: {}",
					existingUser.getUserId(), existingUser.getProvider());

				// 기존 사용자 정보 업데이트
				if (!existingUser.getProvider().equals(userInfo.getProvider())) {
					log.info("기존 사용자 provider 업데이트: {} -> {}",
						existingUser.getProvider(), userInfo.getProvider());
					existingUser.updateProvider(userInfo.getProvider());
					existingUser.updateProviderId(userInfo.getId());
				}
				return existingUser;
			} else {
				return createNewUser(userInfo);
			}
		} catch (Exception e) {
			log.error("processOAuth2User 전체 실패", e);
			throw new OAuth2AuthenticationException("사용자 처리 중 오류 발생: " + e.getMessage());
		}
	}

	private User createNewUser(OAuth2UserInfo userInfo) {
		log.info("=== 새 사용자 생성 시작 ===");

		try {
			// 닉네임 생성
			String uniqueNickname = generateUniqueNickname(userInfo.getProvider(), userInfo.getId());
			log.info("생성된 닉네임: {}", uniqueNickname);

			// 닉네임 중복 체크
			Optional<User> existingNickname = userRepository.findByNicknameAndDeletedAtIsNull(uniqueNickname);
			boolean nicknameExists = existingNickname.isPresent();
			log.info("닉네임 중복 여부: {}", nicknameExists);

			if (nicknameExists) {
				// 중복이면 timestamp 추가
				uniqueNickname = uniqueNickname + "_" + System.currentTimeMillis();
				log.info("중복으로 인한 닉네임 변경: {}", uniqueNickname);
			}

			// 사용자 객체 생성
			log.info("User.createOAuth2User 호출 시작");
			User newUser = User.createOAuth2User(uniqueNickname, userInfo.getEmail(),
				userInfo.getProvider(), userInfo.getId());
			log.info("사용자 객체 생성 완료 - Nickname: {}, Email: {}, Provider: {}",
				newUser.getNickname(), newUser.getEmail(), newUser.getProvider());

			// 데이터베이스에 저장
			log.info("데이터베이스 저장 시작");
			User savedUser = userRepository.save(newUser);
			log.info("데이터베이스 저장 완료 - UserId: {}", savedUser.getUserId());

			// 저장 후 즉시 조회로 검증
			Optional<User> verifyUser = userRepository.findById(savedUser.getUserId());
			if (verifyUser.isPresent()) {
				log.info("저장 검증 성공 - 조회된 UserId: {}", verifyUser.get().getUserId());
			} else {
				log.error("저장 검증 실패 - 저장 후 조회되지 않음");
			}

			return savedUser;

		} catch (DataIntegrityViolationException e) {
			log.error("데이터베이스 제약 조건 위반", e);
			log.error("제약 조건 위반 상세: {}", e.getMostSpecificCause().getMessage());
			throw new OAuth2AuthenticationException("사용자 생성 실패: 데이터 제약 조건 위반");

		} catch (Exception e) {
			log.error("새 사용자 생성 중 예외 발생", e);
			log.error("예외 타입: {}", e.getClass().getSimpleName());
			log.error("예외 메시지: {}", e.getMessage());
			if (e.getCause() != null) {
				log.error("원인 예외: {}", e.getCause().getMessage());
			}
			throw new OAuth2AuthenticationException("사용자 생성 실패: " + e.getMessage());
		}
	}

	private String generateUniqueNickname(String provider, String providerId) {
		try {
			String hash = DigestUtils.md5Hex(providerId).substring(0, 6);
			String nickname = provider + "_" + hash;
			log.info("생성된 기본 닉네임: {}", nickname);
			return nickname;
		} catch (Exception e) {
			log.error("닉네임 생성 실패", e);
			// fallback 닉네임
			return provider + "_" + System.currentTimeMillis();
		}
	}
}