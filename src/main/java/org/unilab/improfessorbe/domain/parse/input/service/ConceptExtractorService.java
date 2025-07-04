/*
package org.unilab.improfessorbe.domain.parse.input.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unilab.improfessorbe.domain.parse.dto.ConceptExtractionResult;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.summary.TextRankSentence;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConceptExtractorService {

	public ConceptExtractionResult extractConcepts(String text) {
		return extractConcepts(text, 50, 50);
	}

	public ConceptExtractionResult extractConcepts(String text, int keywordCount, int sentenceCount) {
		if (!StringUtils.hasText(text)) {
			log.warn("빈 텍스트가 입력되었습니다.");
			return ConceptExtractionResult.empty();
		}

		int originalTextLength = text.length();
		log.info("개념 추출 시작 - 전처리 전 텍스트 길이: {}자, 요청 키워드: {}개, 요청 문장: {}개",
			originalTextLength, keywordCount, sentenceCount);

		List<String> keywords = extractKeywords(text, keywordCount);
		List<String> importantSentences = extractImportantSentences(text, sentenceCount);

		ConceptExtractionResult result = ConceptExtractionResult.builder()
			.keywords(keywords)
			.importantSentences(importantSentences)
			.originalTextLength(text.length())
			.build();

		log.info("개념 추출 완료");
		return result;
	}

	public List<String> extractKeywords(String text, int count) {
		if (!StringUtils.hasText(text)) {
			log.warn("빈 텍스트가 입력되었습니다.");
			return Collections.emptyList();
		}

		try {
			List<String> keywords = HanLP.extractKeyword(text, count);
			log.info("키워드 {}개 추출 완료", keywords.size());
			return keywords;
		} catch (Exception e) {
			log.error("키워드 추출 중 오류 발생: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public List<String> extractImportantSentences(String text, int count) {
		if (!StringUtils.hasText(text)) {
			log.warn("빈 텍스트가 입력되었습니다.");
			return Collections.emptyList();
		}

		try {
			List<String> sentences = TextRankSentence.getTopSentenceList(text, count);

			// 빈 문장이나 너무 짧은 문장 필터링
			List<String> filteredSentences = sentences.stream()
				.filter(sentence -> StringUtils.hasText(sentence) && sentence.trim().length() > 10)
				.collect(Collectors.toList());

			log.info("중요 문장 {}개 추출 완료", filteredSentences.size());
			return filteredSentences;
		} catch (Exception e) {
			log.error("문장 추출 중 오류 발생: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
}*/
