package org.unilab.improfessorbe.domain.problem.infrastructure.domain;

import lombok.Getter;

@Getter
public class Problem {

	//문제 번호, 내용, 풀이과정, 정답
	private String number;
	private String content;
	private String description;
	private String answer;

	private Problem(String number, String content, String description, String answer) {
		this.number = number;
		this.content = content;
		this.description = description;
		this.answer = answer;
	}

	public static Problem create(String number, String content, String description, String answer) {
		return new Problem(number, content, description, answer);
	}

	public void update(String number, String content, String description, String answer) {
		this.number = number;
		this.content = content;
		this.description = description;
		this.answer = answer;
	}
}
