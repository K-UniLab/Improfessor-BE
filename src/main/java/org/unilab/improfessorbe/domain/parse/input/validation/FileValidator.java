package org.unilab.improfessorbe.domain.parse.input.validation;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

@Component
public class FileValidator {

	private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
	private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "ppt", "pptx");

	public void validateFile(MultipartFile file) {
		validateNotEmpty(file);
		validateFileSize(file);
		validateFileExtension(file);
	}

	private void validateNotEmpty(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
		}
	}

	private void validateFileSize(MultipartFile file) {
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new CustomException(ErrorCode.FILE_TOO_LARGE);
		}
	}

	private void validateFileExtension(MultipartFile file) {
		String extension = extractFileExtension(file.getOriginalFilename());
		if (!SUPPORTED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
		}
	}

	private String extractFileExtension(String fileName) {
		if (fileName == null || fileName.lastIndexOf('.') == -1) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}
}