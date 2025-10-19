package org.unilab.improfessorbe.domain.round.service;

import org.springframework.stereotype.Service;
import org.unilab.improfessorbe.domain.round.domain.Round;
import org.unilab.improfessorbe.domain.round.infrastructure.repository.RoundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundService {

	private final RoundRepository roundRepository;

	public void save(Round round) {
		roundRepository.save(round);
	}
}
