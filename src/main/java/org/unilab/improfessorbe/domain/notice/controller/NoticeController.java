package org.unilab.improfessorbe.domain.notice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unilab.improfessorbe.domain.notice.dto.request.NoticeRequest;
import org.unilab.improfessorbe.domain.notice.service.NoticeService;
import org.unilab.improfessorbe.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	@PostMapping("/{userId}")
	public ResponseEntity<ApiResponse<Void>> createNotice(
		@PathVariable Long userId,
		@RequestBody NoticeRequest noticeRequest
	) {
		noticeService.createNotice(userId, noticeRequest);
		return ResponseEntity.ok(ApiResponse.success());
	}

}
