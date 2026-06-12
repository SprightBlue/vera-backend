package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

@Service
public class ValidatorService {

    private static final long MAX_FILE_SIZE_BYTES = 52_428_800;

    private static final List<String> GEMINI_SUPPORTED_IMAGES = Arrays.asList(
            "png", "jpeg", "jpg", "webp", "heic", "heif"
    );

    private static final List<String> GEMINI_SUPPORTED_AUDIO = Arrays.asList(
            "mp3", "mpeg", "wav", "aac", "ogg", "flac"
    );

    private static final List<String> GEMINI_SUPPORTED_VIDEO = Arrays.asList(
            "mp4", "mpeg", "mov", "avi", "flv", "mpg", "webm", "wmv", "3gp"
    );

    private static final List<String> GEMINI_SUPPORTED_DOCUMENTS = Arrays.asList(
            "pdf", "txt", "rtf", "docx", "xlsx", "pptx"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo no puede estar vacío.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("El archivo supera el límite máximo de 50 MB soportado por VERA.");
        }

        String originalFilename = file.getOriginalFilename();

        if (!isMultimedia(originalFilename) && !isDocument(originalFilename)) {
            throw new InvalidFileException("El formato del archivo no está soportado por el motor de IA de VERA.");
        }
    }

    public boolean isDocument(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename);
        return GEMINI_SUPPORTED_DOCUMENTS.contains(ext);
    }

    public boolean isMultimedia(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename);
        return GEMINI_SUPPORTED_IMAGES.contains(ext) ||
                GEMINI_SUPPORTED_AUDIO.contains(ext) ||
                GEMINI_SUPPORTED_VIDEO.contains(ext);
    }

    private String getExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}