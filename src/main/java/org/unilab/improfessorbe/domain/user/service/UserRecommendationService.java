package org.unilab.improfessorbe.domain.user.service;

import org.springframework.stereotype.Service;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.domain.user.infrastructure.repository.UserRepository;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRecommendationService {

	private final UserRepository userRepository;

	public void processRecommendation(Long userId, String recommendNickname) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 사용자가 이미 추천했는지 확인
		if (!user.canRecommend()) {
			throw new CustomException(ErrorCode.ALREADY_RECOMMENDED);
		}

		// 닉네임으로 추천인 찾기
		User recommender = userRepository.findByNicknameAndDeletedAtIsNull(recommendNickname)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_RECOMMEND_NICKNAME));

		// 자기 자신 추천 방지
		if (user.getUserId().equals(recommender.getUserId())) {
			throw new CustomException(ErrorCode.SELF_RECOMMENDATION_NOT_ALLOWED);
		}

		// 추천인이 추천 받을 수 있는지 확인
		if (!recommender.canReceiveRecommendation()) {
			throw new CustomException(ErrorCode.RECOMMEND_LIMIT_EXCEEDED);
		}

		try {
			// 추천 처리
			user.recommendUser();
			recommender.receiveRecommend();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.RECOMMEND_PROCESSING_FAILED);
		}
	}
}
