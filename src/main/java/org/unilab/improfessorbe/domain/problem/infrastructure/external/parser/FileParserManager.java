package org.unilab.improfessorbe.domain.problem.infrastructure.external.parser;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

@Component
public class FileParserManager {

	private final List<FileParser> parsers;

	public FileParserManager(List<FileParser> parsers) {
		this.parsers = parsers;
	}

	public String parse(MultipartFile file) throws IOException {
		String extension = extractFileExtension(file.getOriginalFilename());

		return parsers.stream()
			.filter(parser -> parser.supports(extension))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE))
			.parse(file);
	}

	private String extractFileExtension(String fileName) {
		if (fileName == null || fileName.lastIndexOf('.') == -1) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}
}