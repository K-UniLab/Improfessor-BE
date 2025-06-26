package org.unilab.improfessorbe.domain.problem.application.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileParseResult {
	private String fileName;
	private String content;
	private LocalDateTime timestamp;
	private long fileSize;

	public static FileParseResult of(String fileName, String content, long fileSize) {
		return new FileParseResult(fileName, content, LocalDateTime.now(), fileSize);
	}
}