package org.unilab.improfessorbe.domain.problem.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.domain.problem.application.dto.CachedProblemDto;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

@Component("caffeineCache")
@Slf4j
public class CaffeineProblemCacheService implements ProblemCacheService {

	private final Cache<String, CachedProblemDto> cache = Caffeine.newBuilder()
		.maximumSize(200)  // 최대 200개 항목
		.expireAfterWrite(2, TimeUnit.HOURS)  // 2시간 후 만료
		.removalListener((key, value, cause) ->
			log.info("캐시 제거: key={}, cause={}", key, cause))
		.build();

	@Override
	public String cacheProblems(List<ProblemResponse> problems, String originalFileName) {
		String cacheKey = UUID.randomUUID().toString();
		CachedProblemDto data = CachedProblemDto.builder()
			.problems(problems)
			.originalFileName(originalFileName)
			.createdAt(LocalDateTime.now())
			.build();

		cache.put(cacheKey, data);
		log.info("문제 캐시 저장: key={}, 문제수={}", cacheKey, problems.size());

		return cacheKey;
	}

	@Override
	public CachedProblemDto getCachedProblems(String cacheKey) {
		CachedProblemDto data = cache.getIfPresent(cacheKey);
		if (data == null) {
			log.warn("캐시에서 문제를 찾을 수 없음: key={}", cacheKey);
			throw new CustomException(ErrorCode.ELEMENT_NOT_FOUND);
		}
		return data;
	}
}