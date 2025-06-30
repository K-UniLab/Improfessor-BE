package org.unilab.improfessorbe.domain.problem.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.parse.dto.ConceptExtractionResult;
import org.unilab.improfessorbe.domain.parse.input.service.ConceptExtractorService;
import org.unilab.improfessorbe.domain.parse.input.service.FileParseService;
import org.unilab.improfessorbe.domain.parse.output.ProblemTextParser;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;
import org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini.GeminiApiClient;
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

	public List<ProblemResponse> createProblem(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");
			String formatContent = "";
			if (formatFiles != null) {
				formatContent = fileParseService.parseFileList(formatFiles, "형식");
			}

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개", conceptContent.length(), formatContent.length());

			String problemText = geminiApiClient.generateProblems(conceptContent, formatContent);
			List<Problem> problems = problemTextParser.parseProblemText(problemText);

			List<ProblemResponse> responses = new ArrayList<>();
			for (Problem problem : problems) {
				responses.add(ProblemResponse.toResponse(problem));
			}

			return responses;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
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

			//ML 추가
			ConceptExtractionResult result = conceptExtractorService.extractConcepts(conceptContent);
			String conceptExtraction = result.toFormattedString();

			String problemText = geminiApiClient.generateProblems(conceptExtraction, formatContent);
			List<Problem> problems = problemTextParser.parseProblemText(problemText);

			List<ProblemResponse> responses = new ArrayList<>();
			for (Problem problem : problems) {
				responses.add(ProblemResponse.toResponse(problem));
			}

			return responses;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

	public String getRawGeneratedProblem(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");
			String formatContent = "";
			if (formatFiles != null) {
				formatContent = fileParseService.parseFileList(formatFiles, "형식");
			}

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			String problemText = geminiApiClient.generateProblems(conceptContent, formatContent);
			return problemText;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

	public String getParseFiles(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");
			String formatContent = "";
			if (formatFiles != null) {
				formatContent = fileParseService.parseFileList(formatFiles, "형식");
			}

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			/*StringBuilder sb = new StringBuilder();
			sb.append(conceptContent);
			sb.append(formatContent);*/

			ConceptExtractionResult result = conceptExtractorService.extractConcepts(conceptContent, 100, 100);
			String conceptExtraction = result.toFormattedString();

			return conceptExtraction.toString();

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}
}