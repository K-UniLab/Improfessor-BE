package org.unilab.improfessorbe.domain.notice.dto.request;

import org.unilab.improfessorbe.domain.notice.domain.Notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequest {
	private String title;
	private String content;

	public static Notice toEntity(NoticeRequest noticeRequest) {
		return Notice.createNotice(
			noticeRequest.title,
			noticeRequest.content
		);
	}
}
