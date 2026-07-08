package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ValidatorService")
class ValidatorServiceTest {

    @InjectMocks
    private ValidatorService validatorService;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Debería pasar la validación completa cuando el archivo es válido, no supera los 50MB y su formato está soportado")
    void validate_ValidFile_ShouldPassSuccessfully() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L * 1024L);
        when(mockFile.getOriginalFilename()).thenReturn("evidencia_fraude.png");

        // Act & Assert
        assertDoesNotThrow(() -> validatorService.validate(mockFile));
    }

    @Test
    @DisplayName("Debería lanzar InvalidFileException si el archivo inyectado es nulo o está vacío")
    void validate_NullOrEmptyFile_ShouldThrowInvalidFileException() {
        // Act & Assert
        when(mockFile.isEmpty()).thenReturn(true);
        InvalidFileException exceptionEmpty = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(mockFile)
        );
        assertEquals("El archivo no puede estar vacío.", exceptionEmpty.getMessage());

        // Act & Assert
        InvalidFileException exceptionNull = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(null)
        );
        assertEquals("El archivo no puede estar vacío.", exceptionNull.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidFileException si el tamaño del archivo excede el límite crítico de 50MB")
    void validate_FileTooLarge_ShouldThrowInvalidFileException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(52_428_801L);

        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(mockFile)
        );
        assertEquals("El archivo supera el límite máximo de 50 MB soportado por VERA.", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidFileException si el formato del archivo no está mapeado en las listas de IA")
    void validate_UnsupportedFormat_ShouldThrowInvalidFileException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(5000L);
        when(mockFile.getOriginalFilename()).thenReturn("ejecutable_malicioso.exe");

        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(mockFile)
        );
        assertEquals("El formato del archivo no está soportado por el motor de IA de VERA.", exception.getMessage());
    }

    @Test
    @DisplayName("Debería identificar correctamente todos los subtipos válidos dentro de la categoría Documentos")
    void isDocument_ValidAndInvalidExtensions_ShouldEvaluateCorrectly() {
        assertTrue(validatorService.isDocument("informe.pdf"));
        assertTrue(validatorService.isDocument("notas.txt"));
        assertTrue(validatorService.isDocument("contrato.docx"));
        assertTrue(validatorService.isDocument("CONTRATO.DOCX"));
        assertTrue(validatorService.isDocument(" datos.xlsx "));

        assertFalse(validatorService.isDocument("foto.jpg"));
        assertFalse(validatorService.isDocument("audio.mp3"));
        assertFalse(validatorService.isDocument(null));
    }

    @Test
    @DisplayName("Debería identificar correctamente todos los subtipos multimedia: Imágenes, Audio y Video")
    void isMultimedia_ValidAndInvalidExtensions_ShouldEvaluateCorrectly() {
        assertTrue(validatorService.isMultimedia("captura.png"));
        assertTrue(validatorService.isMultimedia("foto.jpeg"));

        assertTrue(validatorService.isMultimedia("nota_voz.mp3"));
        assertTrue(validatorService.isMultimedia("grabacion.wav"));

        assertTrue(validatorService.isMultimedia("video_evidencia.mp4"));
        assertTrue(validatorService.isMultimedia("adjunto.mov"));

        assertFalse(validatorService.isMultimedia("documento.pdf"));
        assertFalse(validatorService.isMultimedia("script.sh"));
        assertFalse(validatorService.isMultimedia(null));
    }

    @Test
    @DisplayName("Debería manejar de forma segura los casos límite de nombres de archivo sin extensión o con estructuras complejas")
    void extractExtension_EdgeCases_ShouldBeHandledSafely() {
        assertFalse(validatorService.isDocument("archivo_sin_extension"));
        assertFalse(validatorService.isMultimedia("archivo_sin_extension"));

        assertTrue(validatorService.isDocument("mi.backup.documento.docx"));

        assertFalse(validatorService.isDocument(".gitignore"));
    }
}