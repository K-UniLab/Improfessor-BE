package org.unilab.improfessorbe.domain.user.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unilab.improfessorbe.domain.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserIdAndDeletedAtIsNull(Long id);

	Optional<User> findByEmailAndDeletedAtIsNull(String email);

	Optional<User> findByNicknameAndDeletedAtIsNull(String nickname);
}
