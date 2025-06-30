package org.unilab.improfessorbe.domain.parse.dto;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ConceptExtractionResult {

	// 추출된 키워드 목록
	private final List<String> keywords;
	// 추출된 문장 목록
	private final List<String> importantSentences;
	//전처리 전 텍스트 길이
	private final int originalTextLength;

	//추출 성공 여부
	public boolean isSuccessful() {
		return (keywords != null && !keywords.isEmpty()) ||
			(importantSentences != null && !importantSentences.isEmpty());
	}

	//추출된 키워드 개수
	public int getKeywordCount() {
		return keywords != null ? keywords.size() : 0;
	}

	//추출된 문장 개수
	public int getSentenceCount() {
		return importantSentences != null ? importantSentences.size() : 0;
	}

	//빈 결과 객체 생성
	public static ConceptExtractionResult empty() {
		return ConceptExtractionResult.builder()
			.keywords(Collections.emptyList())
			.importantSentences(Collections.emptyList())
			.originalTextLength(0)
			.build();
	}

	//결과 요약 정보
	public String getSummary() {
		return String.format("키워드 %d개, 중요문장 %d개 추출 (원본 %d자)",
			getKeywordCount(), getSentenceCount(), originalTextLength);
	}

	//String으로 변경
	public String toFormattedString() {
		if (!isSuccessful()) {
			return "개념 추출에 실패했습니다.";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("중요한 키워드: ");
		sb.append(keywords != null && !keywords.isEmpty() ?
			String.join(", ", keywords) : "키워드를 찾을 수 없습니다.");
		sb.append(" / ");
		sb.append("중요한 문장: ");
		sb.append(importantSentences != null && !importantSentences.isEmpty() ?
			String.join(" | ", importantSentences) : "중요한 문장을 찾을 수 없습니다.");

		return sb.toString();
	}
}