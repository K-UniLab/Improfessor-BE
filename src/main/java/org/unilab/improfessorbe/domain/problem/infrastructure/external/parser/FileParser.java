package org.unilab.improfessorbe.domain.problem.infrastructure.external.parser;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileParser {
	boolean supports(String extension);

	String parse(MultipartFile file) throws IOException;
}