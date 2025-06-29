package org.unilab.improfessorbe.domain.parse.input.convert;

import java.io.IOException;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PptFileParser implements FileParser {

	private static final String SLIDE_SEPARATOR = "\n--- 슬라이드 구분 ---\n\n";

	@Override
	public boolean supports(String extension) {
		return "ppt".equalsIgnoreCase(extension);
	}

	@Override
	public String parse(MultipartFile file) throws IOException {
		StringBuilder content = new StringBuilder();

		try (HSLFSlideShow ppt = new HSLFSlideShow(file.getInputStream())) {
			for (HSLFSlide slide : ppt.getSlides()) {
				extractSlideText(slide, content);
				content.append(SLIDE_SEPARATOR);
			}
		}

		return content.toString();
	}

	private void extractSlideText(HSLFSlide slide, StringBuilder content) {
		for (HSLFShape shape : slide.getShapes()) {
			if (shape instanceof HSLFTextShape) {
				HSLFTextShape textShape = (HSLFTextShape)shape;
				content.append(textShape.getText()).append("\n");
			}
		}
	}
}