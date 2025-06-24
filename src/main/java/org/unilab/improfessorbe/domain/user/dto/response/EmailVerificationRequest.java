package org.unilab.improfessorbe.domain.user.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String code;
}
