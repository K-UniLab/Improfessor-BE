package org.unilab.improfessorbe.domain.parse.input.convert;

import java.io.IOException;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PptxFileParser implements FileParser {

	private static final String SLIDE_SEPARATOR = "\n--- 슬라이드 구분 ---\n\n";

	@Override
	public boolean supports(String extension) {
		return "pptx".equalsIgnoreCase(extension);
	}

	@Override
	public String parse(MultipartFile file) throws IOException {
		StringBuilder content = new StringBuilder();

		try (XMLSlideShow ppt = new XMLSlideShow(file.getInputStream())) {
			for (XSLFSlide slide : ppt.getSlides()) {
				extractSlideText(slide, content);
				content.append(SLIDE_SEPARATOR);
			}
		}

		return content.toString();
	}

	private void extractSlideText(XSLFSlide slide, StringBuilder content) {
		for (XSLFShape shape : slide.getShapes()) {
			if (shape instanceof XSLFTextShape) {
				XSLFTextShape textShape = (XSLFTextShape)shape;
				content.append(textShape.getText()).append("\n");
			}
		}
	}
}