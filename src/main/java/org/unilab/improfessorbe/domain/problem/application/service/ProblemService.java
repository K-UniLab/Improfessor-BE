package org.unilab.improfessorbe.domain.problem.application.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unilab.improfessorbe.domain.problem.application.dto.FileParseResult;
import org.unilab.improfessorbe.domain.problem.application.dto.ProblemResponse;
import org.unilab.improfessorbe.domain.problem.infrastructure.domain.Problem;
import org.unilab.improfessorbe.domain.problem.infrastructure.external.gemini.GeminiApiClient;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

	private final FileParseService fileParseService;
	private final GeminiApiClient geminiApiClient;

	public ProblemResponse createProblem(List<MultipartFile> conceptFiles, List<MultipartFile> formatFiles) {
		try {
			// 1. 개념 파일들 파싱
			String conceptContent = parseFileList(conceptFiles, "개념");

			// 2. 형식 파일들 파싱
			String formatContent = parseFileList(formatFiles, "형식");

			log.info("개념 파일 글자수: {}개 / 형식 파일 글자수: {}개",
				conceptContent.length(), formatContent.length());

			// 3. gemini이용해서 문제 생성
			String problemText = geminiApiClient.generateProblems(conceptContent, formatContent);

			// 4. Problem 도메인 객체 생성
			Problem problem = Problem.create(
				conceptFiles.get(0).getOriginalFilename() + " 문서의 족보를 생성했습니다.", // 파일 제목
				problemText, // 조합된 내용
				String.format("개념 파일 %d개, 형식 파일 %d개에서 파싱된 내용입니다.",
					conceptFiles.size(), formatFiles.size()), // 설명
				"" // 빈 정답
			);

			// 3. Response 생성 및 반환
			return ProblemResponse.toResponse(problem);

		} catch (CustomException e) {
			// CustomException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 예상치 못한 예외는 PROBLEM_CREATION_FAILED로 변환
			throw new CustomException(ErrorCode.PROBLEM_CREATION_FAILED);
		}
	}

	private String parseFileList(List<MultipartFile> files, String fileType) throws IOException {
		if (files.isEmpty()) {
			return fileType + " 파일이 없습니다.";
		}

		StringBuilder content = new StringBuilder();

		for (int i = 0; i < files.size(); i++) {
			MultipartFile file = files.get(i);
			FileParseResult parseResult = fileParseService.parseFile(file);

			if (i > 0) {
				content.append("\n\n--- ").append(fileType).append(" 파일 구분 ---\n\n");
			}
			content.append(parseResult.getContent());
		}

		return content.toString();
	}

}