package org.unilab.improfessorbe.domain.problem.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.problem.application.dto.FileParseResult;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemService {

	private final FileParseService fileParseService;

	public ProblemResponse createProblem(MultipartFile file) {
		try {
			// 1. 파일 파싱
			FileParseResult parseResult = fileParseService.parseFile(file);
			String parsedContent = parseResult.getContent();

			// 2. Problem 엔티티 생성
			Problem problem = Problem.create(
				file.getOriginalFilename(),
				parsedContent,
				"파일에서 파싱된 원본 내용입니다.",
				""
			);

			// 3. Response 생성 및 반환
			return ProblemResponse.toResponse(problem);

		} catch (CustomException e) {
			// CustomException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 예상치 못한 예외는 PROBLEM_CREATION_FAILED로 변환
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}
}