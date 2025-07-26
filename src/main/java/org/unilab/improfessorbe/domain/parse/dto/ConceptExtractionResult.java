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

	private final List<String> keywords;
	private final List<String> importantSentences;
	private final int originalTextLength;

	public boolean isSuccessful() {
		return (keywords != null && !keywords.isEmpty()) ||
			(importantSentences != null && !importantSentences.isEmpty());
	}

	public int getKeywordCount() {
		return keywords != null ? keywords.size() : 0;
	}

	public int getSentenceCount() {
		return importantSentences != null ? importantSentences.size() : 0;
	}

	public static ConceptExtractionResult empty() {
		return ConceptExtractionResult.builder()
			.keywords(Collections.emptyList())
			.importantSentences(Collections.emptyList())
			.originalTextLength(0)
			.build();
	}

	public String getSummary() {
		return String.format("키워드 %d개, 중요문장 %d개 추출 (원본 %d자)",
			getKeywordCount(), getSentenceCount(), originalTextLength);
	}

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