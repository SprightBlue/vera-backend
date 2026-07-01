package com.unlam.verabackend.application.service;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Component;
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

@Slf4j
@Component
public class ExtractorService {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    );

    public List<String> findUrls(String text) {
        List<String> urls = new ArrayList<>();
        if (text == null || text.isEmpty()) return urls;

        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) urls.add(matcher.group().trim());

        log.debug("Se encontraron {} URLs en el texto proporcionado.", urls.size());
        return urls;
    }

    public String convertDocumentToText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.debug("El archivo proporcionado para extracción de texto es nulo o está vacío.");
            return "";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            log.warn("El archivo no tiene nombre o no posee una extensión válida.");
            return "";
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        log.info("Iniciando extracción de texto para el archivo: {} (Extensión: {})", filename, extension);

        try (InputStream inputStream = file.getInputStream()) {
            return extractTextByExtension(extension, inputStream);
        } catch (Exception e) {
            log.error("Error en la lectura del documento '{}' para la extracción de texto.", filename, e);
            throw new IllegalStateException("Error en la lectura del documento para la extracción de texto: " + filename, e);
        }
    }

    private String extractTextByExtension(String extension, InputStream inputStream) throws Exception {
        return switch (extension) {
            case "txt" -> extractFromTxt(inputStream);
            case "rtf" -> extractFromRtf(inputStream);
            case "pdf" -> extractFromPdf(inputStream);
            case "docx" -> extractFromDocx(inputStream);
            case "xlsx" -> extractFromXlsx(inputStream);
            case "pptx" -> extractFromPptx(inputStream);
            default -> {
                log.debug("La extensión '{}' no requiere o no soporta extracción de texto enriquecido.", extension);
                yield "";
            }
        };
    }

    private String extractFromTxt(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }

    private String extractFromRtf(InputStream inputStream) throws Exception {
        RTFEditorKit rtfKit = new RTFEditorKit();
        DefaultStyledDocument doc = new DefaultStyledDocument();
        rtfKit.read(inputStream, doc, 0);
        return doc.getText(0, doc.getLength());
    }

    private String extractFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(false);
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                return extractor.getText();
        }
    }

    private String extractFromXlsx(InputStream inputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
             XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook)) {
                extractor.setIncludeSheetNames(false);
                extractor.setFormulasNotResults(false);
                return extractor.getText();
        }
    }

    private String extractFromPptx(InputStream inputStream) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream);
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(ppt)) {
                return extractor.getText();
        }
    }
}