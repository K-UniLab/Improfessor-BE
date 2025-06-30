package org.unilab.improfessorbe.domain.problem.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.parse.FileLogUtil;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.application.service.ProblemService;
import org.unilab.improfessorbe.global.common.ApiResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

	private final ProblemService problemService;
	private final FileLogUtil fileLogUtil;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "문제 생성", description = "개념 파일은 필수, 형식 파일은 선택사항")
	public ResponseEntity<ApiResponse<List<ProblemResponse>>> createProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			log.warn("필수 파일 누락: 개념 파일이 없습니다.");
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		// 서비스 호출
		List<ProblemResponse> responses = problemService.createProblem(conceptFiles, formatFiles);

		log.info("문제 생성 완료: 총 {}개 문제", responses.size());

		return ResponseEntity.ok(
			ApiResponse.success(responses, "문제가 성공적으로 생성되었습니다.")
		);
	}

	@PostMapping("/ml")
	public ResponseEntity<ApiResponse<List<ProblemResponse>>> createProblemWithMl(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			log.warn("필수 파일 누락: 개념 파일이 없습니다.");
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		List<ProblemResponse> responses = problemService.createProblemWithMl(conceptFiles, formatFiles);

		log.info("문제 생성 완료: 총 {}개 문제", responses.size());

		return ResponseEntity.ok(
			ApiResponse.success(responses, "문제가 성공적으로 생성되었습니다.")
		);
	}

	//gemini 문제 생성 원본 데이터 조회
	@PostMapping("/raw")
	public ResponseEntity<ApiResponse<String>> getRawGeneratedProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			log.warn("필수 파일 누락: 개념 파일이 없습니다.");
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		String geminiText = problemService.getRawGeneratedProblem(conceptFiles, formatFiles);

		return ResponseEntity.ok(
			ApiResponse.success(geminiText, "문제가 성공적으로 생성되었습니다.")
		);
	}

	//파싱된 결과 그대로 or ml후 반환
	@PostMapping("/files")
	public ResponseEntity<ApiResponse<String>> getParseFiles(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			log.warn("필수 파일 누락: 개념 파일이 없습니다.");
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		String result = problemService.getParseFiles(conceptFiles, formatFiles);

		return ResponseEntity.ok(
			ApiResponse.success(result, "문제가 성공적으로 생성되었습니다.")
		);
	}
}