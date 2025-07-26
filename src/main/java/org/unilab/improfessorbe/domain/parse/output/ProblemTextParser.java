package org.unilab.improfessorbe.domain.parse.output;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.domain.problem.domain.Problem;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProblemTextParser {

	public List<Problem> parseProblemText(String problemText) {
		List<Problem> problems = new ArrayList<>();

		try {
			// 1. 기본 검증
			if (problemText == null || problemText.trim().isEmpty()) {
				log.error("파싱할 문제 텍스트가 비어있습니다.");
				throw new CustomException(ErrorCode.PROBLEM_TEXT_EMPTY);
			}

			// 2. 텍스트 전처리
			String cleanText = cleanProblemText(problemText);
			log.info("파싱할 문제 텍스트 길이: {}", cleanText.length());

			// 3. JSON 파싱
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonArray = objectMapper.readTree(cleanText);

			// 4. 배열 형식 검증
			if (!jsonArray.isArray()) {
				log.error("응답이 JSON 배열 형식이 아닙니다: {}", cleanText.substring(0, Math.min(100, cleanText.length())));
				throw new CustomException(ErrorCode.PROBLEM_TEXT_NOT_JSON_ARRAY);
			}

			if (jsonArray.size() == 0) {
				log.warn("파싱된 문제가 없습니다.");
				throw new CustomException(ErrorCode.PROBLEM_TEXT_NO_PROBLEMS);
			}

			// 5. 문제 객체 생성
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonNode problemNode = jsonArray.get(i);
				Problem problem = parseSingleProblem(problemNode, i + 1);
				problems.add(problem);
			}

		} catch (CustomException e) {
			throw e;
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 에러: {}", e.getMessage());
			log.info("응답 내용: {}", problemText);
			log.info("=== Gemini 응답 전체 내용 끝 ===");
			throw new CustomException(ErrorCode.PROBLEM_JSON_PARSING_ERROR);
		} catch (Exception e) {
			log.error("문제 텍스트 파싱 중 예상치 못한 에러", e);
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}

		log.info("총 {}개의 문제가 파싱되었습니다.", problems.size());
		return problems;
	}

	private String cleanProblemText(String problemText) {
		try {
			String cleanText = problemText.trim();

			// 앞뒤 따옴표 제거
			if (cleanText.startsWith("\"") && cleanText.endsWith("\"")) {
				cleanText = cleanText.substring(1, cleanText.length() - 1);
			}

			// JSON 코드 블록 제거
			if (cleanText.startsWith("```json")) {
				cleanText = cleanText.substring(7).trim();
			}
			if (cleanText.endsWith("```")) {
				cleanText = cleanText.substring(0, cleanText.length() - 3).trim();
			}

			// 이스케이프된 따옴표 처리
			cleanText = cleanText.replace("\\\"", "'");

			cleanText = cleanText.trim();

			// 전처리 후 빈 텍스트 체크
			if (cleanText.isEmpty()) {
				throw new CustomException(ErrorCode.PROBLEM_TEXT_EMPTY);
			}

			return cleanText;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("텍스트 전처리 중 에러 발생", e);
			throw new CustomException(ErrorCode.PROBLEM_TEXT_INVALID_FORMAT);
		}
	}

	private Problem parseSingleProblem(JsonNode problemNode, int index) {
		try {
			// 필수 필드 존재 검증
			String[] requiredFields = {"number", "content", "description", "answer"};
			for (String field : requiredFields) {
				if (!problemNode.has(field) || problemNode.get(field).isNull()) {
					log.error("문제 {}에서 필수 필드 '{}' 누락", index, field);
					throw new CustomException(ErrorCode.PROBLEM_REQUIRED_FIELD_MISSING);
				}
			}

			// 필드 값 추출
			String number = problemNode.get("number").asText();
			String content = problemNode.get("content").asText();
			String description = problemNode.get("description").asText();
			String answer = problemNode.get("answer").asText();

			// 핵심 필드 빈 값 검증
			if (content.trim().isEmpty()) {
				log.error("문제 {}의 내용이 비어있습니다.", index);
				throw new CustomException(ErrorCode.PROBLEM_CONTENT_EMPTY);
			}

			if (answer.trim().isEmpty()) {
				log.error("문제 {}의 답이 비어있습니다.", index);
				throw new CustomException(ErrorCode.PROBLEM_CONTENT_EMPTY);
			}

			return Problem.create(
				"문제 " + number,
				content.trim(),
				description.trim(),
				answer.trim()
			);

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("문제 {} 파싱 중 에러 발생", index, e);
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}
}