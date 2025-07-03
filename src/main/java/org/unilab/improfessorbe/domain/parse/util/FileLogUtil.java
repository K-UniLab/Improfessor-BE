package org.unilab.improfessorbe.domain.parse.util;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileLogUtil {

	public void logFileUploadInfo(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		int conceptCount = conceptFiles != null ? conceptFiles.size() : 0;
		int formatCount = formatFiles != null ? formatFiles.size() : 0;

		double conceptSizeMB = calculateTotalSizeMB(conceptFiles);
		double formatSizeMB = calculateTotalSizeMB(formatFiles);
		double totalSizeMB = conceptSizeMB + formatSizeMB;

		// String.format으로 미리 포맷팅
		log.info("문제 생성 요청: 개념 파일 {}개 ({} MB), 형식 파일 {}개 ({} MB), 전체: {}개 ({} MB)",
			conceptCount, String.format("%.3f", conceptSizeMB),
			formatCount, String.format("%.3f", formatSizeMB),
			conceptCount + formatCount, String.format("%.3f", totalSizeMB));

		logConceptFiles(conceptFiles);
		logFormatFiles(formatFiles);
	}

	private void logConceptFiles(List<MultipartFile> conceptFiles) {
		if (conceptFiles == null || conceptFiles.isEmpty()) {
			log.warn("개념 파일이 없습니다.");
			return;
		}

		double totalSizeMB = calculateTotalSizeMB(conceptFiles);
		log.info("=== 개념 파일 ({}, {} MB) ===", conceptFiles.size(), String.format("%.3f", totalSizeMB));

		for (int i = 0; i < conceptFiles.size(); i++) {
			MultipartFile file = conceptFiles.get(i);
			double sizeInMB = file.getSize() / (1000.0 * 1000.0);
			String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
			log.info("개념 파일 {}: {} ({} MB)",
				i + 1, fileName, String.format("%.3f", sizeInMB));
		}
	}

	private void logFormatFiles(List<MultipartFile> formatFiles) {
		if (formatFiles == null || formatFiles.isEmpty()) {
			log.info("=== 형식 파일 (없음) ===");
			return;
		}

		double totalSizeMB = calculateTotalSizeMB(formatFiles);
		log.info("=== 형식 파일 ({}, {} MB) ===", formatFiles.size(), String.format("%.3f", totalSizeMB));

		for (int i = 0; i < formatFiles.size(); i++) {
			MultipartFile file = formatFiles.get(i);
			double sizeInMB = file.getSize() / (1000.0 * 1000.0);
			String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";

			// 빈 파일 체크 및 로깅
			if (file.isEmpty()) {
				log.warn("형식 파일 {}: {} - 빈 파일입니다!", i + 1, fileName);
			} else {
				log.info("형식 파일 {}: {} ({} MB)", i + 1, fileName, String.format("%.3f", sizeInMB));
			}
		}
	}

	private double calculateTotalSizeMB(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return 0.0;
		}
		return files.stream()
			.mapToLong(MultipartFile::getSize)
			.sum() / (1000.0 * 1000.0);
	}

	public boolean isValidRequest(List<MultipartFile> conceptFiles) {
		return conceptFiles != null && !conceptFiles.isEmpty();
	}
}