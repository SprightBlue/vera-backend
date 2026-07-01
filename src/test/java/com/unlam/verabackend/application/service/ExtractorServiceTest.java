package com.unlam.verabackend.application.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractorServiceTest {

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ExtractorService extractorService;

    @Test
    @DisplayName("Debe retornar una lista vacía si el texto provisto es nulo")
    void findUrls_WhenTextIsNull_ShouldReturnEmptyList() {
        List<String> result = extractorService.findUrls(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si el texto provisto no contiene caracteres")
    void findUrls_WhenTextIsEmpty_ShouldReturnEmptyList() {
        List<String> result = extractorService.findUrls("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe capturar y listar correctamente todas las URLs presentes en el texto")
    void findUrls_WhenTextContainsUrls_ShouldReturnListWithUrls() {
        String text = "Visitá https://www.google.com o http://unlam.edu.ar para más info.";

        List<String> result = extractorService.findUrls(text);

        assertEquals(2, result.size());
        assertEquals("https://www.google.com", result.get(0));
        assertEquals("http://unlam.edu.ar", result.get(1));
    }

    @Test
    @DisplayName("Debe retornar un String vacío si el archivo es nulo")
    void convertDocumentToText_WhenFileIsNull_ShouldReturnEmptyString() {
        String result = extractorService.convertDocumentToText(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar un String vacío si el archivo multipart está vacío")
    void convertDocumentToText_WhenFileIsEmpty_ShouldReturnEmptyString() {
        when(file.isEmpty()).thenReturn(true);

        String result = extractorService.convertDocumentToText(file);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar un String vacío si el nombre original del archivo es nulo")
    void convertDocumentToText_WhenFilenameIsNull_ShouldReturnEmptyString() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        String result = extractorService.convertDocumentToText(file);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar un String vacío si el nombre del archivo carece de extensión")
    void convertDocumentToText_WhenFilenameHasNoExtension_ShouldReturnEmptyString() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("archivoSinExtension");

        String result = extractorService.convertDocumentToText(file);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar un String vacío al procesar una extensión no soportada por el switch")
    void convertDocumentToText_WhenExtensionNotSupported_ShouldReturnEmptyString() {
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file", "documento.exe", "application/octet-stream", "bytes".getBytes()
        );

        String result = extractorService.convertDocumentToText(unsupportedFile);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe extraer el texto literal cuando se procesa un archivo TXT")
    void convertDocumentToText_WhenTxtFile_ShouldReturnTextContent() {
        String content = "Línea 1\nLínea 2";
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", content.getBytes()
        );

        String result = extractorService.convertDocumentToText(txtFile);

        assertEquals(content, result);
    }

    @Test
    @DisplayName("Debe interpretar y extraer el contenido legible de un archivo RTF")
    void convertDocumentToText_WhenRtfFile_ShouldExtractText() {
        String rtfContent = "{\\rtf1\\ansi\\deff0 Hola Mundo RTF}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", "test.rtf", "application/rtf", rtfContent.getBytes()
        );

        String result = extractorService.convertDocumentToText(rtfFile);

        assertNotNull(result);
        assertTrue(result.contains("Hola Mundo RTF"));
    }

    @Test
    @DisplayName("Debe inicializar la API de PDFBox y extraer el texto de un PDF válido")
    void convertDocumentToText_WhenPdfFile_ShouldCoverAllInternalLines() throws IOException {
        byte[] pdfBytes = generateValidPdfBytes();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes
        );

        String result = extractorService.convertDocumentToText(pdfFile);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe inicializar Apache POI y extraer el contenido de un archivo DOCX de Word")
    void convertDocumentToText_WhenDocxFile_ShouldCoverAllInternalLines() throws IOException {
        byte[] docxBytes = generateValidDocxBytes();
        MockMultipartFile docxFile = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes
        );

        String result = extractorService.convertDocumentToText(docxFile);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe procesar e indexar las celdas de un archivo XLSX de Excel")
    void convertDocumentToText_WhenXlsxFile_ShouldCoverAllInternalLines() throws IOException {
        byte[] xlsxBytes = generateValidXlsxBytes();
        MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes
        );

        String result = extractorService.convertDocumentToText(xlsxFile);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe procesar las diapositivas de una presentación PPTX de PowerPoint")
    void convertDocumentToText_WhenPptxFile_ShouldCoverAllInternalLines() throws IOException {
        byte[] pptxBytes = generateValidPptxBytes();
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes
        );

        String result = extractorService.convertDocumentToText(pptxFile);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException cuando falle la apertura del stream de datos")
    void convertDocumentToText_WhenInputStreamThrowsException_ShouldThrowIllegalStateException() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenThrow(new IOException("Stream caído"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                extractorService.convertDocumentToText(file)
        );

        assertTrue(exception.getMessage().contains("Error en la lectura del documento para la extracción de texto: test.txt"));
    }

    private byte[] generateValidPdfBytes() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
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