package org.unilab.improfessorbe.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unilab.improfessorbe.domain.user.dto.request.EmailVerificationResponse;
import org.unilab.improfessorbe.domain.user.dto.request.UserLoginRequest;
import org.unilab.improfessorbe.domain.user.dto.request.UserRegisterRequest;
import org.unilab.improfessorbe.domain.user.dto.request.UserUpdateRequest;
import org.unilab.improfessorbe.domain.user.dto.response.EmailVerificationRequest;
import org.unilab.improfessorbe.domain.user.dto.response.UserLoginResponse;
import org.unilab.improfessorbe.domain.user.dto.response.UserResponse;
import org.unilab.improfessorbe.domain.user.service.UserService;
import org.unilab.improfessorbe.global.common.ApiResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Void>> register(
		@RequestBody @Valid UserRegisterRequest userRegisterRequest
	){
		userService.register(userRegisterRequest);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<UserLoginResponse>> login(
		@RequestBody @Valid UserLoginRequest userLoginRequest
	) {
		UserLoginResponse loginResponse = userService.login(userLoginRequest);
		return ResponseEntity.ok(ApiResponse.success(loginResponse));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
		HttpServletRequest request
	) {
		String accessToken = extractTokenFromRequest(request);
		userService.logout(accessToken);
		return ResponseEntity.ok(ApiResponse.success());
	}

	private String extractTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}


	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<Void>> updateUser(
		@RequestBody @Valid UserUpdateRequest userUpdateRequest
	) {
		userService.updateUser(userUpdateRequest);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(
		@PathVariable Long userId
	){
		UserResponse userResponse = userService.getUser(userId);
		return ResponseEntity.ok(ApiResponse.success(userResponse));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse<UserResponse>> deleteUser(
		@PathVariable Long userId
	){
		userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponse.success());
	}

}
