package org.unilab.improfessorbe.domain.user.domain;

import org.unilab.improfessorbe.global.common.BaseEntity;

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
		ADMIN, USER
	}

	private User(String nickname, String email, String password, String university, String major, Integer freeCount, Integer recommendCount) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.university = university;
		this.major = major;
		this.freeCount = freeCount;
		this.recommendCount = recommendCount;
		this.role = Role.USER;
	}

	public static User create(String nickname, String email, String password, String university, String major, Integer freeCount, Integer recommendCount){
		return new User(nickname, email, password, university, major, freeCount, recommendCount);
	}

	public void updateUser(String password, String university, String major, Integer freeCount, Integer recommendCount){
		this.password = password;
		this.university = university;
		this.major = major;
		this.freeCount = freeCount;
		this.recommendCount = recommendCount;
	}


}
