package org.unilab.improfessorbe.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unilab.improfessorbe.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
