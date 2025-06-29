package org.unilab.improfessorbe.domain.user.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.domain.user.dto.request.EmailVerificationResponse;
import org.unilab.improfessorbe.domain.user.dto.request.UserRegisterRequest;
import org.unilab.improfessorbe.domain.user.dto.request.UserUpdateRequest;
import org.unilab.improfessorbe.domain.user.dto.response.UserResponse;
import org.unilab.improfessorbe.domain.user.repository.UserRepository;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;
import org.unilab.improfessorbe.global.util.RedisUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

	private final EmailService emailService;
	private final UserRepository userRepository;
	private final RedisUtil redisUtil;

	private final Long EXPIRATION = 10 * 60L;

	public void sendVerificationEmail(String email) {
		validateDuplicateEmail(email);

		String title = "나는 교수다 서비스 회원가입 인증 메일";
		String code = UUID.randomUUID().toString();
		log.info("code: {}", code);
		String text = "인증번호: " + code;

		redisUtil.setDataExpire(email, code, EXPIRATION);

		try{
			emailService.sendEmail(email, title, text);
		} catch (Exception e) {
			log.error("Error: {}", e);
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
		}

	}

	public EmailVerificationResponse verifyEmail(String email, String code) {
		if(redisUtil.existData(email)){
			String result = redisUtil.getData(email);
			if(result.equals(code)){
				return EmailVerificationResponse.builder().verified(true).message("인증 성공하였습니다.").build();
			}
			else{
				return EmailVerificationResponse.builder().verified(false).message(result).message("인증번호가 일치하지 않습니다").build();
			}
		}
		else{
			return EmailVerificationResponse.builder().verified(false).message("인증번호가 만료되었습니다. 다시 시도해주세요.").build();
		}
	}

	@Transactional
	public void register(UserRegisterRequest userRegisterRequest) {
		validateDuplicateNickname(userRegisterRequest.getNickname());
		User user = UserRegisterRequest.toEntity(userRegisterRequest);
		userRepository.save(user);
	}

	@Transactional
	public void updateUser(UserUpdateRequest userUpdateRequest) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(userUpdateRequest.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		user.updateUser(
			userUpdateRequest.getPassword(), userUpdateRequest.getUniversity(), userUpdateRequest.getMajor(),
			userUpdateRequest.getFreeCount(), userUpdateRequest.getRecommendCount()
		);
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(Long userId) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		return UserResponse.of(user);
	}

	@Transactional
	public void deleteUser(Long userId) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		user.markAsDeleted();
	}


	private void validateDuplicateEmail(String email) {
		Optional<User> user = userRepository.findByEmailAndDeletedAtIsNull(email);
		if(user.isPresent()) {
			throw new CustomException(ErrorCode.EMAIL_DUPLICATION);
		}
	}

	private void validateDuplicateNickname(String nickname) {
		Optional<User> user = userRepository.findByNicknameAndDeletedAtIsNull(nickname);
		if(user.isPresent()) {
			throw new CustomException(ErrorCode.NICKNAME_DUPLICATION);
		}
	}
}
