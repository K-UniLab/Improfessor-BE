package org.unilab.improfessorbe.domain.problem.application.service;

import java.util.List;

import org.unilab.improfessorbe.domain.problem.application.dto.CachedProblemDto;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;

public interface ProblemCacheService {
	String cacheProblems(List<ProblemResponse> problems, String originalFileName);

	CachedProblemDto getCachedProblems(String cacheKey);
}