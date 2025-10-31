package org.unilab.improfessorbe.domain.problem.service.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.domain.problem.dto.ProblemResponse;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfExportService {

	public byte[] exportProblemsToPdf(List<ProblemResponse> problems, String originalFileName) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			PdfDocument pdf = new PdfDocument(new PdfWriter(out));
			Document document = new Document(pdf);

			// 한글 폰트 설정 - 시스템 기본 폰트 사용
			PdfFont font = createKoreanFont();
			PdfFont boldFont = createKoreanBoldFont();

			// 문서 전체에 폰트 적용
			document.setFont(font);

			// 제목
			document.add(new Paragraph("생성된 문제집")
				.setFontSize(20)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(20));

			// 생성 정보
			document.add(new Paragraph("원본 파일: " + originalFileName)
				.setFontSize(10));
			document.add(new Paragraph("생성 일시: " + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
				.setFontSize(10));
			document.add(new Paragraph("총 문제 수: " + problems.size() + "개")
				.setFontSize(10)
				.setMarginBottom(30));

			// 문제들
			for (int i = 0; i < problems.size(); i++) {
				ProblemResponse problem = problems.get(i);

				// 문제 번호와 제목
				document.add(new Paragraph(problem.getNumber())
					.setFontSize(14)
					.setBold()
					.setMarginTop(20));

				// 문제 내용
				document.add(new Paragraph("문제: " + problem.getContent())
					.setFontSize(12)
					.setMarginLeft(20));

				// 풀이 과정
				if (problem.getDescription() != null && !problem.getDescription().trim().isEmpty()) {
					document.add(new Paragraph("풀이: " + problem.getDescription())
						.setFontSize(11)
						.setMarginLeft(20));
				}

				// 정답
				document.add(new Paragraph("정답: " + problem.getAnswer())
					.setFontSize(12)
					.setBold()
					.setMarginLeft(20)
					.setMarginBottom(15));

				// 문제 사이 여백
				if (i < problems.size() - 1) {
					document.add(new Paragraph("\n"));
				}
			}

			document.close();

			log.info("PDF 파일 생성 완료: {}개 문제", problems.size());
			return out.toByteArray();

		} catch (IOException e) {
			log.error("PDF 파일 생성 실패", e);
			throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
		}
	}

	private PdfFont createKoreanFont() {
		try {
			// 방법 1: 시스템 폰트 사용 (Windows)
			try {
				return PdfFontFactory.createFont("c:/windows/fonts/malgun.ttf", PdfEncodings.IDENTITY_H);
			} catch (Exception e) {
				log.debug("Windows 폰트 로드 실패, 대안 시도");
			}

			// 방법 2: 내장 아시아 폰트 사용
			try {
				return PdfFontFactory.createFont("HeiseiMin-W3", "UniJIS-UCS2-H");
			} catch (Exception e) {
				log.debug("아시아 폰트 로드 실패, 기본 폰트 사용");
			}

			// 방법 3: 기본 폰트 (영문만 지원)
			return PdfFontFactory.createFont();

		} catch (IOException e) {
			log.warn("폰트 생성 실패, 기본 폰트 사용", e);
			try {
				return PdfFontFactory.createFont();
			} catch (IOException ex) {
				throw new RuntimeException("폰트 생성 실패", ex);
			}
		}
	}

	private PdfFont createKoreanBoldFont() {
		try {
			// 방법 1: 시스템 볼드 폰트 사용 (Windows)
			try {
				return PdfFontFactory.createFont("c:/windows/fonts/malgunbd.ttf", PdfEncodings.IDENTITY_H);
			} catch (Exception e) {
				log.debug("Windows 볼드 폰트 로드 실패, 대안 시도");
			}

			// 방법 2: 내장 아시아 볼드 폰트 사용
			try {
				return PdfFontFactory.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H");
			} catch (Exception e) {
				log.debug("아시아 볼드 폰트 로드 실패, 일반 폰트 사용");
			}

			// 방법 3: 일반 폰트 사용
			return createKoreanFont();

		} catch (Exception e) {
			log.warn("볼드 폰트 생성 실패, 일반 폰트 사용", e);
			return createKoreanFont();
		}
	}
}