package com.unlam.verabackend.application.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ExtractorService")
class ExtractorServiceTest {

    @InjectMocks
    private ExtractorService extractorService;

    @Mock
    private MultipartFile mockFile;

    @Test
    @DisplayName("Debería extraer múltiples URLs válidas ignorando texto basura circundante")
    void findUrls_ValidText_ShouldExtractUrls() {
        // Arrange
        String text = "Hola, ingresá acá: http://google.com o revisá https://www.unlam.edu.ar/inicio?user=1 para más datos.";

        // Act
        List<String> result = extractorService.findUrls(text);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("http://google.com"));
        assertTrue(result.contains("https://www.unlam.edu.ar/inicio?user=1"));
    }

    @Test
    @DisplayName("Debería devolver lista vacía si el texto de entrada es nulo o está en blanco")
    void findUrls_NullOrBlankText_ShouldReturnEmptyList() {
        assertTrue(extractorService.findUrls(null).isEmpty());
        assertTrue(extractorService.findUrls("   ").isEmpty());
    }

    @Test
    @DisplayName("Debería retornar string vacío si el MultipartFile provisto está vacío o es nulo")
    void convertDocumentToText_NullOrEmptyFile_ShouldReturnEmptyString() {
        assertEquals("", extractorService.convertDocumentToText(null));

        when(mockFile.isEmpty()).thenReturn(true);
        assertEquals("", extractorService.convertDocumentToText(mockFile));
    }

    @Test
    @DisplayName("Debería retornar string vacío si el nombre de archivo no posee extensión o es nulo")
    void convertDocumentToText_FilenameWithoutExtension_ShouldReturnEmptyString() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("archivo_sin_punto");
        assertEquals("", extractorService.convertDocumentToText(mockFile));

        when(mockFile.getOriginalFilename()).thenReturn(null);
        assertEquals("", extractorService.convertDocumentToText(mockFile));
    }

    @Test
    @DisplayName("Debería lanzar IllegalStateException si ocurre un fallo crítico al abrir el inputStream")
    void convertDocumentToText_InputStreamThrowsException_ShouldThrowIllegalStateException() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("documento.txt");
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated disk error"));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> extractorService.convertDocumentToText(mockFile));
    }

    @Test
    @DisplayName("Debería retornar string vacío si la extensión no califica para el raspado (Caso default)")
    void convertDocumentToText_UnsupportedExtension_ShouldReturnEmptyString() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "documento.xyz", "application/octet-stream", "datos crudos".getBytes()
        );

        // Act
        String result = extractorService.convertDocumentToText(file);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debería procesar correctamente archivos estructurados en texto plano (.txt)")
    void convertDocumentToText_TxtFile_ShouldParseCorrectly() {
        String content = "Línea 1\nLínea 2 con enlace http://vera.com";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8)
        );

        String result = extractorService.convertDocumentToText(file);
        assertEquals(content, result.trim());
    }

    @Test
    @DisplayName("Debería procesar correctamente archivos en formato de texto enriquecido (.rtf)")
    void convertDocumentToText_RtfFile_ShouldParseCorrectly() {
        String rtfContent = """
                {\\rtf1\\ansi\\deff0{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}
                \\viewkind4\\uc1\\pard\\lang11274\\f0\\fs20 Contenido RTF de prueba\\par
                }""";
        MockMultipartFile file = new MockMultipartFile(
                "file", "documento.rtf", "application/rtf", rtfContent.getBytes(StandardCharsets.UTF_8)
        );

        String result = extractorService.convertDocumentToText(file);
        assertTrue(result.contains("Contenido RTF de prueba"));
    }

    @Test
    @DisplayName("Debería procesar correctamente archivos PDF reales usando PDFBox")
    void convertDocumentToText_PdfFile_ShouldParseCorrectly() throws IOException {
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Texto dentro del PDF seguro");
                contentStream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfBytes);

        String result = extractorService.convertDocumentToText(file);
        assertTrue(result.contains("Texto dentro del PDF seguro"));
    }

    @Test
    @DisplayName("Debería procesar correctamente documentos de Word (.docx) usando Apache POI")
    void convertDocumentToText_DocxFile_ShouldParseCorrectly() throws IOException {
        byte[] docxBytes;
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("Contenido Word OOXML");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            docxBytes = baos.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes
        );

        String result = extractorService.convertDocumentToText(file);
        assertTrue(result.contains("Contenido Word OOXML"));
    }

    @Test
    @DisplayName("Debería procesar correctamente planillas de Excel (.xlsx) usando Apache POI")
    void convertDocumentToText_XlsxFile_ShouldParseCorrectly() throws IOException {
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("VERA_Sheet").createRow(0).createCell(0).setCellValue("Dato Celda");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes
        );

        String result = extractorService.convertDocumentToText(file);
        assertTrue(result.contains("Dato Celda"));
    }

    @Test
    @DisplayName("Debería procesar correctamente presentaciones de PowerPoint (.pptx) usando Apache POI")
    void convertDocumentToText_PptxFile_ShouldParseCorrectly() throws IOException {
        byte[] pptxBytes;
        try (XMLSlideShow slideshow = new XMLSlideShow()) {
            slideshow.createSlide().createTextBox().addNewTextParagraph().addNewTextRun().setText("Texto Diapositiva");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            slideshow.write(baos);
            pptxBytes = baos.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes
        );

        String result = extractorService.convertDocumentToText(file);
        assertTrue(result.contains("Texto Diapositiva"));
    }
}