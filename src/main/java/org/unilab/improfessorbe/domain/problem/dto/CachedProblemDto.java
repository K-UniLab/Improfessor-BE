package org.unilab.improfessorbe.domain.problem.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CachedProblemDto {
	private List<ProblemResponse> problems;
	private String originalFileName;
	private LocalDateTime createdAt;
}