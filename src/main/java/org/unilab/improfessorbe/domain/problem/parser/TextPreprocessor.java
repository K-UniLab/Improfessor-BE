package org.unilab.improfessorbe.domain.problem.parser;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class TextPreprocessor {

	/**
	 * 파싱된 텍스트를 정제합니다
	 */
	public String preprocess(String rawText) {
		if (rawText == null || rawText.trim().isEmpty()) {
			return "";
		}

		return rawText
			// 1. OCR 오류 수정 (띄어쓰기가 잘못된 영단어들)
			.replaceAll("([a-zA-Z])\\s+([a-zA-Z])(?=\\s|$)", "$1$2")
			// 2. 특수 문자와 개행 정리
			.replaceAll("\\r\\n", "\n")
			.replaceAll("\\r", "\n")
			// 3. 중복 개행 제거 (3개 이상의 연속 개행을 2개로)
			.replaceAll("\\n{3,}", "\n\n")
			// 4. 중복 공백 제거
			.replaceAll("[ \\t]+", " ")
			// 5. 라인별 앞뒤 공백 제거
			.lines()
			.map(String::trim)
			.filter(line -> !line.isEmpty())
			.collect(Collectors.joining("\n"))
			.trim();
	}

	/**
	 * 슬라이드 구분자 정리
	 */
	public String cleanSlideContent(String content, String separator) {
		return content
			// 빈 슬라이드 제거
			.replaceAll(separator + "\\s*" + separator, separator)
			// 마지막 구분자 제거
			.replaceAll(separator + "\\s*$", "")
			.trim();
	}
}