package org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
		다음을 참고해서 JSON 형식으로 대학교 시험 문제 5개를 생성하세요.
		   [{"number":1,"content":"문제내용","description":"풀이과정","answer":"답"},{"number":2,"content":"문제내용","description":"풀이과정","answer":"답"}]
		   규칙:
		   - JSON 배열만 출력 (다른 텍스트 금지)
		   - 모든 값은 한 줄로 작성 (줄바꿈 금지)
		   - 따옴표 안에서 따옴표 사용 금지
		- 다음 텍스트에서 중요한 개념 위주로 문제를 만드시오: %s
		- 다음 텍스트와 비슷한 문제 형식, (객관식, 주관식, 단답식) 비율으로 만드시오: %s
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
				.onErrorResume(e -> {
					log.error("Gemini API 호출 실패", e);
					return Mono.empty();
				})
				.block();

			return extractResponseText(geminiResponse);

		} catch (Exception e) {
			log.error("Gemini 호출 중 오류 발생", e);
			return "죄송합니다. 현재 응답을 생성할 수 없습니다.";
		}
	}

	private String extractResponseText(GeminiDto.Response response) {
		try {
			if (response == null ||
				response.getCandidates() == null ||
				response.getCandidates().isEmpty()) {
				return "응답을 받을 수 없습니다.";
			}

			return response.getCandidates().get(0)
				.getContent()
				.getParts().get(0)
				.getText();

		} catch (Exception e) {
			log.error("Gemini 응답 파싱 실패", e);
			return "응답을 처리할 수 없습니다.";
		}
	}
}