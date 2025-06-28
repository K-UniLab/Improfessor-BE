package org.unilab.improfessorbe.domain.problem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.application.service.ProblemService;
import org.unilab.improfessorbe.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

	private final ProblemService problemService;

	@PostMapping
	public ResponseEntity<ApiResponse<List<ProblemResponse>>> createProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam("formatFiles") List<MultipartFile> formatFiles) {

		log.info("문제 생성 요청: 개념 파일 {}개, 형식 파일 {}개",
			conceptFiles.size(), formatFiles.size());

		for (int i = 0; i < conceptFiles.size(); i++) {
			MultipartFile file = conceptFiles.get(i);
			double sizeInMB = file.getSize() / (1024.0 * 1024.0);
			String formattedSize = String.format("%.2f", sizeInMB);
			log.info("개념 파일 {}: {} ({} MB)", i + 1, file.getOriginalFilename(), formattedSize);
		}

		for (int i = 0; i < formatFiles.size(); i++) {
			MultipartFile file = formatFiles.get(i);
			double sizeInMB = file.getSize() / (1024.0 * 1024.0);
			String formattedSize = String.format("%.2f", sizeInMB);
			log.info("형식 파일 {}: {} ({} MB)", i + 1, file.getOriginalFilename(), formattedSize);
		}

		List<ProblemResponse> responses = problemService.createProblem(conceptFiles, formatFiles);

		log.info("문제 생성 완료: 총 {}개 문제", responses.size());

		return ResponseEntity.ok(
			ApiResponse.success(responses, "문제가 성공적으로 생성되었습니다.")
		);

	}

	//gemini 문제 생성 원본 데이터 조회
	@PostMapping("/raw")
	public ResponseEntity<ApiResponse<String>> getRawGeneratedProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam("formatFiles") List<MultipartFile> formatFiles) {

		log.info("문제 생성 요청: 개념 파일 {}개, 형식 파일 {}개",
			conceptFiles.size(), formatFiles.size());

		for (int i = 0; i < conceptFiles.size(); i++) {
			MultipartFile file = conceptFiles.get(i);
			double sizeInMB = file.getSize() / (1024.0 * 1024.0);
			String formattedSize = String.format("%.2f", sizeInMB);
			log.info("개념 파일 {}: {} ({} MB)", i + 1, file.getOriginalFilename(), formattedSize);
		}

		for (int i = 0; i < formatFiles.size(); i++) {
			MultipartFile file = formatFiles.get(i);
			double sizeInMB = file.getSize() / (1024.0 * 1024.0);
			String formattedSize = String.format("%.2f", sizeInMB);
			log.info("형식 파일 {}: {} ({} MB)", i + 1, file.getOriginalFilename(), formattedSize);
		}

		String geminiText = problemService.getRawGeneratedProblem(conceptFiles, formatFiles);

		return ResponseEntity.ok(
			ApiResponse.success(geminiText, "문제가 성공적으로 생성되었습니다.")
		);
	}
}