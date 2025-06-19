package org.unilab.improfessorbe.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unilab.improfessorbe.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
