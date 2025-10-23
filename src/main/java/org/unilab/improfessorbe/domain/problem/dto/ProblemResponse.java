package org.unilab.improfessorbe.domain.problem.dto;

import org.unilab.improfessorbe.domain.problem.domain.Problem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemResponse {

	private Long problemId;
	private String type;
	private String content;
	private String description;
	private String answer;

	public static ProblemResponse from(Problem problem) {
		return ProblemResponse.builder()
			.problemId(problem.getId())
			.type(problem.getType())
			.content(problem.getContent())
			.description(problem.getDescription())
			.answer(problem.getAnswer())
			.build();
	}

	public static ProblemResponse of(String type, String content, String description, String answer) {
		return ProblemResponse.builder()
			.type(type)
			.content(content)
			.description(description)
			.answer(answer)
			.build();
	}
}