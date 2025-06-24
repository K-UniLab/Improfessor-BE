package org.unilab.improfessorbe.domain.user.dto.request;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationResponse {
	Boolean verified;
	String message;
}
