package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.exception.InvalidFileException;
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
class ValidatorServiceTest {

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ValidatorService validatorService;

    @Test
    @DisplayName("Debe lanzar InvalidFileException si el archivo recibido es nulo")
    void validate_WhenFileIsNull_ShouldThrowInvalidFileException() {
        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(null)
        );
        assertEquals("El archivo no puede estar vacío.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si el archivo está vacío")
    void validate_WhenFileIsEmpty_ShouldThrowInvalidFileException() {
        when(file.isEmpty()).thenReturn(true);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(file)
        );
        assertEquals("El archivo no puede estar vacío.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si el peso del archivo supera el límite de 50MB")
    void validate_WhenFileSizeExceedsLimit_ShouldThrowInvalidFileException() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(52_428_801L);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(file)
        );
        assertEquals("El archivo supera el límite máximo de 50 MB soportado por VERA.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si la extensión del archivo no está soportada")
    void validate_WhenUnsupportedExtension_ShouldThrowInvalidFileException() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("malware.exe");

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                validatorService.validate(file)
        );
        assertEquals("El formato del archivo no está soportado por el motor de IA de VERA.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe ejecutar exitosamente si el archivo es un documento válido soportado")
    void validate_WhenValidDocument_ShouldPassSuccessfully() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("documento.pdf");

        assertDoesNotThrow(() -> validatorService.validate(file));
    }

    @Test
    @DisplayName("Debe ejecutar exitosamente si el archivo es un elemento multimedia válido soportado")
    void validate_WhenValidMultimedia_ShouldPassSuccessfully() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("imagen.PNG");

        assertDoesNotThrow(() -> validatorService.validate(file));
    }

    @Test
    @DisplayName("Debe retornar false en isDocument si el nombre del archivo es nulo")
    void isDocument_WhenFilenameIsNull_ShouldReturnFalse() {
        boolean result = validatorService.isDocument(null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar true en isDocument si posee una extensión de documento válida")
    void isDocument_WhenValidExtension_ShouldReturnTrue() {
        boolean result = validatorService.isDocument("reporte.docx");
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false en isDocument si posee una extensión que no corresponde a documentos")
    void isDocument_WhenInvalidExtension_ShouldReturnFalse() {
        boolean result = validatorService.isDocument("foto.jpg");
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar false en isDocument si el archivo no posee ninguna extensión")
    void isDocument_WhenFilenameHasNoExtension_ShouldReturnFalse() {
        boolean result = validatorService.isDocument("archivoSinExtension");
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar false en isMultimedia si el nombre del archivo es nulo")
    void isMultimedia_WhenFilenameIsNull_ShouldReturnFalse() {
        boolean result = validatorService.isMultimedia(null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar true en isMultimedia si posee una extensión de imagen válida")
    void isMultimedia_WhenValidImageExtension_ShouldReturnTrue() {
        boolean result = validatorService.isMultimedia("avatar.webp");
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar true en isMultimedia si posee una extensión de audio válida")
    void isMultimedia_WhenValidAudioExtension_ShouldReturnTrue() {
        boolean result = validatorService.isMultimedia("cancion.mp3");
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar true en isMultimedia si posee una extensión de video válida")
    void isMultimedia_WhenValidVideoExtension_ShouldReturnTrue() {
        boolean result = validatorService.isMultimedia("video.mp4");
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false en isMultimedia si posee una extensión que no corresponde a multimedia")
    void isMultimedia_WhenInvalidExtension_ShouldReturnFalse() {
        boolean result = validatorService.isMultimedia("datos.xlsx");
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar false en isMultimedia si el archivo no posee ninguna extensión")
    void isMultimedia_WhenFilenameHasNoExtension_ShouldReturnFalse() {
        boolean result = validatorService.isMultimedia("video_sin_formato");
        assertFalse(result);
    }
}