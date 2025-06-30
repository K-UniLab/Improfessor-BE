package org.unilab.improfessorbe.domain.parse.input.convert;

import java.io.IOException;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PptxFileParser implements FileParser {

	private static final String SLIDE_SEPARATOR = "\n\n=== 슬라이드 구분 ===\n\n";
	private final TextPreprocessor textPreprocessor;

	@Override
	public boolean supports(String extension) {
		return "pptx".equalsIgnoreCase(extension);
	}

	@Override
	public String parse(MultipartFile file) throws IOException {
		StringBuilder content = new StringBuilder();

		try (XMLSlideShow ppt = new XMLSlideShow(file.getInputStream())) {
			for (XSLFSlide slide : ppt.getSlides()) {
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

	private String extractSlideText(XSLFSlide slide) {
		StringBuilder slideContent = new StringBuilder();

		for (XSLFShape shape : slide.getShapes()) {
			if (shape instanceof XSLFTextShape) {
				XSLFTextShape textShape = (XSLFTextShape)shape;
				String text = textShape.getText();
				if (text != null && !text.trim().isEmpty()) {
					slideContent.append(text.trim()).append("\n");
				}
			}
		}

		return slideContent.toString().trim();
	}
}