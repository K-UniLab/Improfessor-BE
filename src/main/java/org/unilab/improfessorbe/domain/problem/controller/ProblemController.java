package org.unilab.improfessorbe.domain.problem.controller;

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
		@RequestParam("file") MultipartFile file) {

		log.info("문제 생성 요청: 파일={}", file.getOriginalFilename());

		ProblemResponse response = problemService.createProblem(file);
		log.info("문제 생성 완료: 제목={}", response.getTitle());

		return ResponseEntity.ok(
			ApiResponse.success(response, "문제가 성공적으로 생성되었습니다.")
		);
	}
}