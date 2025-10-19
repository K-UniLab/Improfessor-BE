package org.unilab.improfessorbe.domain.problem.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemResponse {

	private String type;
	private String content;
	private String description;
	private String answer;

	public static ProblemResponse of(String type, String content, String description, String answer) {
		return ProblemResponse.builder()
			.type(type)
			.content(content)
			.description(description)
			.answer(answer)
			.build();
	}
}