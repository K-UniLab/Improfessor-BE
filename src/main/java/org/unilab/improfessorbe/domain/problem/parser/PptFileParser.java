package org.unilab.improfessorbe.domain.problem.parser;

import java.io.IOException;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PptFileParser implements FileParser {

	private static final String SLIDE_SEPARATOR = "\n\n=== 슬라이드 구분 ===\n\n";
	private final TextPreprocessor textPreprocessor;

	@Override
	public boolean supports(String extension) {
		return "ppt".equalsIgnoreCase(extension);
	}

	@Override
	public String parse(MultipartFile file) throws IOException {
		StringBuilder content = new StringBuilder();

		try (HSLFSlideShow ppt = new HSLFSlideShow(file.getInputStream())) {
			for (HSLFSlide slide : ppt.getSlides()) {
				String slideText = extractSlideText(slide);
				if (!slideText.trim().isEmpty()) {
					content.append(slideText).append(SLIDE_SEPARATOR);
				}
			}
		}

		String rawContent = content.toString();
		String cleanedContent = textPreprocessor.cleanSlideContent(rawContent, SLIDE_SEPARATOR);
		return textPreprocessor.preprocess(cleanedContent);
	}

	private String extractSlideText(HSLFSlide slide) {
		StringBuilder slideContent = new StringBuilder();

		for (HSLFShape shape : slide.getShapes()) {
			if (shape instanceof HSLFTextShape) {
				HSLFTextShape textShape = (HSLFTextShape)shape;
				String text = textShape.getText();
				if (text != null && !text.trim().isEmpty()) {
					slideContent.append(text.trim()).append("\n");
				}
			}
		}

		return slideContent.toString().trim();
	}
}