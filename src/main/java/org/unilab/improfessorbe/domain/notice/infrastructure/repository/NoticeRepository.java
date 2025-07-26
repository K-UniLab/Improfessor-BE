package org.unilab.improfessorbe.domain.notice.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unilab.improfessorbe.domain.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
