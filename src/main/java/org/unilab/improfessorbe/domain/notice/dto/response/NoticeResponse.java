package org.unilab.improfessorbe.domain.notice.dto.response;

import java.time.LocalDateTime;

import org.unilab.improfessorbe.domain.notice.domain.Notice;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NoticeResponse {
	private Long noticeId;
	private String title;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static NoticeResponse of(Notice notice) {
		return NoticeResponse.builder()
			.noticeId(notice.getNoticeId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.createdAt(notice.getCreatedAt())
			.updatedAt(notice.getUpdatedAt())
			.build();
	}
}
