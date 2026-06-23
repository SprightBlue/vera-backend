package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ValidatorServiceTest {

    private ValidatorService validatorService;

    @BeforeEach
    void setUp() {
        validatorService = new ValidatorService();
    }

    // ==========================================
    // Tests para el método validate()
    // ==========================================

    @Test
    void validate_WhenFileIsNull_ShouldThrowInvalidFileException() {
        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> validatorService.validate(null));
        assertEquals("El archivo no puede estar vacío.", exception.getMessage());
    }

    @Test
    void validate_WhenFileIsEmpty_ShouldThrowInvalidFileException() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> validatorService.validate(file));
        assertEquals("El archivo no puede estar vacío.", exception.getMessage());
    }

    @Test
    void validate_WhenFileSizeExceedsLimit_ShouldThrowInvalidFileException() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(52_428_801L);

        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> validatorService.validate(file));
        assertEquals("El archivo supera el límite máximo de 50 MB soportado por VERA.", exception.getMessage());
    }

    @Test
    void validate_WhenUnsupportedExtension_ShouldThrowInvalidFileException() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L); // Tamaño válido
        when(file.getOriginalFilename()).thenReturn("archivo.exe");

        // Act & Assert
        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> validatorService.validate(file));
        assertEquals("El formato del archivo no está soportado por el motor de IA de VERA.", exception.getMessage());
    }

    @Test
    void validate_WhenValidDocument_ShouldPassSuccessfully() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("documento.pdf");

        // Act & Assert
        assertDoesNotThrow(() -> validatorService.validate(file));
    }

    @Test
    void validate_WhenValidMultimedia_ShouldPassSuccessfully() {
        // Arrange
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("imagen.PNG");

        // Act & Assert
        assertDoesNotThrow(() -> validatorService.validate(file));
    }

    // ==========================================
    // Tests para el método isDocument()
    // ==========================================

    @Test
    void isDocument_WhenFilenameIsNull_ShouldReturnFalse() {
        // Act
        boolean result = validatorService.isDocument(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isDocument_WhenValidExtension_ShouldReturnTrue() {
        // Arrange
        String filename = "reporte.docx";

        // Act
        boolean result = validatorService.isDocument(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isDocument_WhenInvalidExtension_ShouldReturnFalse() {
        // Arrange
        String filename = "foto.jpg";

        // Act
        boolean result = validatorService.isDocument(filename);

        // Assert
        assertFalse(result);
    }

    // ==========================================
    // Tests para el método isMultimedia()
    // ==========================================

    @Test
    void isMultimedia_WhenFilenameIsNull_ShouldReturnFalse() {
        // Act
        boolean result = validatorService.isMultimedia(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isMultimedia_WhenValidImageExtension_ShouldReturnTrue() {
        // Arrange
        String filename = "avatar.webp";

        // Act
        boolean result = validatorService.isMultimedia(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isMultimedia_WhenValidAudioExtension_ShouldReturnTrue() {
        // Arrange
        String filename = "cancion.mp3";

        // Act
        boolean result = validatorService.isMultimedia(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isMultimedia_WhenValidVideoExtension_ShouldReturnTrue() {
        // Arrange
        String filename = "video.mp4";

        // Act
        boolean result = validatorService.isMultimedia(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isMultimedia_WhenInvalidExtension_ShouldReturnFalse() {
        // Arrange
        String filename = "datos.xlsx";

        // Act
        boolean result = validatorService.isMultimedia(filename);

        // Assert
        assertFalse(result);
    }

    // ==========================================
    // Tests para cubrir la lógica interna de getExtension()
    // ==========================================

    @Test
    void isDocument_WhenFilenameHasNoExtension_ShouldReturnFalse() {
        // Arrange
        String filename = "archivoSinExtension";

        // Act
        boolean result = validatorService.isDocument(filename);

        // Assert
        assertFalse(result);
    }
}