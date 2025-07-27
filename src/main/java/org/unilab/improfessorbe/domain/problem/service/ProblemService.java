package org.unilab.improfessorbe.domain.problem.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.problem.domain.Problem;
import org.unilab.improfessorbe.domain.problem.dto.CachedProblemDto;
import org.unilab.improfessorbe.domain.problem.dto.ConceptExtractionResult;
import org.unilab.improfessorbe.domain.problem.dto.ProblemDownloadResponse;
import org.unilab.improfessorbe.domain.problem.dto.ProblemGenerationResponse;
import org.unilab.improfessorbe.domain.problem.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini.GeminiApiClient;
import org.unilab.improfessorbe.domain.problem.service.input.ConceptExtractorService;
import org.unilab.improfessorbe.domain.problem.service.input.FileParseService;
import org.unilab.improfessorbe.domain.problem.service.output.PdfExportService;
import org.unilab.improfessorbe.domain.problem.service.output.ProblemTextParser;
import org.unilab.improfessorbe.domain.user.service.UserService;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

	private final FileParseService fileParseService;
	private final GeminiApiClient geminiApiClient;
	private final ProblemTextParser problemTextParser;
	private final ConceptExtractorService conceptExtractorService;
	@Qualifier("redisCache")
	private final ProblemCacheService problemCacheService;
	private final PdfExportService pdfExportService;
	private final UserService userService;

	@Transactional
	public ProblemGenerationResponse createProblemWithCache(Long userId, List<MultipartFile> conceptFiles,
		List<MultipartFile> formatFiles) {
		try {
			// 1. 문제 생성
			List<ProblemResponse> responses = createProblemWithMl(conceptFiles, formatFiles);

			// 2. 캐시 생성 및 저장
			String originalFileName = conceptFiles.get(0).getOriginalFilename();
			String downloadKey = problemCacheService.cacheProblems(responses, originalFileName);

			log.info("문제 생성 및 캐시 저장 완료: 총 {}개 문제, 다운로드 키: {}", responses.size(), downloadKey);

			userService.decrementFreeCount(userId);

			return ProblemGenerationResponse.of(downloadKey, responses);

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("문제 생성 및 캐시 저장 중 에러", e);
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

	public ProblemDownloadResponse downloadProblemsPdf(String downloadKey) {
		// 1. 캐시에서 데이터 조회
		CachedProblemDto cachedData = problemCacheService.getCachedProblems(downloadKey);

		// 2. PDF 생성
		byte[] pdfData = pdfExportService.exportProblemsToPdf(
			cachedData.getProblems(),
			cachedData.getOriginalFileName()
		);

		// 3. 파일명 생성
		String fileName = createDownloadFileName();

		log.info("문제 PDF 생성 완료: key={}, 파일명={}, 문제수={}",
			downloadKey, fileName, cachedData.getProblems().size());

		return ProblemDownloadResponse.builder()
			.pdfData(pdfData)
			.fileName(fileName)
			.originalFileName(cachedData.getOriginalFileName())
			.build();
	}

	private String createDownloadFileName() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		try {
			return URLEncoder.encode("생성된_문제_" + timestamp + ".pdf", "UTF-8")
				.replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return "problems_" + timestamp + ".pdf";
		}
	}

	public List<ProblemResponse> createProblemWithMl(List<MultipartFile> conceptFiles,
		List<MultipartFile> formatFiles) {
		try {
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");
			String formatContent = "";
			if (formatFiles != null) {
				formatContent = fileParseService.parseFileList(formatFiles, "형식");
			}

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			ConceptExtractionResult result = conceptExtractorService.extractConcepts(conceptContent);
			String conceptExtraction = result.toFormattedString();

			String problemText = geminiApiClient.generateProblems(conceptExtraction, formatContent);
			List<Problem> problems = problemTextParser.parseProblemText(problemText);

			List<ProblemResponse> responses = new ArrayList<>();
			for (Problem problem : problems) {
				responses.add(ProblemResponse.of(problem));
			}

			return responses;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

}