package org.unilab.improfessorbe.domain.problem.application.dto;

import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProblemResponse {

	//제목, 내용, 풀이과정, 정답
	private String title;
	private String content;
	private String description;
	private String answer;

	public static ProblemResponse toResponse(Problem problem) {
		return ProblemResponse.builder()
			.title(problem.getTitle())
			.content(problem.getContent())
			.description(problem.getDescription())
			.answer(problem.getAnswer())
			.build();
	}
}