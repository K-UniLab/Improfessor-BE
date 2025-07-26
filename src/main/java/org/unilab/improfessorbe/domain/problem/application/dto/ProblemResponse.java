package org.unilab.improfessorbe.domain.problem.application.dto;

import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProblemResponse {

	private String number;
	private String content;
	private String description;
	private String answer;

	public static ProblemResponse toResponse(Problem problem) {
		return ProblemResponse.builder()
			.number(problem.getNumber())
			.content(problem.getContent())
			.description(problem.getDescription())
			.answer(problem.getAnswer())
			.build();
	}
}