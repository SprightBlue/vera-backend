package com.unlam.verabackend.application.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ExtractorServiceTest {

    private ExtractorService extractorService;

    @BeforeEach
    void setUp() {
        extractorService = new ExtractorService();
    }

    // ==========================================
    // Tests para el método findUrls()
    // ==========================================

    @Test
    void findUrls_WhenTextIsNull_ShouldReturnEmptyList() {
        // Arrange

        // Act
        List<String> result = extractorService.findUrls(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findUrls_WhenTextIsEmpty_ShouldReturnEmptyList() {
        // Arrange
        String text = "";

        // Act
        List<String> result = extractorService.findUrls(text);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findUrls_WhenTextContainsUrls_ShouldReturnListWithUrls() {
        // Arrange
        String text = "Visitá https://www.google.com o http://unlam.edu.ar para más info.";

        // Act
        List<String> result = extractorService.findUrls(text);

        // Assert
        assertEquals(2, result.size());
        assertEquals("https://www.google.com", result.get(0));
        assertEquals("http://unlam.edu.ar", result.get(1));
    }

    // ==========================================
    // Tests para Cláusulas de Guarda Iniciales
    // ==========================================

    @Test
    void convertDocumentToText_WhenFileIsNull_ShouldReturnEmptyString() {
        // Arrange

        // Act
        String result = extractorService.convertDocumentToText(null);

        // Assert
        assertEquals("", result);
    }

    @Test
    void convertDocumentToText_WhenFileIsEmpty_ShouldReturnEmptyString() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals("", result);
    }

    @Test
    void convertDocumentToText_WhenFilenameIsNull_ShouldReturnEmptyString() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals("", result);
    }

    @Test
    void convertDocumentToText_WhenFilenameHasNoExtension_ShouldReturnEmptyString() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("archivoSinExtension");

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals("", result);
    }

    @Test
    void convertDocumentToText_WhenExtensionNotSupported_ShouldReturnEmptyString() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "documento.exe", "application/octet-stream", "bytes".getBytes()
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals("", result);
    }

    // ==========================================
    // Caminos Felices: Cubriendo el Switch Línea por Línea
    // ==========================================

    @Test
    void convertDocumentToText_WhenTxtFile_ShouldReturnTextContent() {
        // Arrange
        String content = "Línea 1\nLínea 2";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content.getBytes()
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals(content, result);
    }

    @Test
    void convertDocumentToText_WhenRtfFile_ShouldExtractText() {
        // Arrange
        String rtfContent = "{\\rtf1\\ansi\\deff0 Hola Mundo RTF}";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rtf", "application/rtf", rtfContent.getBytes()
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Hola Mundo RTF"));
    }

    @Test
    void convertDocumentToText_WhenPdfFile_ShouldCoverAllInternalLines() throws IOException {
        // Arrange
        byte[] pdfBytes = generateValidPdfBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertNotNull(result);
    }

    @Test
    void convertDocumentToText_WhenDocxFile_ShouldCoverAllInternalLines() throws IOException {
        // Arrange
        byte[] docxBytes = generateValidDocxBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertNotNull(result);
    }

    @Test
    void convertDocumentToText_WhenXlsxFile_ShouldCoverAllInternalLines() throws IOException {
        // Arrange
        byte[] xlsxBytes = generateValidXlsxBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertNotNull(result);
    }

    @Test
    void convertDocumentToText_WhenPptxFile_ShouldCoverAllInternalLines() throws IOException {
        // Arrange
        byte[] pptxBytes = generateValidPptxBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertNotNull(result);
    }

    // ==========================================
    // Cobertura del bloque Catch General
    // ==========================================

    @Test
    void convertDocumentToText_WhenInputStreamThrowsException_ShouldThrowRuntimeException() throws IOException {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenThrow(new IOException("Stream caído"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> extractorService.convertDocumentToText(file));

        assertTrue(exception.getMessage().contains("Error en la lectura del documento para la extracción de URLs: test.txt"));
    }

    // ==========================================
    // Métodos Helper para generar Binarios Estructurados Válidos
    // ==========================================

    private byte[] generateValidPdfBytes() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // Un documento de PDFBox necesita al menos una página interna para ser procesable sin excepciones
            document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            document.save(bos);
            return bos.toByteArray();
        }
    }

    private byte[] generateValidDocxBytes() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText("Texto Docx");
            doc.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] generateValidXlsxBytes() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.createSheet("Hoja1").createRow(0).createCell(0).setCellValue("Texto Excel");
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] generateValidPptxBytes() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ppt.createSlide();
            ppt.write(bos);
            return bos.toByteArray();
        }
    }
}