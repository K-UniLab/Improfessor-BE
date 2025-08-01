package org.unilab.improfessorbe.domain.user.domain;

import org.unilab.improfessorbe.global.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	private String nickname;

	private String email;

	private String password;

	private String university;

	private String major;

	private Integer freeCount;

	private Integer recommendCount;

	private boolean isRecommend;

	@Enumerated(value = EnumType.STRING)
	private Role role;

	// OAuth2 관련 필드 추가
	private String provider;        // "kakao", "google", "naver" 등

	private String providerId;      // OAuth2 제공자의 사용자 ID

	public enum Role {
		ADMIN, USER
	}

	private User(String nickname, String email, String password, String university, String major) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.university = university;
		this.major = major;
		this.freeCount = 5;
		this.recommendCount = 33;
		this.role = Role.USER;
		this.isRecommend = false;
		this.provider = "local";  // 일반 회원가입은 "local"
		this.providerId = null;
	}

	// OAuth2 회원가입용 생성자
	private User(String nickname, String email, String provider, String providerId) {
		this.nickname = nickname;
		this.email = email;
		this.password = null; // OAuth2 사용자는 비밀번호 없음
		this.university = null; // 나중에 추가 정보 입력
		this.major = null; // 나중에 추가 정보 입력
		this.freeCount = 5;
		this.recommendCount = 33;
		this.role = Role.USER;
		this.isRecommend = false;
		this.provider = provider;
		this.providerId = providerId;
	}

	public static User create(String nickname, String email, String password, String university, String major) {
		return new User(nickname, email, password, university, major);
	}

	// OAuth2 회원가입용 팩토리 메서드
	public static User createOAuth2User(String nickname, String email, String provider, String providerId) {
		return new User(nickname, email, provider, providerId);
	}

	public void updateUser(String university, String major) {
		this.university = university;
		this.major = major;
	}

	public void decrementFreeCount() {
		this.freeCount--;
	}

	public void receiveRecommend() {
		this.freeCount += 3;
		this.recommendCount--;
	}

	public void recommendUser() {
		this.freeCount++;
		this.isRecommend = true;
	}

	public boolean canReceiveRecommendation() {
		return this.recommendCount > 0;
	}

	public boolean canRecommend() {
		return !this.isRecommend;
	}

	// OAuth2 관련 업데이트 메서드들
	public void updateProvider(String provider) {
		this.provider = provider;
	}

	public void updateProviderId(String providerId) {
		this.providerId = providerId;
	}

	// OAuth2 사용자인지 확인하는 메서드
	public boolean isOAuth2User() {
		return provider != null && !provider.equals("local");
	}

	// 소셜 로그인 사용자는 비밀번호가 없을 수 있으므로 체크 메서드
	public boolean hasPassword() {
		return password != null && !password.isEmpty();
	}
}
