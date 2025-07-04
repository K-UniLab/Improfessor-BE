package org.unilab.improfessorbe.domain.user.dto.request;

import org.unilab.improfessorbe.domain.user.domain.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

	@NotBlank
	private String nickname;

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Size(min = 4, max = 12, message = "비밀번호는 4~12자 사이여야 합니다.")
	private String password;

	private String university;

	private String major;

	public static User toEntity(UserRegisterRequest userRegisterRequest, String encodedPassword) {
		return User.create(
			userRegisterRequest.getNickname(),
			userRegisterRequest.getEmail(),
			encodedPassword,
			userRegisterRequest.getUniversity(),
			userRegisterRequest.getMajor()
		);
	}

}
