package org.unilab.improfessorbe.domain.problem.domain;

import java.time.LocalDateTime;

import org.unilab.improfessorbe.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long roundId;

	private String type;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "answer", columnDefinition = "TEXT")
	private String answer;

	private LocalDateTime savedAt;  // 이것만 있으면 됨!

	private Problem(Long roundId, String type, String content,
		String description, String answer) {
		this.roundId = roundId;
		this.type = type;
		this.content = content;
		this.description = description;
		this.answer = answer;
	}

	public static Problem create(Long roundId, String type, String content,
		String description, String answer) {
		return new Problem(roundId, type, content, description, answer);
	}

	// 문제 저장
	public void save() {
		this.savedAt = LocalDateTime.now();
	}

	// 저장 취소
	public void unsave() {
		this.savedAt = null;
	}

	// 저장 여부 확인
	public boolean isSaved() {
		return this.savedAt != null;
	}

	public boolean isDeleted() {
		return this.getDeletedAt() != null;
	}

}
