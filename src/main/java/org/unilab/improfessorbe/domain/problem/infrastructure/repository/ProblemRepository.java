package org.unilab.improfessorbe.domain.problem.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.unilab.improfessorbe.domain.problem.domain.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

	// 특정 회차의 모든 문제 조회 (저장 여부 상관없이)
	List<Problem> findByRoundIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long roundId);

	// 특정 유저의 저장된 문제만 조회 (네이티브 쿼리로 변경)
	@Query(value =
		"SELECT p.* FROM problem p " +
			"JOIN round r ON p.round_id = r.id " +
			"WHERE r.user_id = :userId " +
			"AND p.saved_at IS NOT NULL " +
			"AND p.deleted_at IS NULL " +
			"ORDER BY p.saved_at DESC",
		nativeQuery = true)
	List<Problem> findSavedProblemsByUserId(@Param("userId") Long userId);

}