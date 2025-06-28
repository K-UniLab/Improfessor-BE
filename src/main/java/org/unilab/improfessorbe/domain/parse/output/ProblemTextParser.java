package org.unilab.improfessorbe.domain.parse.output;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProblemTextParser {

	public List<Problem> parseProblemText(String problemText) {
		List<Problem> problems = new ArrayList<>();

		try {
			// 앞뒤 따옴표 제거
			String cleanText = problemText.trim();
			if (cleanText.startsWith("\"") && cleanText.endsWith("\"")) {
				cleanText = cleanText.substring(1, cleanText.length() - 1);
			}

			log.info("파싱할 문제 텍스트 길이: {}", cleanText.length());

			// JSON 형식 확인 및 추출
			if (cleanText.startsWith("```json")) {
				cleanText = cleanText.substring(7); // ```json 제거
			}
			if (cleanText.endsWith("```")) {
				cleanText = cleanText.substring(0, cleanText.length() - 3); // ``` 제거
			}
			cleanText = cleanText.trim();

			// 이스케이프된 따옴표만 처리 (줄바꿈은 JSON 파싱 후에 처리)
			cleanText = cleanText.replace("\\\"", "\"");

			// JSON 파싱을 위해 ObjectMapper 사용 (Jackson)
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonArray = objectMapper.readTree(cleanText);

			if (jsonArray.isArray()) {
				for (JsonNode problemNode : jsonArray) {
					String number = problemNode.get("number").asText();
					String content = problemNode.get("content").asText();
					String description = problemNode.get("description").asText();
					String answer = problemNode.get("answer").asText();

					Problem problem = Problem.create(
						"문제 " + number,
						content,
						description,
						answer
					);

					problems.add(problem);
					log.info("문제 {} 파싱 완료", number);
				}
			}

		} catch (Exception e) {
			log.error("JSON 파싱 실패: {}", e.getMessage());
			throw new RuntimeException("문제 텍스트 파싱에 실패했습니다: " + e.getMessage(), e);
		}

		log.info("총 {}개의 문제가 파싱되었습니다.", problems.size());
		return problems;
	}
}