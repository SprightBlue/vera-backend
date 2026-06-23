package com.unlam.verabackend.application.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExtractorService {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    );

    public List<String> findUrls(String text) {
        List<String> urls = new ArrayList<>();
        if (text == null || text.isEmpty()) return urls;

        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group().trim());
        }
        return urls;
    }

    public String convertDocumentToText(MultipartFile file) {
        if (file == null || file.isEmpty()) return "";

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) return "";

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            return switch (extension) {
                case "txt" -> new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                case "rtf" -> {
                    RTFEditorKit rtfKit = new RTFEditorKit();
                    DefaultStyledDocument doc = new DefaultStyledDocument();
                    rtfKit.read(inputStream, doc, 0);
                    yield doc.getText(0, doc.getLength());
                }

                case "pdf" -> {
                    try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        stripper.setSortByPosition(false);
                        yield stripper.getText(document);
                    }
                }

                case "docx" -> {
                    try (XWPFDocument doc = new XWPFDocument(inputStream);
                         XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                        yield extractor.getText();
                    }
                }

                case "xlsx" -> {
                    try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                         XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook)) {
                        extractor.setIncludeSheetNames(false);
                        extractor.setFormulasNotResults(false);
                        yield extractor.getText();
                    }
                }

                case "pptx" -> {
                    try (XMLSlideShow ppt = new XMLSlideShow(inputStream);
                         SlideShowExtractor extractor = new SlideShowExtractor(ppt)) {
                        yield extractor.getText();
                    }
                }

                default -> "";
            };
        } catch (Exception e) {
            throw new RuntimeException("Error en la lectura del documento para la extracción de URLs: " + filename, e);
        }
    }
}