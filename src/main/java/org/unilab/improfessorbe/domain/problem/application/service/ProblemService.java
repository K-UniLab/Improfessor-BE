package org.unilab.improfessorbe.domain.problem.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

	public List<ProblemResponse> createProblem(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			// 1. 개념 파일들 파싱
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");

			// 2. 형식 파일들 파싱
			String formatContent = fileParseService.parseFileList(formatFiles, "형식");

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			// 3. gemini이용해서 문제 생성
			String problemText = geminiApiClient.generateProblems(conceptContent, formatContent);

			// 4. 생성된 문제 텍스트를 파싱하여 여러 Problem 객체 생성
			List<Problem> problems = problemTextParser.parseProblemText(problemText);

			// 5. Response 생성 및 반환
			List<ProblemResponse> responses = new ArrayList<>();
			for (Problem problem : problems) {
				responses.add(ProblemResponse.toResponse(problem));
			}

			return responses;

		} catch (CustomException e) {
			// CustomException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 예상치 못한 예외는 PROBLEM_CREATION_FAILED로 변환
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

	public String getRawGeneratedProblem(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			// 1. 개념 파일들 파싱
			String conceptContent = fileParseService.parseFileList(conceptFiles, "개념");

			// 2. 형식 파일들 파싱
			String formatContent = fileParseService.parseFileList(formatFiles, "형식");

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			// 3. gemini이용해서 문제 생성
			String problemText = geminiApiClient.generateProblems(conceptContent, formatContent);

			return problemText;

		} catch (CustomException e) {
			// CustomException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 예상치 못한 예외는 PROBLEM_CREATION_FAILED로 변환
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}
}