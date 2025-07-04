package org.unilab.improfessorbe.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

	private Long id;

	@NotBlank
	@Size(min = 4, max = 12, message = "비밀번호는 4~12자 사이여야 합니다.")
	private String password;

	private String university;

	private String major;

	private Integer recommendCount;

}
