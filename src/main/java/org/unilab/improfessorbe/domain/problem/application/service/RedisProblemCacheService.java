package org.unilab.improfessorbe.domain.problem.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.domain.problem.application.dto.CachedProblemDto;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("redisCache")
@Slf4j
@RequiredArgsConstructor
@Primary
public class RedisProblemCacheService implements ProblemCacheService {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String CACHE_PREFIX = "problem:cache:";
	private static final Duration CACHE_DURATION = Duration.ofHours(2); // 2시간 후 만료

	@Override
	public String cacheProblems(List<ProblemResponse> problems, String originalFileName) {
		String cacheKey = UUID.randomUUID().toString();
		String redisKey = CACHE_PREFIX + cacheKey;

		CachedProblemDto data = CachedProblemDto.builder()
			.problems(problems)
			.originalFileName(originalFileName)
			.createdAt(LocalDateTime.now())
			.build();

		try {
			// 객체를 JSON 문자열로 직렬화
			String jsonData = objectMapper.writeValueAsString(data);

			// Redis에 저장 (TTL 설정)
			redisTemplate.opsForValue().set(redisKey, jsonData, CACHE_DURATION);

			log.info("문제 캐시 저장: key={}, 문제수={}", cacheKey, problems.size());

			return cacheKey;

		} catch (JsonProcessingException e) {
			log.error("캐시 데이터 직렬화 실패: key={}", cacheKey, e);
			throw new CustomException(ErrorCode.CACHE_SERIALIZATION_ERROR);
		} catch (Exception e) {
			log.error("Redis 캐시 저장 실패: key={}", cacheKey, e);
			throw new CustomException(ErrorCode.CACHE_OPERATION_FAILED);
		}
	}

	@Override
	public CachedProblemDto getCachedProblems(String cacheKey) {
		String redisKey = CACHE_PREFIX + cacheKey;

		try {
			String jsonData = redisTemplate.opsForValue().get(redisKey);

			if (jsonData == null) {
				log.warn("캐시에서 문제를 찾을 수 없음: key={}", cacheKey);
				throw new CustomException(ErrorCode.DOWNLOAD_LINK_EXPIRED);
			}

			// JSON 문자열을 객체로 역직렬화
			CachedProblemDto data = objectMapper.readValue(jsonData, CachedProblemDto.class);

			log.debug("캐시 조회 성공: key={}, 문제수={}", cacheKey, data.getProblems().size());

			return data;

		} catch (CustomException e) {
			throw e;
		} catch (JsonProcessingException e) {
			log.error("캐시 데이터 역직렬화 실패: key={}", cacheKey, e);
			throw new CustomException(ErrorCode.CACHE_DESERIALIZATION_ERROR);
		} catch (Exception e) {
			log.error("Redis 캐시 조회 실패: key={}", cacheKey, e);
			throw new CustomException(ErrorCode.CACHE_OPERATION_FAILED);
		}
	}
}