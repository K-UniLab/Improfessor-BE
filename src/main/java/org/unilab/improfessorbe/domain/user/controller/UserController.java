package org.unilab.improfessorbe.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unilab.improfessorbe.domain.user.dto.request.EmailVerificationResponse;
import org.unilab.improfessorbe.domain.user.dto.response.EmailVerificationRequest;
import org.unilab.improfessorbe.domain.user.service.UserService;
import org.unilab.improfessorbe.global.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/email/send-verification")
	public ResponseEntity<ApiResponse<Void>> sendVerificationEmail(
		@RequestParam @Valid @Email String email
	) {
		userService.sendVerificationEmail(email);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@PostMapping("/email/verify")
	public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
		@RequestBody @Valid EmailVerificationRequest emailVerificationRequest
	){
		EmailVerificationResponse emailVerificationResponse = userService.verifyEmail(emailVerificationRequest.getEmail(), emailVerificationRequest.getCode());
		return ResponseEntity.ok(ApiResponse.success(emailVerificationResponse));
	}

}
