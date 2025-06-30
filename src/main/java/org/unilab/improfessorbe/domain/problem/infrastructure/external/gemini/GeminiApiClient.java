package org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiApiClient {

	private final WebClient geminiWebClient;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.timeout}")
	private int timeout;

	// 문제 생성 프롬프트 템플릿
	private static final String PROBLEM_GENERATION_PROMPT = """ 
		다음을 참고해서 JSON 형식으로 대학교 시험 문제 10개를 생성하세요.
		   [{"number":1,"content":"문제내용","description":"풀이과정","answer":"답"},{"number":2,"content":"문제내용","description":"풀이과정","answer":"답"}]
		   규칙:
		   - JSON 배열만 출력 (다른 텍스트 금지)
		   - 모든 값은 한 줄로 작성 (줄바꿈 금지)
		   - 따옴표 안에서 따옴표 사용 금지
		- 다음 텍스트에서 중요한 개념 위주로 문제를 만드시오: %s
		- 다음 텍스트가 존재하면 비슷한 문제 형식(객관식, 주관식, 단답식) 비율으로 만들고, 존재하지 않으면 3, 4, 3개 비율로 만드세요: %s
		""";

	public String generateProblems(String conceptText, String formatText) {
		String prompt = String.format(PROBLEM_GENERATION_PROMPT, conceptText, formatText);
		return callGemini(prompt);
	}

	private String callGemini(String prompt) {
		GeminiDto.Request request = GeminiDto.Request.builder()
			.contents(List.of(
				GeminiDto.Request.Content.builder()
					.parts(List.of(
						GeminiDto.Request.Content.Part.builder()
							.text(prompt)
							.build()
					))
					.build()
			))
			.build();

		try {
			GeminiDto.Response geminiResponse = geminiWebClient.post()
				.uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(GeminiDto.Response.class)
				.timeout(Duration.ofSeconds(timeout))
				.block();  // onErrorResume 제거!

			return extractResponseText(geminiResponse);

		} catch (WebClientResponseException.ServiceUnavailable e) {
			// 503 에러
			log.error("Gemini API 서비스 이용 불가 (503): {}", e.getResponseBodyAsString());
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);

		} catch (WebClientResponseException e) {
			// 기타 HTTP 에러
			log.error("Gemini API HTTP 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);

		} catch (Exception e) {
			// 기타 예외
			log.error("Gemini API 호출 중 예상치 못한 에러", e);
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
		}
	}

	private String extractResponseText(GeminiDto.Response response) {
		try {
			if (response == null) {
				log.warn("Gemini API 응답이 null입니다.");
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
			}

			if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
				log.warn("Gemini API 응답에 candidates가 없습니다.");
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
			}

			String responseText = response.getCandidates().get(0)
				.getContent()
				.getParts().get(0)
				.getText();

			if (responseText == null || responseText.trim().isEmpty()) {
				log.warn("Gemini API 응답 텍스트가 비어있습니다.");
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
			}

			return responseText;

		} catch (CustomException e) {
			throw e;  // CustomException 그대로 전파
		} catch (Exception e) {
			log.error("Gemini 응답 파싱 중 에러 발생", e);
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
		}
	}
}