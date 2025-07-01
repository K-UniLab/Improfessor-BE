package org.unilab.improfessorbe.domain.problem.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.parse.util.FileLogUtil;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemDownloadResponse;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemGenerationResponse;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.application.service.ProblemService;
import org.unilab.improfessorbe.global.common.ApiResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Hidden;
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

	//메인 사용 모델
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "문제 생성", description = "(사용중)개념추출, llm, 캐시 적용 모델")
	public ResponseEntity<ApiResponse<ProblemGenerationResponse>> createProblemWithMl(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		// 문제 생성 + 캐시 저장을 서비스에서 처리
		ProblemGenerationResponse result = problemService.createProblemWithCache(conceptFiles, formatFiles);

		return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
	}

	@GetMapping("/download/{downloadKey}")
	@Operation(summary = "문제 PDF 다운로드")
	public ResponseEntity<byte[]> downloadProblemsPdf(@PathVariable String downloadKey) {

		ProblemDownloadResponse result = problemService.downloadProblemsPdf(downloadKey);

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION,
				ContentDisposition.attachment()
					.filename(result.getFileName())
					.build().toString())
			.contentType(MediaType.APPLICATION_PDF)
			.body(result.getPdfData());
	}

	//ml안쓴 모델, 그냥 파싱한거 다 llm 넣음
	@Hidden
	@PostMapping("/notml")
	public ResponseEntity<ApiResponse<List<ProblemResponse>>> createProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		List<ProblemResponse> responses = problemService.createProblem(conceptFiles, formatFiles);

		log.info("문제 생성 완료: 총 {}개 문제", responses.size());

		return ResponseEntity.ok(
			ApiResponse.success(responses, "문제가 성공적으로 생성되었습니다.")
		);
	}

	//gemini 문제 생성 원본 데이터 조회
	@Hidden
	@PostMapping("/raw")
	public ResponseEntity<ApiResponse<String>> getRawGeneratedProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		String geminiText = problemService.getRawGeneratedProblem(conceptFiles, formatFiles);

		return ResponseEntity.ok(
			ApiResponse.success(geminiText, "문제가 성공적으로 생성되었습니다.")
		);
	}

	//파싱된 결과 그대로 반환
	@Hidden
	@PostMapping("/files")
	public ResponseEntity<ApiResponse<String>> getParseFiles(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam(value = "formatFiles", required = false) List<MultipartFile> formatFiles) {

		fileLogUtil.logFileUploadInfo(conceptFiles, formatFiles);

		if (!fileLogUtil.isValidRequest(conceptFiles)) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}

		String result = problemService.getParseFiles(conceptFiles, formatFiles);

		return ResponseEntity.ok(
			ApiResponse.success(result, "문제가 성공적으로 생성되었습니다.")
		);
	}
}