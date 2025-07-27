package org.unilab.improfessorbe.domain.problem.service.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unilab.improfessorbe.domain.problem.dto.ConceptExtractionResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConceptExtractorService {

	private Set<String> stopwords;

	public ConceptExtractorService() {
		this.stopwords = loadStopwords();
	}

	public ConceptExtractionResult extractConcepts(String text) {
		return extractConcepts(text, 12, 50);
	}

	public ConceptExtractionResult extractConcepts(String text, int keywordCount, int sentenceCount) {
		if (!StringUtils.hasText(text)) {
			log.warn("빈 텍스트가 입력되었습니다.");
			return ConceptExtractionResult.empty();
		}

		long startTime = System.currentTimeMillis();
		int originalTextLength = text.length();
		log.info("개념 추출 시작 - 전처리 전 텍스트 길이: {}자, 요청 키워드: {}개, 요청 문장: {}개",
			originalTextLength, keywordCount, sentenceCount);

		// 텍스트 전처리
		String preprocessedText = preprocessText(text);

		// 키워드 추출
		long keywordStartTime = System.currentTimeMillis();
		List<String> keywords = extractKeywords(preprocessedText, keywordCount);
		long keywordTime = System.currentTimeMillis() - keywordStartTime;
		log.info("키워드 추출 완료 - {}개 추출, 소요시간: {}ms", keywords.size(), keywordTime);

		// 문장 추출
		long sentenceStartTime = System.currentTimeMillis();
		List<String> importantSentences = extractImportantSentences(preprocessedText, sentenceCount);
		long sentenceTime = System.currentTimeMillis() - sentenceStartTime;
		log.info("문장 추출 완료 - {}개 추출, 소요시간: {}ms", importantSentences.size(), sentenceTime);

		ConceptExtractionResult result = ConceptExtractionResult.builder()
			.keywords(keywords)
			.importantSentences(importantSentences)
			.originalTextLength(text.length())
			.build();

		long totalTime = System.currentTimeMillis() - startTime;
		log.info("개념 추출 완료 - 총 소요시간: {}ms (키워드: {}ms, 문장: {}ms)",
			totalTime, keywordTime, sentenceTime);
		return result;
	}

	public List<String> extractKeywords(String text, int count) {
		if (!StringUtils.hasText(text)) {
			log.warn("빈 텍스트가 입력되었습니다.");
			return Collections.emptyList();
		}

		try {
			String preprocessedText = preprocessText(text);
			List<String> tokens = tokenize(preprocessedText, stopwords);
			List<String> keywords = extractKeywordsUsingTextRank(tokens, count);

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
			String preprocessedText = preprocessText(text);
			List<String> sentences = splitIntoSentences(preprocessedText);
			List<String> importantSentences = extractImportantSentencesUsingTextRank(sentences, stopwords, count);

			// 빈 문장이나 너무 짧은 문장 필터링
			List<String> filteredSentences = importantSentences.stream()
				.filter(sentence -> StringUtils.hasText(sentence) && sentence.trim().length() > 10)
				.collect(Collectors.toList());

			return filteredSentences;
		} catch (Exception e) {
			log.error("문장 추출 중 오류 발생: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	// 불용어 파일을 로딩하여 Set으로 반환
	private Set<String> loadStopwords() {
		Set<String> stopwords = new HashSet<>();
		try {
			// 한국어 불용어
			URL ko = new URL("https://raw.githubusercontent.com/stopwords-iso/stopwords-ko/master/stopwords-ko.txt");
			try (BufferedReader r = new BufferedReader(new InputStreamReader(ko.openStream()))) {
				r.lines().map(String::trim).filter(l -> !l.isEmpty()).forEach(stopwords::add);
			}

			// 영어 불용어
			URL en = new URL("https://raw.githubusercontent.com/stopwords-iso/stopwords-en/master/stopwords-en.txt");
			try (BufferedReader r = new BufferedReader(new InputStreamReader(en.openStream()))) {
				r.lines().map(String::trim).filter(l -> !l.isEmpty()).forEach(stopwords::add);
			}

			log.info("불용어 {}개 로딩 완료", stopwords.size());
		} catch (IOException e) {
			log.error("불용어 로딩 실패: {}", e.getMessage());
			// 기본 불용어 추가
			stopwords.addAll(
				Arrays.asList("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
					"이", "그", "저", "것", "수", "있", "하", "되", "않", "없", "만", "더", "잘", "통해", "위해", "같은", "다른", "새로운",
					"이런", "그런", "저런"));
		}
		return stopwords;
	}

	// 텍스트 전처리
	private String preprocessText(String text) {
		return text
			.replaceAll("[\\r\\n\\t]+", " ")
			.replaceAll("(?m)^(import|from)\\b.*", " ")
			.replace("\\r", " ").replace("\\n", " ")
			.trim();
	}

	// 텍스트를 정제하고 불용어 제거하여 토큰 리스트 생성
	private List<String> tokenize(String text, Set<String> stopwords) {
		return Arrays.stream(text
				.replaceAll("[^가-힣a-zA-Z\\s]", " ")
				.split("\\s+"))
			.map(String::toLowerCase)
			.filter(t -> t.length() > 1 && !stopwords.contains(t))
			.collect(Collectors.toList());
	}

	// 키워드를 정규화하여 유사어를 통일
	private String normalizeKeyword(String word) {
		return word
			.replaceAll("(ing|ed|es|s)$", "")
			.replaceAll("(ion|ment|ness)$", "")
			.replaceAll("dataset", "data")
			.replaceAll("training", "train")
			.replaceAll("validation", "validate")
			.replaceAll("modeling", "model")
			.replaceAll("set", "data");
	}

	// TextRank 알고리즘으로 핵심 키워드 추출
	private List<String> extractKeywordsUsingTextRank(List<String> tokens, int topN) {
		Map<String, Integer> idx = new HashMap<>();
		List<String> uniq = new ArrayList<>();

		for (String t : tokens) {
			if (!idx.containsKey(t)) {
				idx.put(t, uniq.size());
				uniq.add(t);
			}
		}

		int n = uniq.size();
		if (n == 0)
			return Collections.emptyList();

		double[][] M = new double[n][n];
		int window = 4;

		for (int i = 0; i < tokens.size(); i++) {
			int wi = idx.get(tokens.get(i));
			for (int j = i + 1; j < tokens.size() && j < i + window; j++) {
				int wj = idx.get(tokens.get(j));
				M[wi][wj] += 1;
				M[wj][wi] += 1;
			}
		}

		double[] score = new double[n];
		Arrays.fill(score, 1.0);
		double d = 0.85;

		for (int it = 0; it < 30; it++) {
			double[] next = new double[n];
			for (int i = 0; i < n; i++) {
				double sum = 0;
				for (int j = 0; j < n; j++) {
					if (M[j][i] > 0) {
						double norm = Arrays.stream(M[j]).sum();
						sum += (M[j][i] / (norm > 0 ? norm : 1)) * score[j];
					}
				}
				next[i] = (1 - d) + d * sum;
			}
			score = next;
		}

		// 정규화하여 키워드 점수 계산
		Map<String, Double> normalizedScores = new HashMap<>();
		Map<String, String> normToOriginal = new HashMap<>();

		for (int i = 0; i < n; i++) {
			String orig = uniq.get(i);
			String norm = normalizeKeyword(orig);
			if (!normalizedScores.containsKey(norm) || score[i] > normalizedScores.get(norm)) {
				normalizedScores.put(norm, score[i]);
				normToOriginal.put(norm, orig);
			}
		}

		return normalizedScores.entrySet().stream()
			.sorted(Map.Entry.<String, Double>comparingByValue().reversed())
			.limit(topN)
			.map(e -> normToOriginal.get(e.getKey()))
			.collect(Collectors.toList());
	}

	// 텍스트를 문장 단위로 분리
	private List<String> splitIntoSentences(String text) {
		return Arrays.stream(
				text
					.replaceAll("\\b\\d+\\.?\\s*", " ")
					.replaceAll("Sampling\\s+\\d+", " ")
					.replaceAll("[●•▪▶▷]", ". ")
					.replaceAll("[\\r\\n]+", ". ")
					.split("[\\.\\!\\?]+"))
			.map(String::trim)
			.filter(s -> !s.matches(".*\\b(import|from|Machine Learning 기초|Bootstrap)\\b.*"))
			.filter(s -> s.matches(".*다$.*")
				|| s.matches(".*\\b(is|are)\\b.*")
				|| s.matches(".*\\w+ing\\b.*"))
			.filter(s -> s.length() > 10) // 최소 길이 필터링
			.distinct()
			.collect(Collectors.toList());
	}

	// 문장 간 유사도를 기반으로 핵심 문장 추출
	private List<String> extractImportantSentencesUsingTextRank(List<String> sents, Set<String> stopwords, int topN) {
		int n = sents.size();
		if (n == 0)
			return Collections.emptyList();

		List<Set<String>> tokenSets = sents.stream()
			.map(s -> new HashSet<>(tokenize(s, stopwords)))
			.collect(Collectors.toList());

		double[][] sim = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) {
					sim[i][j] = 1;
				} else {
					Set<String> a = tokenSets.get(i);
					Set<String> b = tokenSets.get(j);
					Set<String> inter = new HashSet<>(a);
					inter.retainAll(b);
					Set<String> uni = new HashSet<>(a);
					uni.addAll(b);
					sim[i][j] = uni.isEmpty() ? 0 : (double)inter.size() / uni.size();
				}
			}
		}

		double[] score = new double[n];
		Arrays.fill(score, 1.0);
		double d = 0.85;

		for (int it = 0; it < 30; it++) {
			double[] next = new double[n];
			for (int i = 0; i < n; i++) {
				double sum = 0;
				for (int j = 0; j < n; j++) {
					if (i != j && sim[j][i] > 0) {
						double norm = Arrays.stream(sim[j]).sum() - sim[j][j];
						sum += (sim[j][i] / (norm > 0 ? norm : 1)) * score[j];
					}
				}
				next[i] = (1 - d) + d * sum;
			}
			score = next;
		}

		final double[] sc = score;
		return IntStream.range(0, n)
			.boxed()
			.sorted((i, j) -> Double.compare(sc[j], sc[i]))
			.limit(topN)
			.map(sents::get)
			.collect(Collectors.toList());
	}
}