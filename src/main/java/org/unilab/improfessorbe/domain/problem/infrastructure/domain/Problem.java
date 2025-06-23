package org.unilab.improfessorbe.domain.problem.infrastructure.domain;

import lombok.Getter;

@Getter
public class Problem {

	//제목, 내용, 풀이과정, 정답
	private String title;
	private String content;
	private String description;
	private String answer;

	private Problem(String title, String content, String description, String answer) {
		this.title = title;
		this.content = content;
		this.description = description;
		this.answer = answer;
	}

	public static Problem create(String title, String content, String description, String answer) {
		return new Problem(title, content, description, answer);
	}
	
	public void update(String title, String content, String description, String answer) {
		this.title = title;
		this.content = content;
		this.description = description;
		this.answer = answer;
	}
}
