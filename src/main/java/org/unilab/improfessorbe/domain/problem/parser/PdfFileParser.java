package org.unilab.improfessorbe.domain.problem.parser;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PdfFileParser implements FileParser {

	private final TextPreprocessor textPreprocessor;

	@Override
	public boolean supports(String extension) {
		return "pdf".equalsIgnoreCase(extension);
	}

	@Override
	public String parse(MultipartFile file) throws IOException {
		try (PDDocument document = PDDocument.load(file.getInputStream())) {
			PDFTextStripper stripper = new PDFTextStripper();
			String rawText = stripper.getText(document);
			return textPreprocessor.preprocess(rawText);
		}
	}
}