package org.unilab.improfessorbe.domain.problem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemDownloadResponse {
	private byte[] pdfData;
	private String fileName;
	private String originalFileName;
}