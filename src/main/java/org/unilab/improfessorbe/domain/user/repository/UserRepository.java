package org.unilab.improfessorbe.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unilab.improfessorbe.domain.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
