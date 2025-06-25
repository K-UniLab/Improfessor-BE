package org.unilab.improfessorbe.domain.user.dto.response;

import org.unilab.improfessorbe.domain.user.domain.User;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponse {
	private Long userId;
	private String nickname;
	private String email;
	private String password;
	private String university;
	private String major;
	private Integer freeCount;
	private Integer recommendCount;
	private User.Role role;

	public static UserResponse toEntity(User user) {
		return UserResponse.builder()
			.userId(user.getUserId())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.password(user.getPassword())
			.university(user.getUniversity())
			.major(user.getMajor())
			.freeCount(user.getFreeCount())
			.recommendCount(user.getRecommendCount())
			.role(user.getRole())
			.build();
	}
}
