package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@Slf4j
@Component
public class ValidatorService {

    private static final long MAX_FILE_SIZE_BYTES = 52_428_800; // 50 Megabytes

    private static final Set<String> GEMINI_SUPPORTED_IMAGES = Set.of(
            "png", "jpeg", "jpg", "webp", "heic", "heif"
    );

    private static final Set<String> GEMINI_SUPPORTED_AUDIO = Set.of(
            "mp3", "mpeg", "wav", "aac", "ogg", "flac"
    );

    private static final Set<String> GEMINI_SUPPORTED_VIDEO = Set.of(
            "mp4", "mpeg", "mov", "avi", "flv", "mpg", "webm", "wmv", "3gp"
    );

    private static final Set<String> GEMINI_SUPPORTED_DOCUMENTS = Set.of(
            "pdf", "txt", "rtf", "docx", "xlsx", "pptx"
    );

    public void validate(MultipartFile file) {
        log.debug("DomainService: Evaluando integridad estructural del archivo adjunto.");

        ensureFileIsNotEmpty(file);
        ensureFileDoesNotExceedSizeLimit(file);
        ensureFormatIsSupported(file.getOriginalFilename());

        log.info("DomainService: El archivo [{}] pasó con éxito la matriz de validaciones de VERA.", file.getOriginalFilename());
    }

    public boolean isDocument(String filename) {
        if (filename == null) return false;
        return GEMINI_SUPPORTED_DOCUMENTS.contains(extractExtension(filename));
    }

    public boolean isMultimedia(String filename) {
        if (filename == null) return false;
        String ext = extractExtension(filename);
        return GEMINI_SUPPORTED_IMAGES.contains(ext) ||
                GEMINI_SUPPORTED_AUDIO.contains(ext) ||
                GEMINI_SUPPORTED_VIDEO.contains(ext);
    }

    private void ensureFileIsNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation Failure: Stream de datos vacío o payload corrupto.");
            throw new InvalidFileException("El archivo no puede estar vacío.");
        }
    }

    private void ensureFileDoesNotExceedSizeLimit(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("Validation Failure: Peso del adjunto supera el umbral límite. Peso registrado: [{}] bytes.", file.getSize());
            throw new InvalidFileException("El archivo supera el límite máximo de 50 MB soportado por VERA.");
        }
    }

    private void ensureFormatIsSupported(String filename) {
        if (!isMultimedia(filename) && !isDocument(filename)) {
            log.warn("Validation Failure: La extensión del archivo [{}] no está mapeada en el ecosistema de IA.", filename);
            throw new InvalidFileException("El formato del archivo no está soportado por el motor de IA de VERA.");
        }
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex + 1).toLowerCase().trim();
    }
}