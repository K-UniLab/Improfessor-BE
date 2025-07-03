package org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiRateLimitManager {

	private final RedisTemplate<String, String> redisTemplate;

	// 토큰 버켓 설정
	private static final String TOKENS_KEY = "gemini:tokens";
	private static final String LAST_REFILL_KEY = "gemini:last_refill";
	private static final String DAILY_COUNT_KEY = "gemini:daily:count";

	private static final int BUCKET_CAPACITY = 15;           // 버켓 최대 용량 (분당 15회)
	private static final double REFILL_RATE = 15.0 / 60.0;  // 초당 토큰 충전율 (0.25개/초)
	private static final int MAX_DAILY_REQUESTS = 200;      // 일일 최대 요청

	// Lua 스크립트로 원자적 연산 보장
	private static final String TOKEN_BUCKET_LUA_SCRIPT = """
		local tokens_key = KEYS[1]
		local last_refill_key = KEYS[2]
		local current_time = tonumber(ARGV[1])
		local capacity = tonumber(ARGV[2])
		local refill_rate = tonumber(ARGV[3])
		        
		-- 현재 토큰 수와 마지막 충전 시간 조회
		local current_tokens = tonumber(redis.call('GET', tokens_key) or capacity)
		local last_refill = tonumber(redis.call('GET', last_refill_key) or current_time)
		        
		-- 시간 경과에 따른 토큰 충전 계산
		local time_passed = (current_time - last_refill) / 1000.0
		local tokens_to_add = time_passed * refill_rate
		local new_tokens = math.min(capacity, current_tokens + tokens_to_add)
		        
		-- 토큰이 있다면 소비
		if new_tokens >= 1 then
		    new_tokens = new_tokens - 1
		    redis.call('SET', tokens_key, new_tokens)
		    redis.call('SET', last_refill_key, current_time)
		    redis.call('EXPIRE', tokens_key, 3600)
		    redis.call('EXPIRE', last_refill_key, 3600)
		    return 1  -- 성공
		else
		    return 0  -- 실패 (토큰 부족)
		end
		""";

	public void checkRateLimit() {
		// 1. 토큰 버켓으로 분당 제한 확인
		if (!tryConsumeToken()) {
			log.warn("Gemini API 분당 요청 제한 초과 - 토큰 부족");
			throw new CustomException(ErrorCode.GEMINI_RATE_LIMIT_EXCEEDED);
		}

		// 2. 일일 제한 확인
		String dailyCount = redisTemplate.opsForValue().get(DAILY_COUNT_KEY);
		if (dailyCount != null && Integer.parseInt(dailyCount) >= MAX_DAILY_REQUESTS) {
			log.warn("Gemini API 일일 요청 제한 초과: {}/{}", dailyCount, MAX_DAILY_REQUESTS);

			// 토큰을 다시 추가 (일일 제한으로 인한 실패이므로)
			restoreToken();

			throw new CustomException(ErrorCode.GEMINI_DAILY_LIMIT_EXCEEDED);
		}
	}

	public void incrementRequestCount() {
		// 일일 카운트만 증가 (분당 제한은 토큰 버켓에서 처리됨)
		redisTemplate.opsForValue().increment(DAILY_COUNT_KEY);
		redisTemplate.expire(DAILY_COUNT_KEY, Duration.ofHours(24));

		log.debug("Gemini API 일일 요청 카운트 증가");
	}

	/**
	 * 토큰 버켓에서 토큰 1개 소비 시도
	 * @return 성공시 true, 토큰 부족시 false
	 */
	private boolean tryConsumeToken() {
		long currentTime = System.currentTimeMillis();

		Long result = redisTemplate.execute(
			RedisScript.of(TOKEN_BUCKET_LUA_SCRIPT, Long.class),
			List.of(TOKENS_KEY, LAST_REFILL_KEY),
			String.valueOf(currentTime),
			String.valueOf(BUCKET_CAPACITY),
			String.valueOf(REFILL_RATE)
		);

		boolean success = result != null && result == 1;

		if (success) {
			log.debug("토큰 소비 성공 - 현재 시각: {}", currentTime);
		} else {
			log.debug("토큰 부족 - 요청 거부");
		}

		return success;
	}

	/**
	 * 일일 제한으로 인한 실패 시 토큰 복구
	 */
	private void restoreToken() {
		String restoreLuaScript = """
			local tokens_key = KEYS[1]
			local capacity = tonumber(ARGV[1])
			            
			local current_tokens = tonumber(redis.call('GET', tokens_key) or 0)
			local restored_tokens = math.min(capacity, current_tokens + 1)
			            
			redis.call('SET', tokens_key, restored_tokens)
			redis.call('EXPIRE', tokens_key, 3600)
			            
			return restored_tokens
			""";

		redisTemplate.execute(
			RedisScript.of(restoreLuaScript, Long.class),
			List.of(TOKENS_KEY),
			String.valueOf(BUCKET_CAPACITY)
		);

		log.debug("일일 제한으로 인한 토큰 복구 완료");
	}

	/**
	 * 현재 토큰 버켓 상태 조회 (디버깅/모니터링 용도)
	 */
	public TokenBucketStatus getTokenBucketStatus() {
		String tokensStr = redisTemplate.opsForValue().get(TOKENS_KEY);
		String lastRefillStr = redisTemplate.opsForValue().get(LAST_REFILL_KEY);
		String dailyCountStr = redisTemplate.opsForValue().get(DAILY_COUNT_KEY);

		double currentTokens = tokensStr != null ? Double.parseDouble(tokensStr) : BUCKET_CAPACITY;
		long lastRefill = lastRefillStr != null ? Long.parseLong(lastRefillStr) : System.currentTimeMillis();
		int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;

		return TokenBucketStatus.builder()
			.currentTokens(currentTokens)
			.maxTokens(BUCKET_CAPACITY)
			.lastRefillTime(lastRefill)
			.dailyRequestCount(dailyCount)
			.maxDailyRequests(MAX_DAILY_REQUESTS)
			.build();
	}

	// 토큰 버켓 상태 정보 클래스
	@lombok.Builder
	@lombok.Data
	public static class TokenBucketStatus {
		private double currentTokens;
		private int maxTokens;
		private long lastRefillTime;
		private int dailyRequestCount;
		private int maxDailyRequests;

		public boolean canMakeRequest() {
			return currentTokens >= 1 && dailyRequestCount < maxDailyRequests;
		}

		public double getTokenRefillProgress() {
			long timeSinceRefill = System.currentTimeMillis() - lastRefillTime;
			double tokensToAdd = (timeSinceRefill / 1000.0) * REFILL_RATE;
			return Math.min(1.0, (currentTokens + tokensToAdd) / maxTokens);
		}
	}
}