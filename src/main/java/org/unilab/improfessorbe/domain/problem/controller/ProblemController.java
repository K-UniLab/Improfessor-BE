package org.unilab.improfessorbe.domain.problem.controller;

import java.text.DecimalFormat;
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
	public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
		@RequestParam("conceptFiles") List<MultipartFile> conceptFiles,
		@RequestParam("formatFiles") List<MultipartFile> formatFiles) {

		log.info("문제 생성 요청: 개념 파일 {}개, 형식 파일 {}개",
			conceptFiles.size(), formatFiles.size());

		DecimalFormat df = new DecimalFormat("#.##");

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

		ProblemResponse response = problemService.createProblem(conceptFiles, formatFiles);

		log.info("문제 생성 완료: 제목={}", response.getTitle());

		return ResponseEntity.ok(
			ApiResponse.success(response, "문제가 성공적으로 생성되었습니다.")
		);

	}
}