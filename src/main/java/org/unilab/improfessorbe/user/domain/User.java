package org.unilab.improfessorbe.user.domain;

import org.unilab.improfessorbe.config.domain.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	private String nickname;

	private String email;

	private String password;

	private String university;

	private String major;

	private Integer freeCount;

	private Integer recommendCount;

	@Enumerated(value = EnumType.STRING)
	private Role role;

	enum Role {
		admin, user
	}

}
