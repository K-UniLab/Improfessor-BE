package org.unilab.improfessorbe.domain.problem.service;

import java.util.List;

import org.unilab.improfessorbe.domain.problem.dto.CachedProblemDto;
import org.unilab.improfessorbe.domain.problem.dto.ProblemResponse;

public interface ProblemCacheService {
	String cacheProblems(List<ProblemResponse> problems, String originalFileName);

	CachedProblemDto getCachedProblems(String cacheKey);
}