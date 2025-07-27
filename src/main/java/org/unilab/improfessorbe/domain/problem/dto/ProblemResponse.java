package org.unilab.improfessorbe.domain.problem.dto;

import org.unilab.improfessorbe.domain.problem.domain.Problem;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProblemResponse {

	private String number;
	private String content;
	private String description;
	private String answer;

	public static ProblemResponse of(Problem problem) {
		return ProblemResponse.builder()
			.number(problem.getNumber())
			.content(problem.getContent())
			.description(problem.getDescription())
			.answer(problem.getAnswer())
			.build();
	}
}