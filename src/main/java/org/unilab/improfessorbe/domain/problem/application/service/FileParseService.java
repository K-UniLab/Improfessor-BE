package org.unilab.improfessorbe.domain.problem.application.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.problem.application.dto.FileParseResult;
import org.unilab.improfessorbe.domain.problem.infrastructure.external.parser.FileParserManager;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

@Service
public class FileParseService {

	private final FileParserManager fileParserManager;
	private final FileValidationService fileValidationService;

	public FileParseService(FileParserManager fileParserManager,
		FileValidationService fileValidationService) {
		this.fileParserManager = fileParserManager;
		this.fileValidationService = fileValidationService;
	}

	public FileParseResult parseFile(MultipartFile file) {
		try {
			fileValidationService.validateFile(file);

			String parsedContent = fileParserManager.parse(file);

			return FileParseResult.of(
				file.getOriginalFilename(),
				parsedContent,
				file.getSize()
			);
		} catch (IOException e) {
			throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
		}
	}
}